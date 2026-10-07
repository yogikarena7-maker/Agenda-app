package com.smartagenda.app.data.repository

import android.util.Log
import com.smartagenda.app.ai.GeminiEmailAssistant
import com.smartagenda.app.data.local.EmailDao
import com.smartagenda.app.data.local.EmailDraftEntity
import com.smartagenda.app.data.local.EmailMessageEntity
import com.smartagenda.app.data.remote.GmailApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class EmailRepository(private val emailDao: EmailDao) {

    val allEmails: Flow<List<EmailMessageEntity>> = emailDao.getAllEmails()
    val allDrafts: Flow<List<EmailDraftEntity>> = emailDao.getAllDrafts()

    fun getEmailById(id: String): Flow<EmailMessageEntity?> = emailDao.getEmailById(id)

    fun getDraftForEmail(emailId: String): Flow<EmailDraftEntity?> = emailDao.getDraftForEmail(emailId)

    suspend fun markAsRead(id: String, isRead: Boolean) = withContext(Dispatchers.IO) {
        emailDao.markAsRead(id, isRead)
    }

    suspend fun markAsStarred(id: String, isStarred: Boolean) = withContext(Dispatchers.IO) {
        emailDao.markAsStarred(id, isStarred)
    }

    suspend fun syncGmailInbox(accessToken: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val result = GmailApiClient.fetchInboxMessages(accessToken, maxResults = 10)
            if (result.isSuccess) {
                val emails = result.getOrDefault(emptyList())
                if (emails.isNotEmpty()) {
                    emailDao.insertEmails(emails)
                    Log.d("EmailRepository", "Successfully synced ${emails.size} real Gmail emails")
                    return@withContext Result.success(emails.size)
                }
            }
            Result.success(0)
        } catch (e: Exception) {
            Log.e("EmailRepository", "Error syncing Gmail inbox", e)
            Result.failure(e)
        }
    }

    suspend fun generateAiDraft(
        email: EmailMessageEntity,
        tone: String = "Professional",
        customPrompt: String? = null
    ): EmailDraftEntity = withContext(Dispatchers.IO) {
        val draftBody = GeminiEmailAssistant.generateDraftReply(email, tone, customPrompt)
        val draft = EmailDraftEntity(
            emailId = email.id,
            recipientEmail = email.senderEmail,
            recipientName = email.senderName,
            subject = if (email.subject.startsWith("Re:", ignoreCase = true)) email.subject else "Re: ${email.subject}",
            draftBody = draftBody,
            tone = tone,
            status = "PENDING_APPROVAL",
            createdAt = System.currentTimeMillis()
        )
        val id = emailDao.insertDraft(draft)
        draft.copy(id = id)
    }

    suspend fun updateDraft(draft: EmailDraftEntity) = withContext(Dispatchers.IO) {
        emailDao.updateDraft(draft)
    }

    suspend fun saveDraftToGmail(
        draft: EmailDraftEntity,
        accessToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val originalEmail = emailDao.getEmailById(draft.emailId).firstOrNull()
            val threadId = originalEmail?.threadId
            GmailApiClient.createDraftInGmail(accessToken, draft, threadId)
        } catch (e: Exception) {
            Log.e("EmailRepository", "Error saving draft to Gmail", e)
            Result.failure(e)
        }
    }

    suspend fun approveAndSendDraft(
        draft: EmailDraftEntity,
        accessToken: String? = null
    ): Result<EmailDraftEntity> = withContext(Dispatchers.IO) {
        try {
            var sentViaGmail = false
            if (!accessToken.isNullOrBlank()) {
                val originalEmail = emailDao.getEmailById(draft.emailId).firstOrNull()
                val threadId = originalEmail?.threadId
                val sendResult = GmailApiClient.sendEmailReply(accessToken, draft, threadId)
                if (sendResult.isFailure) {
                    return@withContext Result.failure(
                        sendResult.exceptionOrNull() ?: Exception("Failed to send via Gmail API")
                    )
                }
                sentViaGmail = true
            }

            val now = System.currentTimeMillis()
            val approvedAndSent = draft.copy(
                status = "SENT",
                approvedAt = now,
                sentAt = now
            )
            emailDao.updateDraft(approvedAndSent)
            emailDao.markAsRead(draft.emailId, true)
            Log.d("EmailRepository", "Draft ${draft.id} marked as SENT (sentViaGmail=$sentViaGmail)")
            Result.success(approvedAndSent)
        } catch (e: Exception) {
            Log.e("EmailRepository", "Error sending draft", e)
            Result.failure(e)
        }
    }

    suspend fun fetchEmailById(id: String): EmailMessageEntity? = withContext(Dispatchers.IO) {
        emailDao.getEmailById(id).firstOrNull()
    }

    suspend fun discardDraft(draftId: Long) = withContext(Dispatchers.IO) {
        emailDao.deleteDraft(draftId)
    }

    suspend fun ensureInitialEmailsLoaded(): Unit = withContext(Dispatchers.IO) {
        // No-op: Demo emails removed. Real emails come from Gmail API sync.
        Unit
    }
}
