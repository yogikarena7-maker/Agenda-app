package com.smartagenda.app.ui.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.app.data.local.ReminderEntity
import com.smartagenda.app.ui.components.AgendaNavView
import com.smartagenda.app.ui.components.AiNaturalLanguageQuickAddBar
import com.smartagenda.app.ui.components.FilterChipRow
import com.smartagenda.app.ui.components.FloatingNavPill
import com.smartagenda.app.ui.components.HeaderBar
import com.smartagenda.app.ui.components.HorizontalDatePicker
import com.smartagenda.app.ui.components.TimelineView
import com.smartagenda.app.ui.theme.BrandDark
import com.smartagenda.app.ui.theme.OnSurfaceWhite
import java.time.format.DateTimeFormatter

@Composable
fun AgendaScreen(
    viewModel: AgendaViewModel,
    currentView: AgendaNavView = AgendaNavView.TIMELINE,
    onViewChange: (AgendaNavView) -> Unit = {},
    onAddClick: () -> Unit = {},
    onReminderClick: (ReminderEntity) -> Unit = {},
    hasNotificationPermission: Boolean = true,
    hasExactAlarmPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val reminders by viewModel.remindersForSelectedDate.collectAsState()
    val isQuickAddParsing by viewModel.isQuickAddParsing.collectAsState()
    val quickAddPreview by viewModel.quickAddPreview.collectAsState()

    val quickAddText by viewModel.quickAddText.collectAsState()
    val quickAddError by viewModel.quickAddError.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }

    val monthTitle = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dynamic Header Bar ("September 2026", profile pill, action icons, search)
            HeaderBar(
                monthTitle = monthTitle,
                searchQuery = searchQuery,
                onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                isSearchActive = isSearchActive,
                onSearchToggle = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) {
                        viewModel.updateSearchQuery("")
                    }
                },
                onCalendarClick = {
                    onViewChange(AgendaNavView.MONTH_GRID)
                },
                onProfileClick = {
                    onViewChange(AgendaNavView.SETTINGS)
                }
            )

            // Permission Warning Banner (if notifications or exact alarms are disabled)
            if (!hasNotificationPermission || !hasExactAlarmPermission) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2200))
                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifications Disabled",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFFFB300)
                        )
                        Text(
                            text = "Enable notification permission to receive alerts for scheduled reminders.",
                            fontSize = 10.sp,
                            color = OnSurfaceWhite.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!hasNotificationPermission) onRequestPermission() else onOpenSettings()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 0.dp)
                    ) {
                        Text(text = "Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // AI Natural Language Quick Add Bar
            AiNaturalLanguageQuickAddBar(
                text = quickAddText,
                onTextChange = { viewModel.updateQuickAddText(it) },
                onParseRequest = { viewModel.parseQuickAddText() },
                onConfirm = { viewModel.confirmQuickAdd() },
                onCancel = { viewModel.cancelQuickAdd() },
                onEdit = { 
                    val preview = quickAddPreview
                    if (preview != null) {
                        val tempReminder = com.smartagenda.app.data.local.ReminderEntity(
                            id = 0,
                            title = preview.title,
                            notes = preview.notes,
                            scheduledDate = preview.dateString,
                            scheduledTime = preview.timeString,
                            category = preview.category,
                            isCompleted = false
                        )
                        viewModel.cancelQuickAdd()
                        onReminderClick(tempReminder)
                    }
                },
                isParsing = isQuickAddParsing,
                parsedPreview = quickAddPreview,
                errorMessage = quickAddError,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Category Filter Chips
            FilterChipRow(
                categories = viewModel.categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { cat -> viewModel.selectCategory(cat) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Horizontal Weekly Date Selector
            HorizontalDatePicker(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    viewModel.selectDate(date)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Timeline Schedule (Hour slots, live needle, Room cards)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                TimelineView(
                    reminders = reminders,
                    onToggleComplete = { reminder ->
                        viewModel.toggleReminderCompleted(reminder)
                    },
                    onDelete = { reminder ->
                        viewModel.deleteReminder(reminder)
                    },
                    onReminderClick = onReminderClick
                )
            }
        }

        // Bottom Floating Navigation Capsule
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            FloatingNavPill(
                currentView = currentView,
                onViewChange = onViewChange,
                onAddClick = onAddClick
            )
        }
    }
}
