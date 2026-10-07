package com.smartagenda.app.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.app.data.local.ReminderEntity
import com.smartagenda.app.ui.components.AgendaNavView
import com.smartagenda.app.ui.components.FloatingNavPill
import com.smartagenda.app.ui.components.HeaderBar
import com.smartagenda.app.ui.theme.BrandBorderDim
import com.smartagenda.app.ui.theme.BrandDark
import com.smartagenda.app.ui.theme.BrandDarkSurface
import com.smartagenda.app.ui.theme.BrandLimeAccent
import com.smartagenda.app.ui.theme.BrandTextMuted
import com.smartagenda.app.ui.theme.OnLimeBlack
import com.smartagenda.app.ui.theme.OnSurfaceWhite
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthGridScreen(
    selectedDate: LocalDate = LocalDate.now(),
    allReminders: List<ReminderEntity> = emptyList(),
    onDateSelected: (LocalDate) -> Unit = {},
    currentView: AgendaNavView = AgendaNavView.MONTH_GRID,
    onViewChange: (AgendaNavView) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val monthTitle = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")

    val yearMonth = YearMonth.of(selectedDate.year, selectedDate.month)
    val firstOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()
    
    // Calculate Monday-based day of week offset for day 1 (Monday = 0, Sunday = 6)
    val startOffset = firstOfMonth.dayOfWeek.value - 1

    // Group reminders by date string "yyyy-MM-dd"
    val remindersByDate = remember(allReminders) {
        allReminders.groupBy { it.scheduledDate }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderBar(monthTitle = monthTitle)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BrandDarkSurface)
                    .padding(16.dp)
            ) {
                // Day names header
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Render dynamic month grid rows
                var currentDayNum = 1 - startOffset

                for (week in 0..5) {
                    if (currentDayNum > daysInMonth) break

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        for (dayOfWeek in 0..6) {
                            if (currentDayNum in 1..daysInMonth) {
                                val dayNum = currentDayNum
                                val dateObj = yearMonth.atDay(dayNum)
                                val dateStr = dateObj.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val isSelectedDay = dateObj.isEqual(selectedDate)
                                val isToday = dateObj.isEqual(today)
                                val hasReminders = remindersByDate.containsKey(dateStr)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isSelectedDay -> BrandLimeAccent
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday && !isSelectedDay) 1.dp else 0.dp,
                                            color = if (isToday && !isSelectedDay) BrandLimeAccent else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onDateSelected(dateObj) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelectedDay || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelectedDay) OnLimeBlack else OnSurfaceWhite
                                        )

                                        if (hasReminders) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelectedDay) OnLimeBlack else BrandLimeAccent)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            currentDayNum++
                        }
                    }
                }
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
