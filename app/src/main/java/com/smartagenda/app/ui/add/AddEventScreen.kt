package com.smartagenda.app.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.smartagenda.app.ui.components.ZendaDatePicker
import com.smartagenda.app.ui.components.ZendaTimePicker
import com.smartagenda.app.ui.theme.BrandBorderDim
import com.smartagenda.app.ui.theme.BrandDark
import com.smartagenda.app.ui.theme.BrandDarkElevated
import com.smartagenda.app.ui.theme.BrandDarkSurface
import com.smartagenda.app.ui.theme.BrandLimeAccent
import com.smartagenda.app.ui.theme.BrandTextMuted
import com.smartagenda.app.ui.theme.OnLimeBlack
import com.smartagenda.app.ui.theme.OnSurfaceWhite
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddEventScreen(
    reminderToEdit: ReminderEntity? = null,
    onBackClick: () -> Unit = {},
    onSaveClick: (title: String, notes: String, date: String, time: String, category: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteClick: (ReminderEntity) -> Unit = {}
) {
    val defaultDateStr = remember(reminderToEdit) {
        reminderToEdit?.scheduledDate ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
    val defaultTimeStr = remember(reminderToEdit) {
        reminderToEdit?.scheduledTime ?: LocalTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("HH:00"))
    }

    var title by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.title ?: "") }
    var date by remember(reminderToEdit) { mutableStateOf(defaultDateStr) }
    var time by remember(reminderToEdit) { mutableStateOf(defaultTimeStr) }
    var notes by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.notes ?: "") }
    var category by remember(reminderToEdit) { mutableStateOf(reminderToEdit?.category ?: "Personal") }

    var isSaving by remember { mutableStateOf(false) }
    var showTitleError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val categories = listOf("Personal", "Work", "Health", "Study", "Finance")
    val isEditMode = reminderToEdit != null && reminderToEdit.id != 0L

    val handleSave = {
        if (title.isBlank()) {
            showTitleError = true
        } else if (!isSaving) {
            isSaving = true
            onSaveClick(title.trim(), notes.trim(), date, time, category)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnSurfaceWhite
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isEditMode) "Edit Reminder" else "New Reminder",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceWhite
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { handleSave() },
                    enabled = !isSaving
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = if (title.isNotBlank() && !isSaving) BrandLimeAccent else BrandTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Container Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BrandDarkSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "TITLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showTitleError && title.isBlank()) Color(0xFFFF5252) else BrandTextMuted
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) showTitleError = false
                    },
                    placeholder = { Text("Reminder title...", color = BrandTextMuted) },
                    singleLine = true,
                    isError = showTitleError && title.isBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BrandDarkElevated,
                        unfocusedContainerColor = BrandDarkElevated,
                        focusedBorderColor = BrandLimeAccent,
                        unfocusedBorderColor = if (showTitleError && title.isBlank()) Color(0xFFFF5252) else BrandBorderDim,
                        focusedTextColor = OnSurfaceWhite,
                        unfocusedTextColor = OnSurfaceWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
                if (showTitleError && title.isBlank()) {
                    Text(
                        text = "Title is required",
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date & Time Custom Interactive Cards
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Date Field Card
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTextMuted
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandDarkElevated)
                                .border(1.dp, BrandBorderDim, RoundedCornerShape(12.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = BrandLimeAccent,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = date,
                                    color = OnSurfaceWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Time Field Card
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTextMuted
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandDarkElevated)
                                .border(1.dp, BrandBorderDim, RoundedCornerShape(12.dp))
                                .clickable { showTimePicker = true }
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Select Time",
                                    tint = BrandLimeAccent,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = time,
                                    color = OnSurfaceWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Chips Selector
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandTextMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == category
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandLimeAccent else BrandDarkElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) BrandLimeAccent else BrandBorderDim,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { category = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) OnLimeBlack else OnSurfaceWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "NOTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandTextMuted
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Optional notes...", color = BrandTextMuted) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BrandDarkElevated,
                        unfocusedContainerColor = BrandDarkElevated,
                        focusedBorderColor = BrandLimeAccent,
                        unfocusedBorderColor = BrandBorderDim,
                        focusedTextColor = OnSurfaceWhite,
                        unfocusedTextColor = OnSurfaceWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Action Button (Save / Update)
                Button(
                    onClick = { handleSave() },
                    enabled = title.isNotBlank() && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandLimeAccent,
                        contentColor = OnLimeBlack,
                        disabledContainerColor = BrandLimeAccent.copy(alpha = 0.4f),
                        disabledContentColor = OnLimeBlack.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isEditMode) "Update Reminder" else "Save Reminder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Delete Button (only in Edit mode for saved reminders)
                if (isEditMode && reminderToEdit != null && reminderToEdit.id != 0L) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onDeleteClick(reminderToEdit) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Delete Reminder",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Custom Zenda Date Picker Dialog
    if (showDatePicker) {
        ZendaDatePicker(
            initialDateStr = date,
            onDateSelected = { selected ->
                date = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Custom Zenda Time Picker Dialog
    if (showTimePicker) {
        ZendaTimePicker(
            initialTimeStr = time,
            onTimeSelected = { selected ->
                time = selected
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
