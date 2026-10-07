package com.smartagenda.app.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.app.data.local.ReminderEntity
import com.smartagenda.app.ui.theme.BrandBorderDim
import com.smartagenda.app.ui.theme.BrandDark
import com.smartagenda.app.ui.theme.BrandDarkSurface
import com.smartagenda.app.ui.theme.BrandLimeAccent
import com.smartagenda.app.ui.theme.BrandLimeVibrant
import com.smartagenda.app.ui.theme.BrandMintCard
import com.smartagenda.app.ui.theme.BrandTextMuted
import com.smartagenda.app.ui.theme.OnLimeBlack
import com.smartagenda.app.ui.theme.OnSurfaceWhite
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimelineView(
    reminders: List<ReminderEntity>,
    onToggleComplete: (ReminderEntity) -> Unit = {},
    onDelete: (ReminderEntity) -> Unit = {},
    onReminderClick: (ReminderEntity) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val startHour = 6
    val endHour = 23
    val slotHeightDp = 76
    val hours = (startHour..endHour).map { String.format("%02d:00", it) }

    val now = LocalTime.now()
    val currentTimeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"))

    val currentHour = now.hour
    val currentMinute = now.minute
    val needleTopOffset = if (currentHour in startHour..endHour) {
        ((currentHour - startHour) * slotHeightDp + (currentMinute * slotHeightDp / 60)).dp
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Grid Hour Lines
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            hours.forEach { hour ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(slotHeightDp.dp)
                ) {
                    HorizontalDivider(
                        color = BrandBorderDim.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                    Text(
                        text = hour,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandTextMuted,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(84.dp))
        }

        // Empty State (if no reminders scheduled for selected date)
        if (reminders.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = 100.dp, start = 48.dp, end = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrandDarkSurface.copy(alpha = 0.7f))
                    .border(1.dp, BrandBorderDim.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = "No Reminders",
                    tint = BrandLimeAccent,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No Reminders Scheduled",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your day is clear.\nTap + or use AI Quick Add to schedule a reminder.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = BrandTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // Render Room Reminders as Timeline Cards
        reminders.forEach { reminder ->
            val timeParts = reminder.scheduledTime.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

            val clampedHour = hour.coerceIn(startHour, endHour)
            val topOffset = ((clampedHour - startHour) * slotHeightDp + (minute * slotHeightDp / 60)).dp

            val isLimeTheme = reminder.category == "Work"
            val cardBg = if (reminder.isCompleted) {
                BrandBorderDim.copy(alpha = 0.6f)
            } else if (isLimeTheme) {
                BrandLimeVibrant
            } else {
                BrandMintCard
            }

            val contentColor = if (reminder.isCompleted) {
                OnSurfaceWhite.copy(alpha = 0.6f)
            } else {
                OnLimeBlack
            }

            androidx.compose.material3.SwipeToDismissBox(
                state = androidx.compose.material3.rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                            onDelete(reminder)
                            true
                        } else false
                    }
                ),
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .padding(top = topOffset, start = 48.dp, end = 8.dp)
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .clip(RoundedCornerShape(18.dp))
                            .background(androidx.compose.ui.graphics.Color(0xFFE53935)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                modifier = Modifier.padding(top = topOffset, start = 48.dp, end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(cardBg)
                        .clickable { onReminderClick(reminder) }
                        .padding(14.dp)
                ) {
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox Circle for Toggling Complete State (Accessible 44dp target)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    onToggleComplete(reminder) 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (reminder.isCompleted) BrandLimeAccent else contentColor.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (reminder.isCompleted) BrandLimeAccent else contentColor.copy(alpha = 0.4f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = reminder.isCompleted,
                                    enter = androidx.compose.animation.scaleIn(),
                                    exit = androidx.compose.animation.scaleOut()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = OnLimeBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
    
                        Spacer(modifier = Modifier.width(10.dp))
    
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = reminder.scheduledTime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(contentColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = reminder.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor
                                    )
                                }
                            }
    
                            Spacer(modifier = Modifier.height(3.dp))
    
                            Text(
                                text = reminder.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
    
                            if (reminder.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = reminder.notes,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = contentColor.copy(alpha = 0.8f),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Current Time Needle Line (if within 6:00-23:00 window)
        needleTopOffset?.let { offset ->
            Row(
                modifier = Modifier
                    .padding(top = offset)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandDark)
                        .border(1.dp, BrandLimeAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentTimeStr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandLimeAccent
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(BrandLimeAccent, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(BrandLimeAccent)
                )
            }
        }
    }
}
