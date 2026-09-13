package com.example.ui.agenda

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ReminderEntity
import com.example.ui.add.AddEditReminderDialog
import com.example.ui.components.FilterChipRow
import com.example.ui.components.ReminderCard
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SuccessGreen
import com.example.util.DateTimeUtils

@Composable
fun AgendaScreen(
    viewModel: AgendaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var reminderToDelete by remember { mutableStateOf<ReminderEntity?>(null) }
    var showPermissionBanner by remember { mutableStateOf(false) }

    // Notification Permission Request for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        showPermissionBanner = !isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            showPermissionBanner = !hasPermission
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = PrimaryIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .size(60.dp)
                    .testTag("add_reminder_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Reminder",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Section
            AgendaHeader(
                todayDate = DateTimeUtils.formatHeaderDate(),
                totalTasks = uiState.totalCount,
                completedTasks = uiState.completedCount
            )

            // Phase 2: Natural Language AI Reminder Input
            com.example.ui.components.AiNaturalLanguageQuickAddBar(
                isParsing = uiState.isParsingAi,
                parsedReminder = uiState.parsedAiReminder,
                onParseText = { viewModel.parseNaturalLanguage(it) },
                onConfirmParsed = { viewModel.confirmParsedAiReminder(it) },
                onDismissParsed = { viewModel.dismissParsedAi() }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Notification Permission Banner if not granted
            AnimatedVisibility(
                visible = showPermissionBanner,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NotificationPermissionCard(
                    onEnableClicked = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onDismiss = { showPermissionBanner = false }
                )
            }

            // Filter Chips
            FilterChipRow(
                selectedFilter = uiState.selectedFilter,
                onSelectFilter = { viewModel.setFilter(it) },
                totalCount = uiState.totalCount,
                todayCount = uiState.todayCount,
                completedCount = uiState.completedCount,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reminders List or Empty State
            if (uiState.filteredReminders.isEmpty()) {
                EmptyAgendaState(
                    filter = uiState.selectedFilter,
                    onAddClick = { viewModel.openAddDialog() },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("reminders_list"),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 88.dp // Space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.filteredReminders,
                        key = { it.id }
                    ) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggleComplete = { viewModel.toggleComplete(reminder) },
                            onEdit = { viewModel.openEditDialog(reminder) },
                            onDelete = { reminderToDelete = reminder }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Reminder Dialog
    if (uiState.isAddEditDialogOpen) {
        AddEditReminderDialog(
            reminder = uiState.editingReminder,
            onDismiss = { viewModel.closeDialog() },
            onSave = { id, title, notes, date, time, category ->
                viewModel.saveReminder(id, title, notes, date, time, category)
            }
        )
    }

    // Delete Confirmation Dialog
    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = {
                Text(
                    text = "Delete Reminder?",
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${reminder.title}\"? Scheduled notifications for this reminder will be cancelled.",
                    color = SlateMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReminder(reminder)
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { reminderToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text("Cancel", color = SlateMuted)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun AgendaHeader(
    todayDate: String,
    totalTasks: Int,
    completedTasks: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = todayDate,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = SlateMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Today\'s Agenda",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    ),
                    color = SlateDark
                )
            }

            // Quick App Logo / Identity Pill
            Surface(
                shape = CircleShape,
                color = PrimaryIndigoContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.EventNote,
                        contentDescription = "Smart Agenda",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SlateBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (totalTasks > 0 && completedTasks == totalTasks) SuccessGreen else PrimaryIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (totalTasks == 0) "No tasks for today"
                            else if (completedTasks == totalTasks) "All tasks completed!"
                            else "$completedTasks of $totalTasks completed",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = SlateDark
                        )
                    }

                    if (totalTasks > 0) {
                        val progressPercent = (completedTasks.toFloat() / totalTasks * 100).toInt()
                        Text(
                            text = "$progressPercent%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = PrimaryIndigo
                        )
                    }
                }

                if (totalTasks > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val progress = completedTasks.toFloat() / totalTasks
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (completedTasks == totalTasks) SuccessGreen else PrimaryIndigo,
                        trackColor = SlateBorder.copy(alpha = 0.6f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    onEnableClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryIndigoContainer),
        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notification alert",
                tint = PrimaryIndigo,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable Notifications",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = SlateDark
                )
                Text(
                    text = "Allow reminders to alert you at your scheduled times.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = SlateMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onEnableClicked,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("enable_notifications_button")
            ) {
                Text(
                    text = "Enable",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun EmptyAgendaState(
    filter: AgendaFilter,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Illustration
            Surface(
                shape = CircleShape,
                color = PrimaryIndigoContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (filter) {
                            AgendaFilter.COMPLETED -> Icons.Outlined.CheckCircle
                            else -> Icons.AutoMirrored.Outlined.EventNote
                        },
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = when (filter) {
                    AgendaFilter.COMPLETED -> "No completed items"
                    AgendaFilter.TODAY -> "No items for today"
                    AgendaFilter.UPCOMING -> "No upcoming reminders"
                    AgendaFilter.ALL -> "No agenda items yet"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = SlateDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (filter) {
                    AgendaFilter.COMPLETED -> "Check off reminders as you finish them to see your completed history here."
                    AgendaFilter.TODAY -> "You're all clear for today! Tap the button below to add a reminder."
                    AgendaFilter.UPCOMING -> "Plan ahead by adding reminders for tomorrow or later this week."
                    AgendaFilter.ALL -> "Tap the + button below to plan your day or set a time-sensitive reminder."
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = SlateMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("empty_state_add_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Reminder",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
