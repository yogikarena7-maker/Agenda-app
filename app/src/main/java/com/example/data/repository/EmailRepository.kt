package com.example.data.repository

import com.example.ai.GeminiEmailAssistant
import com.example.data.local.EmailDao
import com.example.data.local.EmailDraftEntity
import com.example.data.local.EmailMessageEntity
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

    suspend fun approveAndSendDraft(draft: EmailDraftEntity): EmailDraftEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val approvedAndSent = draft.copy(
            status = "SENT",
            approvedAt = now,
            sentAt = now
        )
        emailDao.updateDraft(approvedAndSent)
        // Also mark original email as read and processed
        emailDao.markAsRead(draft.emailId, true)
        approvedAndSent
    }

    suspend fun discardDraft(draftId: Long) = withContext(Dispatchers.IO) {
        emailDao.deleteDraft(draftId)
    }

    suspend fun ensureInitialEmailsLoaded() = withContext(Dispatchers.IO) {
        val existing = emailDao.getAllEmails().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val sampleEmails = listOf(
                EmailMessageEntity(
                    id = "msg_001",
                    threadId = "th_001",
                    senderName = "Marcus Vance",
                    senderEmail = "marcus.vance@techcorp.io",
                    recipientEmail = "me@smartagenda.local",
                    subject = "Q3 Product Architecture Review & Milestone Signoff",
                    snippet = "Could you please review the attached architecture blueprint and confirm your approval by 4 PM tomorrow?",
                    body = """
                        Hi Team,
                        
                        We have finalized the system blueprint for the upcoming Phase 2 and Phase 3 deployments. 
                        
                        Could you please review the attached architecture blueprint and confirm your approval by 4 PM tomorrow? We need to lock the sprint dependencies before Thursday's leadership sync.
                        
                        Key items for review:
                        1. Local Room DB schema and offline-first persistence
                        2. Gemini AI integration latency thresholds
                        3. Strict human approval workflows for email communications
                        
                        Looking forward to your thoughts.
                        
                        Best regards,
                        Marcus Vance
                        VP of Product Engineering
                    """.trimIndent(),
                    receivedEpochMillis = now - (1000 * 60 * 45), // 45 mins ago
                    category = "Action Required",
                    isRead = false,
                    isStarred = true
                ),
                EmailMessageEntity(
                    id = "msg_002",
                    threadId = "th_002",
                    senderName = "Dr. Elena Rostova",
                    senderEmail = "elena.rostova@healthclinic.org",
                    recipientEmail = "me@smartagenda.local",
                    subject = "Appointment Confirmation: Friday at 2:30 PM",
                    snippet = "Please confirm if this time works or reply with your preferred alternative slot.",
                    body = """
                        Hello,
                        
                        This is a courtesy reminder regarding your upcoming health checkup scheduled for this Friday at 2:30 PM with Dr. Rostova.
                        
                        Please confirm if this time works for you or reply with your preferred alternative slot. 
                        
                        Kindly bring your photo ID and updated insurance card.
                        
                        Warm regards,
                        Clinic Reception
                    """.trimIndent(),
                    receivedEpochMillis = now - (1000 * 60 * 180), // 3 hours ago
                    category = "Needs Reply",
                    isRead = false,
                    isStarred = false
                ),
                EmailMessageEntity(
                    id = "msg_003",
                    threadId = "th_003",
                    senderName = "Cloud Operations Alert",
                    senderEmail = "alerts@cloudplatform.io",
                    recipientEmail = "me@smartagenda.local",
                    subject = "[URGENT] API Gateway Rate Limit Warning (92% threshold reached)",
                    snippet = "Production API cluster rate limit threshold exceeded. Immediate action required to avoid throttling.",
                    body = """
                        [ALERT NOTIFICATION]
                        Severity: HIGH
                        Timestamp: Just now
                        
                        Your application API gateway has hit 92% of the quota limit for the current billing cycle. 
                        
                        Recommended immediate action:
                        1. Review background pollers and ensure batching.
                        2. Verify caching layers on high-frequency endpoints.
                        3. Upgrade tier if traffic expansion is deliberate.
                        
                        Engineering Team
                    """.trimIndent(),
                    receivedEpochMillis = now - (1000 * 60 * 20), // 20 mins ago
                    category = "Urgent",
                    isRead = false,
                    isStarred = true
                ),
                EmailMessageEntity(
                    id = "msg_004",
                    threadId = "th_004",
                    senderName = "Android Weekly Digest",
                    senderEmail = "newsletter@androidweekly.net",
                    recipientEmail = "me@smartagenda.local",
                    subject = "Android Weekly #645: Compose Multiplatform, Room 2.7 & Gemini on Device",
                    snippet = "Catch up on this week's top Kotlin and Jetpack Compose articles, tutorials, and libraries.",
                    body = """
                        Welcome to Android Weekly issue #645!
                        
                        Featured articles this week:
                        - Building Offline-First Reactive UIs with Jetpack Compose & Room
                        - Production Guide: Integrating Gemini 3.5 Flash via REST in Android
                        - Designing Accessible Material 3 Components
                        
                        Enjoy reading, and happy coding!
                    """.trimIndent(),
                    receivedEpochMillis = now - (1000 * 60 * 60 * 24), // 1 day ago
                    category = "Newsletter",
                    isRead = true,
                    isStarred = false
                )
            )
            emailDao.insertEmails(sampleEmails)
        }
    }
}
