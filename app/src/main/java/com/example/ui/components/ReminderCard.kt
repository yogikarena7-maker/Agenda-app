package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReminderEntity
import com.example.ui.theme.GeneralBg
import com.example.ui.theme.GeneralColor
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.PersonalBg
import com.example.ui.theme.PersonalColor
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.UrgentBg
import com.example.ui.theme.UrgentColor
import com.example.ui.theme.WorkBg
import com.example.ui.theme.WorkColor
import com.example.util.DateTimeUtils

@Composable
fun ReminderCard(
    reminder: ReminderEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = !reminder.isCompleted && DateTimeUtils.isOverdue(reminder.scheduledEpochMillis)

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            reminder.isCompleted -> SlateBorder.copy(alpha = 0.5f)
            isOverdue -> OverdueRed.copy(alpha = 0.3f)
            else -> SlateBorder
        },
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reminder_card_${reminder.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (reminder.isCompleted) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Circular Completion Checkbox
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onToggleComplete)
                        .testTag("complete_checkbox_${reminder.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (reminder.isCompleted) SuccessGreen
                                else Color.Transparent
                            )
                            .then(
                                if (!reminder.isCompleted) {
                                    Modifier.background(
                                        color = Color.Transparent,
                                        shape = CircleShape
                                    )
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(26.dp),
                            shape = CircleShape,
                            color = if (reminder.isCompleted) SuccessGreen else Color.Transparent,
                            border = if (!reminder.isCompleted) BorderStroke(2.dp, PrimaryIndigo) else null
                        ) {
                            if (reminder.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Title and notes
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (reminder.isCompleted) SlateMuted else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (reminder.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reminder.notes,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = SlateMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Actions: Edit and Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("edit_button_${reminder.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit reminder",
                            tint = SlateMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("delete_button_${reminder.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete reminder",
                            tint = SlateMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips Row: Date/Time badge, Category pill, Overdue indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Time & Date Chip
                val dateDisplay = DateTimeUtils.formatDateDisplay(reminder.dateString)
                val timeDisplay = DateTimeUtils.formatTimeDisplay(reminder.timeString)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        reminder.isCompleted -> MaterialTheme.colorScheme.surfaceVariant
                        isOverdue -> OverdueRed.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    },
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = when {
                            reminder.isCompleted -> Color.Transparent
                            isOverdue -> OverdueRed.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (dateDisplay == "Today") Icons.Outlined.AccessTime else Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = when {
                                reminder.isCompleted -> SlateMuted
                                isOverdue -> OverdueRed
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (dateDisplay == "Today") timeDisplay else "$dateDisplay, $timeDisplay",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = when {
                                reminder.isCompleted -> SlateMuted
                                isOverdue -> OverdueRed
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }

                // Category Chip
                val (catBg, catColor) = when (reminder.category) {
                    "Work" -> WorkBg to WorkColor
                    "Personal" -> PersonalBg to PersonalColor
                    "Urgent" -> UrgentBg to UrgentColor
                    else -> GeneralBg to GeneralColor
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = catBg
                ) {
                    Text(
                        text = reminder.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = catColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (isOverdue) {
                    Text(
                        text = "Overdue",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = OverdueRed
                    )
                }
            }
        }
    }
}
