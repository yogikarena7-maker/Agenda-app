package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d(TAG, "Device booted, restoring active reminders...")
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val futureReminders = db.reminderDao().getPendingFutureReminders(System.currentTimeMillis())
                    for (reminder in futureReminders) {
                        ReminderScheduler.schedule(context, reminder)
                    }
                    Log.d(TAG, "Successfully restored ${futureReminders.size} reminders after boot.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore reminders on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
