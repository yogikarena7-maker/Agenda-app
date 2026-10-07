package com.smartagenda.app.data.repository

import com.smartagenda.app.ai.NaturalLanguageReminderParser
import com.smartagenda.app.ai.ParsedReminder
import com.smartagenda.app.data.local.ReminderDao
import com.smartagenda.app.data.local.ReminderEntity
import com.smartagenda.app.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllReminders(): Flow<List<ReminderEntity>> {
        return reminderDao.getAllReminders()
    }

    suspend fun getAllRemindersList(): List<ReminderEntity> {
        return reminderDao.getAllRemindersList()
    }

    fun getRemindersForDate(dateStr: String): Flow<List<ReminderEntity>> {
        return reminderDao.getRemindersForDate(dateStr)
    }

    fun getRemindersByCategory(category: String): Flow<List<ReminderEntity>> {
        return reminderDao.getRemindersByCategory(category)
    }

    fun getRemindersForDateAndCategory(dateStr: String, category: String): Flow<List<ReminderEntity>> {
        return reminderDao.getRemindersForDateAndCategory(dateStr, category)
    }

    suspend fun parseNaturalLanguageReminder(input: String): ParsedReminder {
        return NaturalLanguageReminderParser.parse(input)
    }

    suspend fun addQuickReminderFromText(input: String): ReminderEntity {
        val parsed = NaturalLanguageReminderParser.parse(input)
        val reminder = ReminderEntity(
            title = parsed.title,
            notes = parsed.notes,
            scheduledDate = parsed.dateString,
            scheduledTime = parsed.timeString,
            category = parsed.category,
            isCompleted = false,
            scheduledEpochMillis = DateTimeUtils.calculateEpochMillis(parsed.dateString, parsed.timeString)
        )
        val id = reminderDao.insertReminder(reminder)
        return reminder.copy(id = id)
    }

    suspend fun insertReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    suspend fun getReminderCount(): Int {
        return reminderDao.getReminderCount()
    }
}
