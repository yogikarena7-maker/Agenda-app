package com.smartagenda.app.ui.email

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.app.data.local.EmailMessageEntity
import com.smartagenda.app.ui.theme.AccentCyan
import com.smartagenda.app.ui.theme.GradientAmberEnd
import com.smartagenda.app.ui.theme.GradientAmberStart
import com.smartagenda.app.ui.theme.GradientCyanEnd
import com.smartagenda.app.ui.theme.GradientCyanStart
import com.smartagenda.app.ui.theme.GradientLimeEnd
import com.smartagenda.app.ui.theme.GradientLimeStart
import com.smartagenda.app.ui.theme.GradientRedEnd
import com.smartagenda.app.ui.theme.GradientRedStart
import com.smartagenda.app.util.DateTimeUtils
import java.util.Date

@Composable
fun EmailItemCard(
    email: EmailMessageEntity,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val obsidianBg = Color(0xFF13151A)
    val cardBorder = Color(0xFF262B36)
    val accentAmber = Color(0xFFFFB74D)

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "press_scale"
    )

    val (categoryColor, categoryGradientStart, categoryGradientEnd) = when (email.category.lowercase()) {
        "urgent" -> Triple(Color(0xFFFF5252), GradientRedStart, GradientRedEnd)
        "action required" -> Triple(Color(0xFFFF9800), GradientAmberStart, GradientAmberEnd)
        "needs reply" -> Triple(AccentCyan, GradientCyanStart, GradientCyanEnd)
        "newsletter" -> Triple(Color(0xFFB0BEC5), Color(0xFF607D8B), Color(0xFF455A64))
        else -> Triple(Color(0xFF81C784), GradientLimeStart, GradientLimeEnd)
    }

    val initials = email.senderName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "E" }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (email.isRead) obsidianBg else Color(0xFF191D26))
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .height(IntrinsicSize.Min)
    ) {
        // Gradient left stripe for unread emails
        if (!email.isRead) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(categoryGradientStart, categoryGradientEnd)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.padding(start = if (!email.isRead) 18.dp else 14.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Profile Pic
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        categoryGradientStart.copy(alpha = 0.2f),
                                        categoryGradientEnd.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(1.dp, categoryColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = email.senderName,
                            color = Color.White,
                            fontWeight = if (email.isRead) FontWeight.Medium else FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = email.senderEmail,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateTimeUtils.formatDisplayDate(
                            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(Date(email.receivedEpochMillis))
                        ),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    IconButton(
                        onClick = onToggleStar,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (email.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Star",
                            tint = if (email.isStarred) accentAmber else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = email.subject,
                color = Color.White,
                fontWeight = if (email.isRead) FontWeight.Normal else FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = email.snippet,
                color = Color.LightGray,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                categoryColor.copy(alpha = 0.15f),
                                categoryColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = email.category,
                    color = categoryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
