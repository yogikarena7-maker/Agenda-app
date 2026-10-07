package com.smartagenda.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartagenda.app.ui.theme.BrandLimeAccent
import com.smartagenda.app.ui.theme.OnSurfaceWhite
import kotlinx.coroutines.launch

@Composable
fun ZendaTimePicker(
    initialTimeStr: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Parse initial time string into 12-hour components
    val (initialHour12, initialMinute, initialIsAm) = remember(initialTimeStr) {
        parseTimeComponents(initialTimeStr)
    }

    var selectedHour by remember { mutableIntStateOf(initialHour12) } // 1..12
    var selectedMinute by remember { mutableIntStateOf(initialMinute) } // 0..59
    var isAm by remember { mutableStateOf(initialIsAm) }

    // Clean Entry Animations
    val bgAlpha = remember { Animatable(0f) }
    val surfaceScale = remember { Animatable(0.95f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        launch { bgAlpha.animateTo(0.65f, tween(durationMillis = 250)) }
        launch {
            surfaceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch { contentAlpha.animateTo(1f, tween(durationMillis = 240, easing = FastOutSlowInEasing)) }
        launch { contentOffsetY.animateTo(0f, tween(durationMillis = 240, easing = FastOutSlowInEasing)) }
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
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Title
                    Text(
                        text = "Select Time",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Digital Time Display & AM/PM Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Digital Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1B2840))
                                .border(1.5.dp, BrandLimeAccent, RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", selectedHour),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandLimeAccent
                            )
                        }

                        Text(
                            text = ":",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceWhite
                        )

                        // Minute Digital Box
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1B2840))
                                .border(1.5.dp, BrandLimeAccent, RoundedCornerShape(16.dp))
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", selectedMinute),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandLimeAccent
                            )
                        }

                        // AM/PM Segmented Toggle Pill
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B2840))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAm) BrandLimeAccent else Color.Transparent)
                                    .clickable { isAm = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAm) Color.Black else OnSurfaceWhite
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isAm) BrandLimeAccent else Color.Transparent)
                                    .clickable { isAm = false }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isAm) Color.Black else OnSurfaceWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Hours Selection Grid (1 - 12)
                    Text(
                        text = "Hour",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceWhite.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )

                    Column {
                        for (r in 0 until 2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (c in 1..6) {
                                    val hr = r * 6 + c
                                    val isSelected = selectedHour == hr
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .padding(horizontal = 2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) BrandLimeAccent else Color(0xFF18253B))
                                            .clickable { selectedHour = hr },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = hr.toString(),
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.Black else OnSurfaceWhite
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Minute Fine-Tuning & 5-minute Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Minute",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceWhite.copy(alpha = 0.6f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { selectedMinute = (selectedMinute - 1 + 60) % 60 },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B2840))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Minute",
                                    tint = OnSurfaceWhite
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%02d m", selectedMinute),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandLimeAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { selectedMinute = (selectedMinute + 1) % 60 },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1B2840))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Minute",
                                    tint = OnSurfaceWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Minute Preset Buttons (00, 05, 10, 15, 30, 45)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0, 5, 10, 15, 30, 45).forEach { minPreset ->
                            val isSelected = selectedMinute == minPreset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandLimeAccent else Color(0xFF18253B))
                                    .clickable { selectedMinute = minPreset },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format("%02d", minPreset),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else OnSurfaceWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                                val hour24 = when {
                                    isAm && selectedHour == 12 -> 0
                                    !isAm && selectedHour < 12 -> selectedHour + 12
                                    else -> selectedHour
                                }
                                val formattedTime = String.format("%02d:%02d", hour24, selectedMinute)
                                onTimeSelected(formattedTime)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandLimeAccent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(
                                text = "Select Time",
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

private fun parseTimeComponents(input: String): Triple<Int, Int, Boolean> {
    val clean = input.trim()
    try {
        if (clean.contains(":")) {
            val parts = clean.split(":")
            val h24 = parts[0].toIntOrNull() ?: 9
            val m = parts[1].split(" ")[0].toIntOrNull() ?: 0
            val isAm = h24 < 12 || clean.lowercase().contains("am")
            val h12 = when {
                h24 == 0 -> 12
                h24 > 12 -> h24 - 12
                else -> h24
            }
            return Triple(h12, m, isAm)
        }
    } catch (e: Exception) {
        // Fallback default
    }
    return Triple(9, 30, true)
}
