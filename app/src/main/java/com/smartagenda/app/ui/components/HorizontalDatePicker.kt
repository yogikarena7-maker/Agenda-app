package com.smartagenda.app.ui.components

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.smartagenda.app.ui.theme.BrandDarkSurface
import com.smartagenda.app.ui.theme.BrandLimeAccent
import com.smartagenda.app.ui.theme.BrandTextMuted
import com.smartagenda.app.ui.theme.OnLimeBlack
import com.smartagenda.app.ui.theme.OnSurfaceWhite
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class DynamicDayItem(
    val date: LocalDate,
    val dayMonogram: String,
    val dayNumber: String,
    val isSelected: Boolean = false
)

@Composable
fun HorizontalDatePicker(
    selectedDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit = {}
) {
    // Generate week days (Monday through Sunday) for the selected week
    val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = (0..6).map { offset ->
        val date = startOfWeek.plusDays(offset.toLong())
        DynamicDayItem(
            date = date,
            dayMonogram = date.dayOfWeek.name.take(1),
            dayNumber = date.dayOfMonth.toString(),
            isSelected = date.isEqual(selectedDate)
        )
    }

    val isToday = selectedDate.isEqual(LocalDate.now())
    val formattedSubtitle = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Day selector row container with week navigation controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BrandDarkSurface)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Week Button
            IconButton(
                onClick = { onDateSelected(selectedDate.minusWeeks(1)) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Week",
                    tint = BrandTextMuted
                )
            }

            days.forEach { dayItem ->
                val pillModifier = Modifier
                    .width(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (dayItem.isSelected) BrandLimeAccent else Color.Transparent)
                    .clickable { onDateSelected(dayItem.date) }
                    .padding(vertical = 9.dp)

                Column(
                    modifier = pillModifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayItem.dayMonogram,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dayItem.isSelected) OnLimeBlack.copy(alpha = 0.8f) else BrandTextMuted,
                        lineHeight = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayItem.dayNumber,
                        fontSize = 15.sp,
                        fontWeight = if (dayItem.isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (dayItem.isSelected) OnLimeBlack else OnSurfaceWhite,
                        lineHeight = 15.sp
                    )
                }
            }

            // Next Week Button
            IconButton(
                onClick = { onDateSelected(selectedDate.plusWeeks(1)) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Week",
                    tint = BrandTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date Context Subtitle with "Today" indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isToday) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandLimeAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Today",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandLimeAccent
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = formattedSubtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = BrandTextMuted,
                modifier = Modifier.weight(1f)
            )

            if (!isToday) {
                Text(
                    text = "Jump to Today",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandLimeAccent,
                    modifier = Modifier.clickable { onDateSelected(LocalDate.now()) }
                )
            }
        }
    }
}
