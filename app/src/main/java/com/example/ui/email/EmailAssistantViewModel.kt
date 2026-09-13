package com.example.ui.email

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.EmailDraftEntity
import com.example.data.local.EmailMessageEntity
import com.example.data.repository.EmailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EmailUiState(
    val emails: List<EmailMessageEntity> = emptyList(),
    val drafts: List<EmailDraftEntity> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val selectedEmail: EmailMessageEntity? = null,
    val activeDraft: EmailDraftEntity? = null,
    val isGeneratingDraft: Boolean = false,
    val isSendingDraft: Boolean = false,
    val showApprovalDialog: Boolean = false,
    val showConfirmSendDialog: Boolean = false,
    val userFeedbackMessage: String? = null
)

class EmailAssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EmailRepository

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedEmail = MutableStateFlow<EmailMessageEntity?>(null)
    private val _activeDraft = MutableStateFlow<EmailDraftEntity?>(null)
    private val _isGeneratingDraft = MutableStateFlow(false)
    private val _isSendingDraft = MutableStateFlow(false)
    private val _showApprovalDialog = MutableStateFlow(false)
    private val _showConfirmSendDialog = MutableStateFlow(false)
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = EmailRepository(database.emailDao())
        viewModelScope.launch {
            repository.ensureInitialEmailsLoaded()
        }
    }

    val uiState: StateFlow<EmailUiState> = combine(
        repository.allEmails,
        repository.allDrafts,
        _selectedCategory,
        _searchQuery,
        _selectedEmail,
        _activeDraft,
        _isGeneratingDraft,
        _showApprovalDialog,
        _showConfirmSendDialog,
        _userFeedbackMessage
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val allEmails = params[0] as List<EmailMessageEntity>
        @Suppress("UNCHECKED_CAST")
        val allDrafts = params[1] as List<EmailDraftEntity>
        val category = params[2] as String?
        val query = params[3] as String
        val selectedEmail = params[4] as EmailMessageEntity?
        val activeDraft = params[5] as EmailDraftEntity?
        val isGenerating = params[6] as Boolean
        val showApproval = params[7] as Boolean
        val showConfirmSend = params[8] as Boolean
        val feedback = params[9] as String?

        val filteredEmails = allEmails.filter { email ->
            val matchesCategory = category == null || email.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                email.subject.contains(query, ignoreCase = true) ||
                email.senderName.contains(query, ignoreCase = true) ||
                email.snippet.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        EmailUiState(
            emails = filteredEmails,
            drafts = allDrafts,
            selectedCategory = category,
            searchQuery = query,
            selectedEmail = selectedEmail,
            activeDraft = activeDraft,
            isGeneratingDraft = isGenerating,
            showApprovalDialog = showApproval,
            showConfirmSendDialog = showConfirmSend,
            userFeedbackMessage = feedback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EmailUiState()
    )

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openEmailDetails(email: EmailMessageEntity) {
        _selectedEmail.value = email
        viewModelScope.launch {
            repository.markAsRead(email.id, true)
            // Check if draft exists
            val existingDraft = uiState.value.drafts.firstOrNull { it.emailId == email.id }
            _activeDraft.value = existingDraft
        }
    }

    fun closeEmailDetails() {
        _selectedEmail.value = null
        _activeDraft.value = null
        _showApprovalDialog.value = false
        _showConfirmSendDialog.value = false
    }

    fun toggleStarred(email: EmailMessageEntity) {
        viewModelScope.launch {
            repository.markAsStarred(email.id, !email.isStarred)
        }
    }

    fun generateAiDraft(email: EmailMessageEntity, tone: String = "Professional", customPrompt: String? = null) {
        viewModelScope.launch {
            _isGeneratingDraft.value = true
            _selectedEmail.value = email
            try {
                val draft = repository.generateAiDraft(email, tone, customPrompt)
                _activeDraft.value = draft
                _showApprovalDialog.value = true
                _userFeedbackMessage.value = "AI draft generated. Human review required."
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Failed to generate draft: ${e.message}"
            } finally {
                _isGeneratingDraft.value = false
            }
        }
    }

    fun openDraftReview(email: EmailMessageEntity) {
        val draft = uiState.value.drafts.firstOrNull { it.emailId == email.id }
        if (draft != null) {
            _selectedEmail.value = email
            _activeDraft.value = draft
            _showApprovalDialog.value = true
        } else {
            generateAiDraft(email)
        }
    }

    fun updateDraftText(newSubject: String, newBody: String) {
        val current = _activeDraft.value ?: return
        val updated = current.copy(subject = newSubject, draftBody = newBody)
        _activeDraft.value = updated
        viewModelScope.launch {
            repository.updateDraft(updated)
        }
    }

    fun requestSendApproval() {
        _showConfirmSendDialog.value = true
    }

    fun dismissConfirmSend() {
        _showConfirmSendDialog.value = false
    }

    fun confirmAndSendDraft() {
        val draft = _activeDraft.value ?: return
        viewModelScope.launch {
            _isSendingDraft.value = true
            try {
                repository.approveAndSendDraft(draft)
                _userFeedbackMessage.value = "Approved & sent to ${draft.recipientEmail}"
                _showConfirmSendDialog.value = false
                _showApprovalDialog.value = false
                _activeDraft.value = null
                _selectedEmail.value = null
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Failed to send: ${e.message}"
            } finally {
                _isSendingDraft.value = false
            }
        }
    }

    fun discardDraft() {
        val draft = _activeDraft.value ?: return
        viewModelScope.launch {
            repository.discardDraft(draft.id)
            _activeDraft.value = null
            _showApprovalDialog.value = false
            _showConfirmSendDialog.value = false
            _userFeedbackMessage.value = "Draft discarded"
        }
    }

    fun dismissApprovalDialog() {
        _showApprovalDialog.value = false
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EmailAssistantViewModel(application) as T
                }
            }
    }
}
