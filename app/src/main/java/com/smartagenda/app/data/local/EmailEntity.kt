package com.smartagenda.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class EmailMessageEntity(
    @PrimaryKey
    val id: String,
    val threadId: String = "",
    val senderName: String,
    val senderEmail: String,
    val recipientEmail: String,
    val subject: String,
    val snippet: String,
    val body: String,
    val receivedEpochMillis: Long,
    val category: String, // "Urgent", "Action Required", "Needs Reply", "Informational", "Newsletter"
    val isRead: Boolean = false,
    val isStarred: Boolean = false
)

@Entity(tableName = "email_drafts")
data class EmailDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val emailId: String,
    val recipientEmail: String,
    val recipientName: String,
    val subject: String,
    val draftBody: String,
    val tone: String = "Professional",
    val status: String = "PENDING_APPROVAL", // "PENDING_APPROVAL", "APPROVED", "SENT", "DISCARDED"
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val sentAt: Long? = null
)
