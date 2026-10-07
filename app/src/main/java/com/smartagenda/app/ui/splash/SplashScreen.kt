package com.smartagenda.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation properties
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotationY = remember { Animatable(-30f) }
    val logoRotationX = remember { Animatable(15f) }
    val ringScale = remember { Animatable(0.8f) }
    val ringAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(20f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: App Launch (0 - 300ms) — Logo scales in & fades in
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1.05f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }

        delay(300)

        // Step 2: 3D Icon Animation & Orbital Light Ring (300 - 800ms)
        launch {
            logoRotationY.animateTo(
                targetValue = 15f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoRotationX.animateTo(
                targetValue = -10f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            ringAlpha.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(durationMillis = 300)
            )
            ringAlpha.animateTo(
                targetValue = 0.3f,
                animationSpec = tween(durationMillis = 400, easing = LinearEasing)
            )
        }
        launch {
            ringScale.animateTo(
                targetValue = 1.4f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }

        delay(450)

        // Step 3: Logo Settles (750 - 1000ms)
        launch {
            logoRotationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoRotationX.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        delay(250)

        // Step 4: App Name Appears ("Zenda") (1000 - 1250ms)
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250)
            )
        }
        launch {
            titleOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
        }

        delay(200)

        // Step 5: Final Tagline ("Plan • Focus • Achieve") (1200 - 1500ms)
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
        }

        delay(400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0D1627),
                        Color(0xFF060910)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Expanding Orbital Glow Light Ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ringScale.value)
                        .alpha(ringAlpha.value)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.4f),
                                    Color(0xFF2979FF).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF2979FF),
                                    Color(0xFF00E5FF)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                )

                // Main Zenda 3D Glassmorphic Emblem Container
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .graphicsLayer {
                            rotationY = logoRotationY.value
                            rotationX = logoRotationX.value
                            cameraDistance = 12f * density
                        }
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF131F37),
                                    Color(0xFF0B1220)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF41659D),
                                    Color(0xFF1A2B49)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_zenda),
                        contentDescription = "Zenda Logo",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name ("Zenda")
            Text(
                text = "Zenda",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .graphicsLayer {
                        translationY = titleOffsetY.value
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline ("Plan • Focus • Achieve")
            Text(
                text = "Plan  •  Focus  •  Achieve",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8A99AD),
                letterSpacing = 1.5.sp,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
