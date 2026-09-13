package com.example

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.NotificationHelper
import com.example.ui.MainAppScreen
import com.example.ui.agenda.AgendaViewModel
import com.example.ui.email.EmailAssistantViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    try {
      // Initialize notification channels on startup safely
      NotificationHelper.createNotificationChannel(applicationContext)
    } catch (e: Exception) {
      Log.e("MainActivity", "Notification channel initialization failed", e)
    }

    setContent {
      MyApplicationTheme {
        val app = LocalContext.current.applicationContext as Application
        val agendaViewModel: AgendaViewModel = viewModel(
            factory = AgendaViewModel.provideFactory(app)
        )
        val emailViewModel: EmailAssistantViewModel = viewModel(
            factory = EmailAssistantViewModel.provideFactory(app)
        )
        MainAppScreen(
            agendaViewModel = agendaViewModel,
            emailViewModel = emailViewModel
        )
      }
    }
  }
}

