package com.smartagenda.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(Date())
    }

    fun getCurrentTimeString(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        return timeFormat.format(Date())
    }

    fun formatDisplayDate(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateString) ?: return dateString
            val cal = Calendar.getInstance().apply { time = date }

            val today = Calendar.getInstance()
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

            when {
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
                cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) -> "Tomorrow"
                else -> SimpleDateFormat("EEE, MMM d", Locale.US).format(date)
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDisplayTime(timeString: String): String {
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
            val date = sdf24.parse(timeString) ?: return timeString
            SimpleDateFormat("h:mm a", Locale.US).format(date)
        } catch (e: Exception) {
            timeString
        }
    }

    fun calculateEpochMillis(dateString: String, timeString: String): Long {
        return try {
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val date = dateTimeFormat.parse("$dateString $timeString")
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
