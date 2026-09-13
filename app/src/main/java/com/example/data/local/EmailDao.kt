package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {
    @Query("SELECT * FROM emails ORDER BY receivedEpochMillis DESC")
    fun getAllEmails(): Flow<List<EmailMessageEntity>>

    @Query("SELECT * FROM emails WHERE id = :id")
    fun getEmailById(id: String): Flow<EmailMessageEntity?>

    @Query("SELECT * FROM emails WHERE category = :category ORDER BY receivedEpochMillis DESC")
    fun getEmailsByCategory(category: String): Flow<List<EmailMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailMessageEntity)

    @Update
    suspend fun updateEmail(email: EmailMessageEntity)

    @Query("UPDATE emails SET isRead = :isRead WHERE id = :id")
    suspend fun markAsRead(id: String, isRead: Boolean)

    @Query("UPDATE emails SET isStarred = :isStarred WHERE id = :id")
    suspend fun markAsStarred(id: String, isStarred: Boolean)

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmail(id: String)

    // Draft queries
    @Query("SELECT * FROM email_drafts WHERE emailId = :emailId ORDER BY createdAt DESC LIMIT 1")
    fun getDraftForEmail(emailId: String): Flow<EmailDraftEntity?>

    @Query("SELECT * FROM email_drafts WHERE id = :draftId")
    suspend fun getDraftById(draftId: Long): EmailDraftEntity?

    @Query("SELECT * FROM email_drafts ORDER BY createdAt DESC")
    fun getAllDrafts(): Flow<List<EmailDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: EmailDraftEntity): Long

    @Update
    suspend fun updateDraft(draft: EmailDraftEntity)

    @Query("DELETE FROM email_drafts WHERE id = :draftId")
    suspend fun deleteDraft(draftId: Long)
}
