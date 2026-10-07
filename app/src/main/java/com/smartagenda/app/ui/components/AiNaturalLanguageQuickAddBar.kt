package com.smartagenda.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.app.ai.ParsedReminder

@Composable
fun AiNaturalLanguageQuickAddBar(
    text: String,
    onTextChange: (String) -> Unit,
    onParseRequest: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    parsedPreview: ParsedReminder? = null,
    isParsing: Boolean = false,
    errorMessage: String? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val obsidianBg = Color(0xFF13151A)
    val accentCyan = Color(0xFF00E5FF)
    val cardBorder = Color(0xFF262B36)
    val errorColor = Color(0xFFFF5252)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(obsidianBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Sparkle",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                enabled = !isParsing,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "AI Quick Add: 'Meeting tomorrow at 3pm #work'",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorContainerColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    disabledTextColor = Color.White.copy(alpha = 0.6f),
                    disabledBorderColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank() && !isParsing) {
                        onParseRequest()
                        keyboardController?.hide()
                    }
                })
            )

            // Clear button if user has entered text
            if (text.isNotEmpty() && !isParsing) {
                IconButton(
                    onClick = { onTextChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Input",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (isParsing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = accentCyan,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            onParseRequest()
                            keyboardController?.hide()
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Add Reminder",
                        tint = if (text.isNotBlank()) accentCyan else Color.Gray
                    )
                }
            }
        }

        // Loading indicator state with helpful message
        AnimatedVisibility(
            visible = isParsing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 46.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI is understanding your reminder...",
                    color = accentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Friendly Error message state
        if (errorMessage != null && !isParsing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 46.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = errorMessage,
                    color = errorColor,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { onParseRequest() },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Try Again",
                        color = accentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Structured preview card for user verification / confirmation
        if (parsedPreview != null && !isParsing) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E222D))
                    .border(1.dp, Color(0xFF262B36), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "I understood:",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = parsedPreview.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = parsedPreview.dateString, color = Color.LightGray, fontSize = 13.sp)
                    Text(text = " • ", color = Color.Gray, fontSize = 13.sp)
                    Text(text = parsedPreview.timeString, color = Color.LightGray, fontSize = 13.sp)
                    Text(text = " • ", color = Color.Gray, fontSize = 13.sp)
                    Text(text = parsedPreview.category, color = accentCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                // Ambiguity / clarification banner if detected
                if (parsedPreview.isAmbiguous || !parsedPreview.clarification.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2C2200))
                            .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = parsedPreview.clarification ?: "Please verify time and details.",
                            color = Color(0xFFFFB300),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = Color.Gray, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = onEdit) {
                        Text("Edit", color = Color.White, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
