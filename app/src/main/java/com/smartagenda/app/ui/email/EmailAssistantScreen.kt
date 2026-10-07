package com.smartagenda.app.ui.email

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.app.auth.AuthState
import com.smartagenda.app.auth.GoogleAuthManager
import com.smartagenda.app.ui.components.FilterChipRow
import com.smartagenda.app.ui.theme.AccentCyan
import com.smartagenda.app.ui.theme.CardBorderSubtle
import com.smartagenda.app.ui.theme.CardObsidian
import com.smartagenda.app.ui.theme.GlassWhite

@Composable
fun EmailAssistantScreen(
    viewModel: EmailAssistantViewModel,
    modifier: Modifier = Modifier
) {
    val emails by viewModel.filteredEmails.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedEmail by viewModel.selectedEmail.collectAsState()
    val currentDraft by viewModel.currentDraft.collectAsState()
    val isGeneratingDraft by viewModel.isGeneratingDraft.collectAsState()
    val isSyncing by viewModel.isSyncingGmail.collectAsState()
    val authState by GoogleAuthManager.authState.collectAsState()

    val obsidianBg = Color(0xFF0A0C10)
    val isConnected = authState is AuthState.Connected
    val hasAccessToken = isConnected && (authState as? AuthState.Connected)?.accountInfo?.accessToken != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(obsidianBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ── Premium Header ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Email AI Assistant",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = if (isConnected) {
                        val email = (authState as AuthState.Connected).accountInfo.email
                        "Connected: $email"
                    } else {
                        "Automated classification & human-approved drafts"
                    },
                    color = if (isConnected) AccentCyan.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sync Button (only when connected with access token)
                if (hasAccessToken) {
                    val syncRotation = if (isSyncing) {
                        val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotation"
                        )
                        rotation
                    } else 0f

                    IconButton(
                        onClick = { viewModel.syncGmailInbox() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardObsidian)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Gmail",
                            tint = AccentCyan,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(syncRotation)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Guard Active Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0A1A2A),
                                    Color(0xFF131722)
                                )
                            )
                        )
                        .border(1.dp, AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Safe Guard",
                            tint = AccentCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Guard",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category filter chips
        FilterChipRow(
            categories = viewModel.categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ── Shimmer Loading Placeholders ─────────────────────────────────────
        AnimatedVisibility(
            visible = isSyncing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    ShimmerEmailPlaceholder()
                }
            }
        }

        // ── Content Area ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = !isSyncing,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
            exit = fadeOut()
        ) {
            if (!isConnected) {
                // Not connected — show CTA to connect
                EmptyStateNotConnected()
            } else if (emails.isEmpty()) {
                // Connected but no emails — show sync CTA
                EmptyStateSyncPrompt(
                    hasAccessToken = hasAccessToken,
                    onSyncClick = { viewModel.syncGmailInbox() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(emails, key = { it.id }) { email ->
                        EmailItemCard(
                            email = email,
                            onClick = { viewModel.selectEmail(email) },
                            onToggleStar = { viewModel.toggleStar(email) }
                        )
                    }
                }
            }
        }
    }

    // Email detail dialog
    selectedEmail?.let { email ->
        EmailDetailDialog(
            email = email,
            onDismiss = { viewModel.selectEmail(null) },
            onGenerateAiDraft = { tone -> viewModel.generateAiDraft(email, tone) },
            isGeneratingDraft = isGeneratingDraft
        )
    }

    // Human Approval Draft dialog
    currentDraft?.let { draft ->
        HumanApprovalDraftDialog(
            draft = draft,
            onDismiss = { viewModel.dismissDraftDialog() },
            onApproveAndSend = { approvedDraft -> viewModel.approveAndSendDraft(approvedDraft) },
            onDiscard = { viewModel.discardDraft() },
            onRegenerateWithTone = { newTone -> viewModel.regenerateDraftWithTone(newTone) }
        )
    }
}

@Composable
private fun EmptyStateNotConnected() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentCyan.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LinkOff,
                    contentDescription = "Not Connected",
                    tint = Color(0xFF4A5568),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Gmail Not Connected",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in with Google in Settings to sync your real inbox and unlock AI-powered email drafting.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentCyan.copy(alpha = 0.1f))
                    .border(1.dp, AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Go to Settings → Sign in with Google",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateSyncPrompt(
    hasAccessToken: Boolean,
    onSyncClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentCyan.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Sync",
                    tint = AccentCyan.copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Inbox Empty",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasAccessToken) {
                    "Tap the button below to fetch your latest Gmail messages."
                } else {
                    "Gmail access token not available. Please sign out and sign in again."
                },
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            if (hasAccessToken) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onSyncClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sync Gmail Inbox",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerEmailPlaceholder() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardObsidian)
            .border(1.dp, CardBorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = shimmerAlpha))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha * 0.7f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha * 0.6f))
            )
        }
    }
}
