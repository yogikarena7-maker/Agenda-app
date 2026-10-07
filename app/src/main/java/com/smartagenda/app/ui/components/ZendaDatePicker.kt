package com.smartagenda.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartagenda.app.ui.theme.BrandLimeAccent
import com.smartagenda.app.ui.theme.OnSurfaceWhite
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ZendaDatePicker(
    initialDateStr: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialDate = remember(initialDateStr) {
        try {
            LocalDate.parse(initialDateStr)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    var selectedDate by remember { mutableStateOf(initialDate) }
    var displayedYearMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var isNextMonthTransition by remember { mutableStateOf(true) }

    // Clean Layer Entry Animations
    val bgAlpha = remember { Animatable(0f) }
    val surfaceScale = remember { Animatable(0.95f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        launch {
            bgAlpha.animateTo(0.65f, tween(durationMillis = 250))
        }
        launch {
            surfaceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            contentAlpha.animateTo(1f, tween(durationMillis = 240, easing = FastOutSlowInEasing))
        }
        launch {
            contentOffsetY.animateTo(0f, tween(durationMillis = 240, easing = FastOutSlowInEasing))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = bgAlpha.value))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .scale(surfaceScale.value)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF131D31),
                                Color(0xFF0C1322)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF283D61),
                                Color(0xFF18253B)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .alpha(contentAlpha.value)
                        .graphicsLayer {
                            translationY = contentOffsetY.value
                        }
                ) {
                    // Header Bar: Month + Year Navigation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceWhite
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    isNextMonthTransition = false
                                    displayedYearMonth = displayedYearMonth.minusMonths(1)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B2840))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Month",
                                    tint = OnSurfaceWhite
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    isNextMonthTransition = true
                                    displayedYearMonth = displayedYearMonth.plusMonths(1)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B2840))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = OnSurfaceWhite
                                )
                            }
                        }
                    }

                    // Weekday Headers (S, M, T, W, T, F, S)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(
                            DayOfWeek.SUNDAY,
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY,
                            DayOfWeek.SATURDAY
                        ).forEach { dayOfWeek ->
                            Text(
                                text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandLimeAccent.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Directional Slide Animated Calendar Grid
                    AnimatedContent(
                        targetState = displayedYearMonth,
                        transitionSpec = {
                            if (isNextMonthTransition) {
                                (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                            } else {
                                (slideInHorizontally(initialOffsetX = { -it }) + fadeIn()) togetherWith
                                        (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                            }
                        },
                        label = "MonthTransition"
                    ) { yearMonth ->
                        CalendarGrid(
                            yearMonth = yearMonth,
                            selectedDate = selectedDate,
                            onDayClick = { date ->
                                selectedDate = date
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons (Cancel / Confirm)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text(
                                text = "Cancel",
                                color = OnSurfaceWhite.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                val formatted = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                onDateSelected(formatted)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandLimeAccent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(
                                text = "Select Date",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val firstOfMonth = yearMonth.atDay(1)
    val dayOfWeekOfFirst = firstOfMonth.dayOfWeek
    // Convert Sunday=0, Monday=1, ... Saturday=6
    val offset = dayOfWeekOfFirst.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    val today = LocalDate.now()

    Column {
        var currentDayCounter = 1 - offset

        for (row in 0 until 6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0 until 7) {
                    val dateForCell = when {
                        currentDayCounter in 1..daysInMonth -> {
                            yearMonth.atDay(currentDayCounter)
                        }
                        currentDayCounter < 1 -> {
                            yearMonth.minusMonths(1).atDay(
                                yearMonth.minusMonths(1).lengthOfMonth() + currentDayCounter
                            )
                        }
                        else -> {
                            yearMonth.plusMonths(1).atDay(currentDayCounter - daysInMonth)
                        }
                    }

                    val isCurrentMonth = currentDayCounter in 1..daysInMonth
                    val isSelected = dateForCell == selectedDate
                    val isToday = dateForCell == today

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> BrandLimeAccent
                                    isToday -> Color(0xFF1E3252)
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                color = if (isToday && !isSelected) BrandLimeAccent else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                onDayClick(dateForCell)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dateForCell.dayOfMonth.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected -> Color.Black
                                isCurrentMonth -> OnSurfaceWhite
                                else -> OnSurfaceWhite.copy(alpha = 0.3f)
                            }
                        )
                    }

                    currentDayCounter++
                }
            }
        }
    }
}
