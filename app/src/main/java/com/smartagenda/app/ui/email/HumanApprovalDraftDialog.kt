package com.smartagenda.app.ui.email

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartagenda.app.ai.GeminiEmailAssistant
import com.smartagenda.app.data.local.EmailDraftEntity

@Composable
fun HumanApprovalDraftDialog(
    draft: EmailDraftEntity,
    onDismiss: () -> Unit,
    onApproveAndSend: (EmailDraftEntity) -> Unit,
    onSaveToGmailDraft: (EmailDraftEntity) -> Unit = {},
    onDiscard: () -> Unit,
    onRegenerateWithTone: (String) -> Unit
) {
    var editableBody by remember { mutableStateOf(draft.draftBody) }
    var selectedTone by remember { mutableStateOf(draft.tone) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var isSavingDraft by remember { mutableStateOf(false) }

    val obsidianBg = Color(0xFF13151A)
    val cardBorder = Color(0xFF262B36)
    val accentCyan = Color(0xFF00E5FF)
    val accentGreen = Color(0xFF00E676)
    val accentRed = Color(0xFFFF5252)

    Dialog(onDismissRequest = { if (!isSending) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, cardBorder, RoundedCornerShape(24.dp)),
            color = obsidianBg
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Human Approval Safeguard",
                            tint = accentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Human Approval Required",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSending
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "AI draft response for ${draft.recipientName} (${draft.recipientEmail})",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mood / Tone Selector Dropdown
                Text(
                    text = "Select Mood / Tone:",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1D24))
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isSending) { isDropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tone: ",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Text(
                                text = selectedTone,
                                color = accentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Tone",
                            tint = accentCyan
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF1A1D24))
                    ) {
                        GeminiEmailAssistant.SUPPORTED_TONES.forEach { tone ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = tone,
                                        color = if (tone.equals(selectedTone, ignoreCase = true)) accentCyan else Color.White,
                                        fontWeight = if (tone.equals(selectedTone, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedTone = tone
                                    isDropdownExpanded = false
                                    onRegenerateWithTone(tone)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Editable Draft Text Area
                Text(
                    text = "Review & Edit Response Body:",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = editableBody,
                    onValueChange = { editableBody = it },
                    enabled = !isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1D24),
                        unfocusedContainerColor = Color(0xFF1A1D24),
                        focusedBorderColor = accentCyan,
                        unfocusedBorderColor = cardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Discard / Save Draft / Approve & Send)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onDiscard,
                            enabled = !isSending && !isSavingDraft,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF261D22)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Discard",
                                tint = accentRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Discard", color = accentRed, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (!isSending && !isSavingDraft) {
                                    isSavingDraft = true
                                    onSaveToGmailDraft(draft.copy(draftBody = editableBody, tone = selectedTone))
                                }
                            },
                            enabled = !isSending && !isSavingDraft,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E2838)
                            )
                        ) {
                            if (isSavingDraft) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = accentCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Save as Draft",
                                    color = accentCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (!isSending && !isSavingDraft) {
                                isSending = true
                                onApproveAndSend(draft.copy(draftBody = editableBody, tone = selectedTone))
                            }
                        },
                        enabled = !isSending && !isSavingDraft,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentGreen
                        )
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sending to Gmail...",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Approve & Send",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Approve & Send",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
