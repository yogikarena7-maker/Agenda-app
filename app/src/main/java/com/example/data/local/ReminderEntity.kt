package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val notes: String = "",
    val dateString: String, // ISO format: yyyy-MM-dd
    val timeString: String, // 24h format: HH:mm
    val scheduledEpochMillis: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String = "General"
)
