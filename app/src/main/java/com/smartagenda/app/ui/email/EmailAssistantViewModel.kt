package com.smartagenda.app.ui.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartagenda.app.auth.AuthState
import com.smartagenda.app.auth.GoogleAuthManager
import com.smartagenda.app.data.local.EmailDraftEntity
import com.smartagenda.app.data.local.EmailMessageEntity
import com.smartagenda.app.data.repository.EmailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class EmailAssistantViewModel(
    private val emailRepository: EmailRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredEmails: StateFlow<List<EmailMessageEntity>> = combine(
        emailRepository.allEmails,
        _selectedCategory
    ) { emails, category ->
        if (category == "All") emails
        else emails.filter { it.category.equals(category, ignoreCase = true) }
    }.let { flow ->
        val stateFlow = MutableStateFlow<List<EmailMessageEntity>>(emptyList())
        viewModelScope.launch {
            flow.collect { stateFlow.value = it }
        }
        stateFlow.asStateFlow()
    }

    private val _selectedEmail = MutableStateFlow<EmailMessageEntity?>(null)
    val selectedEmail: StateFlow<EmailMessageEntity?> = _selectedEmail.asStateFlow()

    private val _currentDraft = MutableStateFlow<EmailDraftEntity?>(null)
    val currentDraft: StateFlow<EmailDraftEntity?> = _currentDraft.asStateFlow()

    private val _isGeneratingDraft = MutableStateFlow(false)
    val isGeneratingDraft: StateFlow<Boolean> = _isGeneratingDraft.asStateFlow()

    private val _isSyncingGmail = MutableStateFlow(false)
    val isSyncingGmail: StateFlow<Boolean> = _isSyncingGmail.asStateFlow()

    val categories = listOf("All", "Urgent", "Action Required", "Needs Reply", "Informational", "Newsletter")

    init {
        viewModelScope.launch {
            emailRepository.ensureInitialEmailsLoaded()
        }
    }

    fun syncGmailInbox(token: String = "") {
        val authState = GoogleAuthManager.authState.value
        val accessToken = token.ifBlank {
            if (authState is AuthState.Connected) authState.accountInfo.accessToken ?: "" else ""
        }

        if (accessToken.isBlank()) {
            // No OAuth access token available — can't sync
            return
        }

        viewModelScope.launch {
            _isSyncingGmail.value = true
            GoogleAuthManager.setSyncing(true)
            try {
                emailRepository.syncGmailInbox(accessToken)
            } catch (e: Exception) {
                // handle sync error gracefully
            } finally {
                _isSyncingGmail.value = false
                GoogleAuthManager.setSyncing(false)
            }
        }
    }

    fun syncGmail() {
        syncGmailInbox()
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectEmail(email: EmailMessageEntity?) {
        _selectedEmail.value = email
        if (email != null && !email.isRead) {
            viewModelScope.launch {
                emailRepository.markAsRead(email.id, true)
            }
        }
    }

    fun toggleStar(email: EmailMessageEntity) {
        viewModelScope.launch {
            emailRepository.markAsStarred(email.id, !email.isStarred)
        }
    }

    fun generateAiDraft(email: EmailMessageEntity, tone: String = "Professional") {
        viewModelScope.launch {
            _isGeneratingDraft.value = true
            try {
                val draft = emailRepository.generateAiDraft(email, tone)
                _currentDraft.value = draft
                _selectedEmail.value = null // Close detail dialog and present human approval draft dialog
            } catch (e: Exception) {
                // handle error
            } finally {
                _isGeneratingDraft.value = false
            }
        }
    }

    fun regenerateDraftWithTone(tone: String) {
        val draft = _currentDraft.value ?: return
        viewModelScope.launch {
            _isGeneratingDraft.value = true
            try {
                val email = _selectedEmail.value ?: emailRepository.fetchEmailById(draft.emailId)
                if (email != null) {
                    val newDraft = emailRepository.generateAiDraft(email, tone)
                    _currentDraft.value = newDraft
                } else {
                    val updatedDraft = draft.copy(tone = tone)
                    emailRepository.updateDraft(updatedDraft)
                    _currentDraft.value = updatedDraft
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isGeneratingDraft.value = false
            }
        }
    }

    fun approveAndSendDraft(draft: EmailDraftEntity) {
        viewModelScope.launch {
            val authState = GoogleAuthManager.authState.value
            val token = if (authState is AuthState.Connected) authState.accountInfo.accessToken?.takeIf { it.isNotBlank() } else null
            
            val result = emailRepository.approveAndSendDraft(draft, token)
            if (result.isSuccess) {
                _currentDraft.value = null
            }
        }
    }

    fun saveDraftToGmail(draft: EmailDraftEntity, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val authState = GoogleAuthManager.authState.value
            val token = if (authState is AuthState.Connected) authState.accountInfo.accessToken?.takeIf { it.isNotBlank() } else null
            if (token.isNullOrBlank()) {
                onResult(false, "Google account is not connected. Please connect in Settings.")
                return@launch
            }
            val result = emailRepository.saveDraftToGmail(draft, token)
            if (result.isSuccess) {
                emailRepository.updateDraft(draft)
                _currentDraft.value = null
                onResult(true, "Draft saved to your Gmail drafts!")
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to save draft to Gmail"
                onResult(false, err)
            }
        }
    }

    fun discardDraft() {
        val draft = _currentDraft.value ?: return
        viewModelScope.launch {
            emailRepository.discardDraft(draft.id)
            _currentDraft.value = null
        }
    }

    fun dismissDraftDialog() {
        _currentDraft.value = null
    }

    class Factory(private val repository: EmailRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EmailAssistantViewModel::class.java)) {
                return EmailAssistantViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
