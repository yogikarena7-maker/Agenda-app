package com.example.data.repository

import com.example.data.local.ReminderDao
import com.example.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun getReminderById(id: Long): ReminderEntity? = reminderDao.getReminderById(id)

    suspend fun getPendingFutureReminders(currentTime: Long = System.currentTimeMillis()): List<ReminderEntity> =
        reminderDao.getPendingFutureReminders(currentTime)

    suspend fun insertReminder(reminder: ReminderEntity): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)

    suspend fun deleteReminderById(id: Long) = reminderDao.deleteReminderById(id)

    suspend fun setCompleted(id: Long, completed: Boolean) = reminderDao.updateCompletedStatus(id, completed)
}
