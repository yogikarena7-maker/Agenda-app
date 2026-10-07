package com.smartagenda.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartagenda.app.util.DateTimeUtils

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val notes: String = "",
    val scheduledDate: String, // Format: YYYY-MM-DD
    val scheduledTime: String, // Format: HH:mm
    val category: String = "Personal",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledEpochMillis: Long = DateTimeUtils.calculateEpochMillis(scheduledDate, scheduledTime)
) {
    val dateString: String get() = scheduledDate
    val timeString: String get() = scheduledTime
}
