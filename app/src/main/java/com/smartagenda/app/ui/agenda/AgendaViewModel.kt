package com.smartagenda.app.ui.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartagenda.app.ai.ParsedReminder
import com.smartagenda.app.data.local.ReminderEntity
import com.smartagenda.app.data.repository.ReminderRepository
import com.smartagenda.app.notification.NotificationScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AgendaViewModel(
    application: Application,
    private val repository: ReminderRepository
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>().applicationContext

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isQuickAddParsing = MutableStateFlow(false)
    val isQuickAddParsing: StateFlow<Boolean> = _isQuickAddParsing.asStateFlow()

    private val _quickAddPreview = MutableStateFlow<ParsedReminder?>(null)
    val quickAddPreview: StateFlow<ParsedReminder?> = _quickAddPreview.asStateFlow()

    private val _quickAddText = MutableStateFlow("")
    val quickAddText: StateFlow<String> = _quickAddText.asStateFlow()

    private val _quickAddError = MutableStateFlow<String?>(null)
    val quickAddError: StateFlow<String?> = _quickAddError.asStateFlow()

    val categories = listOf("All", "Personal", "Work", "Health", "Study", "Finance")

    @OptIn(ExperimentalCoroutinesApi::class)
    val remindersForSelectedDate: StateFlow<List<ReminderEntity>> = combine(
        _selectedDate,
        _selectedCategory,
        _searchQuery
    ) { date, category, query -> Triple(date, category, query) }
        .flatMapLatest { (date, category, query) ->
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val baseFlow = if (category == "All") {
                repository.getRemindersForDate(dateStr)
            } else {
                repository.getRemindersForDateAndCategory(dateStr, category)
            }
            baseFlow.combine(MutableStateFlow(query)) { list, q ->
                if (q.isBlank()) {
                    list
                } else {
                    list.filter { item ->
                        item.title.contains(q, ignoreCase = true) ||
                                item.notes.contains(q, ignoreCase = true) ||
                                item.category.contains(q, ignoreCase = true)
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allReminders: StateFlow<List<ReminderEntity>> = repository.getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateQuickAddText(text: String) {
        _quickAddText.value = text
        _quickAddError.value = null
    }

    fun parseQuickAddText() {
        val input = _quickAddText.value
        if (input.isBlank()) return
        
        viewModelScope.launch {
            _isQuickAddParsing.value = true
            _quickAddError.value = null
            _quickAddPreview.value = null
            try {
                val parsed = repository.parseNaturalLanguageReminder(input)
                _quickAddPreview.value = parsed
            } catch (e: Exception) {
                _quickAddError.value = "AI couldn't process that right now."
            } finally {
                _isQuickAddParsing.value = false
            }
        }
    }

    fun confirmQuickAdd() {
        val parsed = _quickAddPreview.value ?: return
        viewModelScope.launch {
            val reminder = ReminderEntity(
                title = parsed.title,
                notes = parsed.notes,
                scheduledDate = parsed.dateString,
                scheduledTime = parsed.timeString,
                category = parsed.category,
                isCompleted = false
            )
            val id = repository.insertReminder(reminder)
            val savedReminder = reminder.copy(id = id)
            NotificationScheduler.scheduleNotification(context, savedReminder)
            _quickAddPreview.value = null
            _quickAddText.value = ""
        }
    }

    fun cancelQuickAdd() {
        _quickAddPreview.value = null
    }

    fun toggleReminderCompleted(reminder: ReminderEntity) {
        viewModelScope.launch {
            val isNowCompleted = !reminder.isCompleted
            val updated = reminder.copy(isCompleted = isNowCompleted)
            repository.updateReminder(updated)

            if (isNowCompleted) {
                NotificationScheduler.cancelNotification(context, reminder.id.toInt())
            } else {
                NotificationScheduler.scheduleNotification(context, updated)
            }
        }
    }

    fun addReminder(
        title: String,
        notes: String = "",
        scheduledDateStr: String,
        scheduledTimeStr: String,
        category: String = "Personal"
    ) {
        viewModelScope.launch {
            val newReminder = ReminderEntity(
                title = title,
                notes = notes,
                scheduledDate = scheduledDateStr,
                scheduledTime = scheduledTimeStr,
                category = category,
                isCompleted = false
            )
            val generatedId = repository.insertReminder(newReminder)
            val savedReminder = newReminder.copy(id = generatedId)
            NotificationScheduler.scheduleNotification(context, savedReminder)
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            if (reminder.isCompleted) {
                NotificationScheduler.cancelNotification(context, reminder.id.toInt())
            } else {
                NotificationScheduler.scheduleNotification(context, reminder)
            }
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            NotificationScheduler.cancelNotification(context, reminder.id.toInt())
            repository.deleteReminder(reminder)
        }
    }
}

class AgendaViewModelFactory(
    private val application: Application,
    private val repository: ReminderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendaViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
