package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    private val time24Format = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    private val time12Format = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    private val headerDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    private val displayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    fun getTodayDateString(): String {
        return isoDateFormat.format(Date())
    }

    fun getTomorrowDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return isoDateFormat.format(calendar.time)
    }

    fun getCurrentTimeString(): String {
        return time24Format.format(Date())
    }

    fun formatHeaderDate(): String {
        return headerDateFormat.format(Date())
    }

    fun formatDateDisplay(dateString: String): String {
        return try {
            val today = getTodayDateString()
            val tomorrow = getTomorrowDateString()

            when (dateString) {
                today -> "Today"
                tomorrow -> "Tomorrow"
                else -> {
                    val parsed = isoDateFormat.parse(dateString)
                    if (parsed != null) displayDateFormat.format(parsed) else dateString
                }
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatTimeDisplay(timeString: String): String {
        return try {
            val parsed = time24Format.parse(timeString)
            if (parsed != null) time12Format.format(parsed) else timeString
        } catch (e: Exception) {
            timeString
        }
    }

    fun calculateEpochMillis(dateString: String, timeString: String): Long {
        return try {
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            val date = dateTimeFormat.parse("$dateString $timeString")
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun isToday(dateString: String): Boolean {
        return dateString == getTodayDateString()
    }

    fun isOverdue(epochMillis: Long): Boolean {
        return epochMillis < System.currentTimeMillis()
    }

    fun formatRelativeTime(epochMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - epochMillis
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            diff < 0 -> "Just now"
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> {
                val format = SimpleDateFormat("MMM d", Locale.getDefault())
                format.format(Date(epochMillis))
            }
        }
    }
}
