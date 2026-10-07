package com.smartagenda.ui.dashboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

data class Task(val id: String, val title: String, val estimateMins: Int, val isCompleted: Boolean = false)

@Composable
fun FocusDashboardScreen(
    tasks: List<Task>,
    isLoading: Boolean,
    isFocusModeActive: Boolean,
    focusProgress: Float,
    onTaskComplete: (String) -> Unit,
    onStartFocus: () -> Unit,
    onPauseFocus: () -> Unit
) {
    val haptic = LocalView.current 

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) 
            .windowInsetsPadding(WindowInsets.statusBars) 
    ) {
        
        AnimatedVisibility(
            visible = isFocusModeActive,
            enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)),
            modifier = Modifier.fillMaxSize().zIndex(10f)
        ) {
            ZenFocusModeHardwareOverlay(
                progress = focusProgress,
                onPauseBtnClick = {
                    haptic.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onPauseFocus()
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "My Day",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            if (isLoading) {
                SkeletonLoadingList()
            } else if (tasks.isEmpty()) {
                InteractiveEmptyState(haptic)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskPlanningCard(task = task, onComplete = { 
                            haptic.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            onTaskComplete(it.id)
                        })
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isFocusModeActive,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FocusLaunchBottomSheet(onStartFocus = {
                haptic.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onStartFocus()
            })
        }
    }
}

@Composable
fun TaskPlanningCard(task: Task, onComplete: (Task) -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    
    val containerColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable { 
                isPressed = !isPressed 
                onComplete(task)
            }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Complete Task",
            tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface 
            )
            Text(
                text = "${task.estimateMins}m est.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant 
            )
        }
    }
}

@Composable
fun skeletonShimmerAnimation(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    return alpha
}

@Composable
fun SkeletonLoadingList() {
    val alpha = skeletonShimmerAnimation()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun InteractiveEmptyState(haptic: android.view.View) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(32.dp)))
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("No tasks planned", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { haptic.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) },
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("+ Plan First Focus Session")
        }
    }
}

@Composable
fun FocusLaunchBottomSheet(onStartFocus: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(24.dp)
    ) {
        Button(
            onClick = onStartFocus,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Begin Focus", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ZenFocusModeHardwareOverlay(progress: Float, onPauseBtnClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) 
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
            label = "focus_arc"
        )
        
        val strokeColor = MaterialTheme.colorScheme.primary
        val bgColor = MaterialTheme.colorScheme.surfaceVariant

        Canvas(modifier = Modifier.size(280.dp)) {
            val strokeWidth = 12.dp.toPx()
            
            drawArc(
                color = bgColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width, size.height),
                topLeft = Offset(0f, 0f)
            )

            drawArc(
                color = strokeColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width, size.height),
                topLeft = Offset(0f, 0f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 380.dp)
        ) {
            FilledTonalButton(
                onClick = onPauseBtnClick,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Pause Session")
            }
        }
    }
}
