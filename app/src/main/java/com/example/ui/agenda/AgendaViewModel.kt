package com.example.ui.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.NaturalLanguageReminderParser
import com.example.ai.ParsedReminder
import com.example.data.local.AppDatabase
import com.example.data.local.ReminderEntity
import com.example.data.repository.ReminderRepository
import com.example.notification.ReminderScheduler
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AgendaFilter {
    ALL,
    TODAY,
    UPCOMING,
    COMPLETED
}

data class AgendaUiState(
    val reminders: List<ReminderEntity> = emptyList(),
    val filteredReminders: List<ReminderEntity> = emptyList(),
    val selectedFilter: AgendaFilter = AgendaFilter.ALL,
    val isAddEditDialogOpen: Boolean = false,
    val editingReminder: ReminderEntity? = null,
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val todayCount: Int = 0,
    val parsedAiReminder: ParsedReminder? = null,
    val isParsingAi: Boolean = false,
    val aiStatusMessage: String? = null
)

class AgendaViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ReminderRepository = ReminderRepository(
        AppDatabase.getDatabase(application).reminderDao()
    )
) : AndroidViewModel(application) {

    private val _selectedFilter = MutableStateFlow(AgendaFilter.ALL)
    val selectedFilter: StateFlow<AgendaFilter> = _selectedFilter.asStateFlow()

    private val _isAddEditDialogOpen = MutableStateFlow(false)
    val isAddEditDialogOpen: StateFlow<Boolean> = _isAddEditDialogOpen.asStateFlow()

    private val _editingReminder = MutableStateFlow<ReminderEntity?>(null)
    val editingReminder: StateFlow<ReminderEntity?> = _editingReminder.asStateFlow()

    private val _parsedAiReminder = MutableStateFlow<ParsedReminder?>(null)
    private val _isParsingAi = MutableStateFlow(false)
    private val _aiStatusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AgendaUiState> = combine(
        repository.allReminders,
        _selectedFilter,
        _isAddEditDialogOpen,
        _editingReminder,
        _parsedAiReminder,
        _isParsingAi,
        _aiStatusMessage
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val allItems = params[0] as List<ReminderEntity>
        val filter = params[1] as AgendaFilter
        val isDialogOpen = params[2] as Boolean
        val editingItem = params[3] as ReminderEntity?
        val parsedAi = params[4] as ParsedReminder?
        val isParsing = params[5] as Boolean
        val aiStatus = params[6] as String?

        val now = System.currentTimeMillis()
        val todayStr = DateTimeUtils.getTodayDateString()

        val filtered = when (filter) {
            AgendaFilter.ALL -> allItems
            AgendaFilter.TODAY -> allItems.filter { it.dateString == todayStr }
            AgendaFilter.UPCOMING -> allItems.filter {
                !it.isCompleted && (it.dateString > todayStr || (it.dateString == todayStr && it.scheduledEpochMillis > now))
            }
            AgendaFilter.COMPLETED -> allItems.filter { it.isCompleted }
        }

        val completed = allItems.count { it.isCompleted }
        val today = allItems.count { it.dateString == todayStr }

        AgendaUiState(
            reminders = allItems,
            filteredReminders = filtered,
            selectedFilter = filter,
            isAddEditDialogOpen = isDialogOpen,
            editingReminder = editingItem,
            totalCount = allItems.size,
            completedCount = completed,
            todayCount = today,
            parsedAiReminder = parsedAi,
            isParsingAi = isParsing,
            aiStatusMessage = aiStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AgendaUiState()
    )

    fun parseNaturalLanguage(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _isParsingAi.value = true
            _aiStatusMessage.value = "Analyzing with AI..."
            try {
                val result = NaturalLanguageReminderParser.parse(input)
                _parsedAiReminder.value = result
                _aiStatusMessage.value = "Parsed with ${result.confidence}!"
            } catch (e: Exception) {
                _aiStatusMessage.value = "Error parsing: ${e.message}"
            } finally {
                _isParsingAi.value = false
            }
        }
    }

    fun dismissParsedAi() {
        _parsedAiReminder.value = null
        _aiStatusMessage.value = null
    }

    fun confirmParsedAiReminder(parsed: ParsedReminder) {
        saveReminder(
            id = 0L,
            title = parsed.title,
            notes = parsed.notes,
            dateString = parsed.dateString,
            timeString = parsed.timeString,
            category = parsed.category
        )
        dismissParsedAi()
    }

    fun setFilter(filter: AgendaFilter) {
        _selectedFilter.value = filter
    }

    fun openAddDialog() {
        _editingReminder.value = null
        _isAddEditDialogOpen.value = true
    }

    fun openEditDialog(reminder: ReminderEntity) {
        _editingReminder.value = reminder
        _isAddEditDialogOpen.value = true
    }

    fun closeDialog() {
        _isAddEditDialogOpen.value = false
        _editingReminder.value = null
    }

    fun saveReminder(
        id: Long = 0L,
        title: String,
        notes: String,
        dateString: String,
        timeString: String,
        category: String
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val epochMillis = DateTimeUtils.calculateEpochMillis(dateString, timeString)

        viewModelScope.launch {
            val context = getApplication<Application>()
            if (id == 0L) {
                // New reminder
                val newEntity = ReminderEntity(
                    title = trimmedTitle,
                    notes = notes.trim(),
                    dateString = dateString,
                    timeString = timeString,
                    scheduledEpochMillis = epochMillis,
                    isCompleted = false,
                    category = category
                )
                val generatedId = repository.insertReminder(newEntity)
                val savedReminder = newEntity.copy(id = generatedId)
                ReminderScheduler.schedule(context, savedReminder)
            } else {
                // Edit existing reminder
                val existing = _editingReminder.value
                val updatedEntity = ReminderEntity(
                    id = id,
                    title = trimmedTitle,
                    notes = notes.trim(),
                    dateString = dateString,
                    timeString = timeString,
                    scheduledEpochMillis = epochMillis,
                    isCompleted = existing?.isCompleted ?: false,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    category = category
                )
                repository.updateReminder(updatedEntity)
                // Cancel previous alarm and reschedule with updated time
                ReminderScheduler.cancel(context, id)
                if (!updatedEntity.isCompleted) {
                    ReminderScheduler.schedule(context, updatedEntity)
                }
            }
            closeDialog()
        }
    }

    fun toggleComplete(reminder: ReminderEntity) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val newCompleted = !reminder.isCompleted
            repository.setCompleted(reminder.id, newCompleted)

            if (newCompleted) {
                // Cancel scheduled notification if completed early
                ReminderScheduler.cancel(context, reminder.id)
            } else {
                // Reschedule if uncompleted and in future
                val updated = reminder.copy(isCompleted = false)
                ReminderScheduler.schedule(context, updated)
            }
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            ReminderScheduler.cancel(context, reminder.id)
            repository.deleteReminder(reminder)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AgendaViewModel(application) as T
                }
            }
    }
}
