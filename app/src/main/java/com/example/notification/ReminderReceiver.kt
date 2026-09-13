package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_NOTES = "notes"
        const val EXTRA_TIME = "time"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Agenda Reminder"
        val notes = intent.getStringExtra(EXTRA_NOTES) ?: ""
        val time = intent.getStringExtra(EXTRA_TIME) ?: ""

        if (reminderId != 0L) {
            NotificationHelper.showReminderNotification(
                context = context,
                reminderId = reminderId,
                title = title,
                notes = notes,
                timeFormatted = time
            )
        }
    }
}
