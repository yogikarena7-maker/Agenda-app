package com.smartagenda.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY scheduledTime ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY scheduledTime ASC")
    suspend fun getAllRemindersList(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE scheduledDate = :date ORDER BY scheduledTime ASC")
    fun getRemindersForDate(date: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE category = :category ORDER BY scheduledTime ASC")
    fun getRemindersByCategory(category: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE scheduledDate = :date AND category = :category ORDER BY scheduledTime ASC")
    fun getRemindersForDateAndCategory(date: String, category: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun getReminderCount(): Int
}
