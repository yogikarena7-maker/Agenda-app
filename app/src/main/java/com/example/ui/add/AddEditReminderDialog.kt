package com.example.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ReminderEntity
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.util.DateTimeUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditReminderDialog(
    reminder: ReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, notes: String, dateString: String, timeString: String, category: String) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = reminder != null

    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var notes by remember { mutableStateOf(reminder?.notes ?: "") }
    var selectedDate by remember { mutableStateOf(reminder?.dateString ?: DateTimeUtils.getTodayDateString()) }
    var selectedTime by remember { mutableStateOf(reminder?.timeString ?: "09:00") }
    var selectedCategory by remember { mutableStateOf(reminder?.category ?: "General") }
    var titleError by remember { mutableStateOf(false) }

    val todayStr = remember { DateTimeUtils.getTodayDateString() }
    val tomorrowStr = remember { DateTimeUtils.getTomorrowDateString() }

    // Date Picker Dialog setup
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        val parts = selectedDate.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            y,
            m,
            d
        )
    }

    // Time Picker Dialog setup
    val timePickerDialog = remember {
        val parts = selectedTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
            },
            h,
            min,
            false // 12-hour format display with AM/PM
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("add_edit_reminder_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title
                Text(
                    text = if (isEditMode) "Edit Reminder" else "New Reminder",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = SlateDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Reminder Title") },
                    placeholder = { Text("e.g., Team Sync Meeting, Buy Groceries") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text("Title cannot be empty", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Notes Input Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Add any details or instructions") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selection Section
                Text(
                    text = "Scheduled Date",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = SlateMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick "Today" pill
                    DateQuickChip(
                        label = "Today",
                        isSelected = selectedDate == todayStr,
                        onClick = { selectedDate = todayStr }
                    )

                    // Quick "Tomorrow" pill
                    DateQuickChip(
                        label = "Tomorrow",
                        isSelected = selectedDate == tomorrowStr,
                        onClick = { selectedDate = tomorrowStr }
                    )

                    // Custom Date Button
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (selectedDate != todayStr && selectedDate != tomorrowStr) PrimaryIndigo else SlateBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("date_selector_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Pick Date",
                            tint = if (selectedDate != todayStr && selectedDate != tomorrowStr) PrimaryIndigo else SlateMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DateTimeUtils.formatDateDisplay(selectedDate),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = if (selectedDate != todayStr && selectedDate != tomorrowStr) PrimaryIndigo else SlateDark,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Selection Section
                Text(
                    text = "Scheduled Time",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = SlateMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Preset Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeQuickChip(label = "09:00 AM", timeVal = "09:00", current = selectedTime) { selectedTime = it }
                    TimeQuickChip(label = "02:00 PM", timeVal = "14:00", current = selectedTime) { selectedTime = it }
                    TimeQuickChip(label = "06:00 PM", timeVal = "18:00", current = selectedTime) { selectedTime = it }
                    TimeQuickChip(label = "09:00 PM", timeVal = "21:00", current = selectedTime) { selectedTime = it }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Time Button
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("time_selector_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Pick Time",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Custom Time: ${DateTimeUtils.formatTimeDisplay(selectedTime)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = PrimaryIndigo
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Section
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = SlateMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("General", "Work", "Personal", "Urgent")
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryIndigo else SlateBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) Color.White else SlateDark,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("cancel_reminder_button")
                    ) {
                        Text("Cancel", color = SlateMuted)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                            } else {
                                onSave(
                                    reminder?.id ?: 0L,
                                    title,
                                    notes,
                                    selectedDate,
                                    selectedTime,
                                    selectedCategory
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("save_reminder_button")
                    ) {
                        Text(
                            text = if (isEditMode) "Update" else "Save Reminder",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateQuickChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) PrimaryIndigo else SlateBorder),
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(42.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isSelected) Color.White else SlateDark
            )
        }
    }
}

@Composable
private fun TimeQuickChip(
    label: String,
    timeVal: String,
    current: String,
    onSelect: (String) -> Unit
) {
    val isSelected = current == timeVal
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) PrimaryIndigo else SlateBorder),
        modifier = Modifier
            .clickable { onSelect(timeVal) }
            .height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                ),
                color = if (isSelected) Color.White else SlateDark
            )
        }
    }
}
