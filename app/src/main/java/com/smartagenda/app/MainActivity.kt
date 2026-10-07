package com.smartagenda.app

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartagenda.app.data.local.AppDatabase
import com.smartagenda.app.data.local.ReminderEntity
import com.smartagenda.app.data.repository.EmailRepository
import com.smartagenda.app.data.repository.ReminderRepository
import com.smartagenda.app.notification.NotificationScheduler
import com.smartagenda.app.ui.add.AddEventScreen
import com.smartagenda.app.ui.agenda.AgendaScreen
import com.smartagenda.app.ui.agenda.AgendaViewModel
import com.smartagenda.app.ui.agenda.AgendaViewModelFactory
import com.smartagenda.app.ui.components.AgendaNavView
import com.smartagenda.app.ui.email.EmailAssistantScreen
import com.smartagenda.app.ui.email.EmailAssistantViewModel
import com.smartagenda.app.ui.month.MonthGridScreen
import com.smartagenda.app.ui.settings.SettingsScreen
import com.smartagenda.app.ui.splash.SplashScreen
import com.smartagenda.app.ui.theme.SmartAgendaTheme

enum class ScreenState {
    SPLASH,
    AGENDA,
    MONTH_GRID,
    EMAIL_AI,
    SETTINGS,
    ADD_EVENT
}

class MainActivity : ComponentActivity() {

    private var hasNotificationPermission by mutableStateOf(true)
    private var hasExactAlarmPermission by mutableStateOf(true)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Notification Channel
        NotificationScheduler.createNotificationChannel(applicationContext)

        checkPermissions()

        setContent {
            SmartAgendaTheme {
                SmartAgendaApp(
                    application = application,
                    hasNotificationPermission = hasNotificationPermission,
                    hasExactAlarmPermission = hasExactAlarmPermission,
                    onRequestPermission = { requestNotificationPermission() },
                    onOpenSettings = { openAppSettings() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        // Notification permission check (Android 13+)
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // Exact Alarm permission check (Android 12+)
        hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}

@Composable
fun SmartAgendaApp(
    application: Application,
    hasNotificationPermission: Boolean,
    hasExactAlarmPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val reminderRepository = remember { ReminderRepository(database.reminderDao()) }
    val emailRepository = remember { EmailRepository(database.emailDao()) }

    val agendaViewModel: AgendaViewModel = viewModel(
        factory = AgendaViewModelFactory(application, reminderRepository)
    )

    val emailViewModel: EmailAssistantViewModel = viewModel(
        factory = EmailAssistantViewModel.Factory(emailRepository)
    )

    var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
    var currentNavView by remember { mutableStateOf(AgendaNavView.TIMELINE) }
    var reminderToEdit by remember { mutableStateOf<ReminderEntity?>(null) }
    val selectedDate by agendaViewModel.selectedDate.collectAsState()

    // Hardware Back Button Handler
    BackHandler(enabled = currentScreen != ScreenState.AGENDA && currentScreen != ScreenState.SPLASH) {
        reminderToEdit = null
        currentNavView = AgendaNavView.TIMELINE
        currentScreen = ScreenState.AGENDA
    }

    when (currentScreen) {
        ScreenState.SPLASH -> {
            SplashScreen(
                onSplashFinished = {
                    currentScreen = ScreenState.AGENDA
                }
            )
        }

        ScreenState.AGENDA -> {
            AgendaScreen(
                viewModel = agendaViewModel,
                currentView = currentNavView,
                onViewChange = { navView ->
                    currentNavView = navView
                    when (navView) {
                        AgendaNavView.MONTH_GRID -> currentScreen = ScreenState.MONTH_GRID
                        AgendaNavView.EMAIL_AI -> currentScreen = ScreenState.EMAIL_AI
                        AgendaNavView.SETTINGS -> currentScreen = ScreenState.SETTINGS
                        else -> {}
                    }
                },
                onAddClick = {
                    reminderToEdit = null
                    currentScreen = ScreenState.ADD_EVENT
                },
                onReminderClick = { reminder ->
                    reminderToEdit = reminder
                    currentScreen = ScreenState.ADD_EVENT
                },
                hasNotificationPermission = hasNotificationPermission,
                hasExactAlarmPermission = hasExactAlarmPermission,
                onRequestPermission = onRequestPermission,
                onOpenSettings = onOpenSettings
            )
        }

        ScreenState.MONTH_GRID -> {
            val allReminders by agendaViewModel.allReminders.collectAsState()
            MonthGridScreen(
                selectedDate = selectedDate,
                allReminders = allReminders,
                onDateSelected = { date ->
                    agendaViewModel.selectDate(date)
                    currentNavView = AgendaNavView.TIMELINE
                    currentScreen = ScreenState.AGENDA
                },
                currentView = currentNavView,
                onViewChange = { navView ->
                    currentNavView = navView
                    when (navView) {
                        AgendaNavView.TIMELINE -> currentScreen = ScreenState.AGENDA
                        AgendaNavView.EMAIL_AI -> currentScreen = ScreenState.EMAIL_AI
                        AgendaNavView.SETTINGS -> currentScreen = ScreenState.SETTINGS
                        else -> {}
                    }
                },
                onAddClick = {
                    reminderToEdit = null
                    currentScreen = ScreenState.ADD_EVENT
                }
            )
        }

        ScreenState.EMAIL_AI -> {
            EmailAssistantScreen(
                viewModel = emailViewModel
            )
        }

        ScreenState.SETTINGS -> {
            SettingsScreen(
                onSyncGmailClick = { accessToken ->
                    emailViewModel.syncGmailInbox(accessToken)
                }
            )
        }

        ScreenState.ADD_EVENT -> {
            AddEventScreen(
                reminderToEdit = reminderToEdit,
                onBackClick = {
                    reminderToEdit = null
                    currentScreen = ScreenState.AGENDA
                },
                onSaveClick = { title, notes, date, time, category ->
                    if (reminderToEdit != null && reminderToEdit!!.id != 0L) {
                        // UPDATE existing record in Room DB & update notification
                        val updatedReminder = reminderToEdit!!.copy(
                            title = title.trim(),
                            notes = notes.trim(),
                            scheduledDate = normalizeDate(date),
                            scheduledTime = normalizeTime(time),
                            category = category
                        )
                        agendaViewModel.updateReminder(updatedReminder)
                    } else {
                        // CREATE new record in Room DB & schedule notification
                        agendaViewModel.addReminder(
                            title = title.trim(),
                            notes = notes.trim(),
                            scheduledDateStr = normalizeDate(date),
                            scheduledTimeStr = normalizeTime(time),
                            category = category
                        )
                    }
                    reminderToEdit = null
                    currentScreen = ScreenState.AGENDA
                },
                onDeleteClick = { reminder ->
                    // DELETE record from Room DB & cancel notification
                    agendaViewModel.deleteReminder(reminder)
                    reminderToEdit = null
                    currentScreen = ScreenState.AGENDA
                }
            )
        }
    }
}

private fun normalizeDate(input: String): String {
    val clean = input.trim()
    val parts = clean.split("-")
    if (parts.size == 3) {
        val y = parts[0]
        val m = parts[1].padStart(2, '0')
        val d = parts[2].padStart(2, '0')
        return "$y-$m-$d"
    }
    return clean
}

private fun normalizeTime(input: String): String {
    val clean = input.trim()
    val parts = clean.split(":")
    if (parts.size == 2) {
        val h = parts[0].padStart(2, '0')
        val m = parts[1].padStart(2, '0')
        return "$h:$m"
    }
    return clean
}
