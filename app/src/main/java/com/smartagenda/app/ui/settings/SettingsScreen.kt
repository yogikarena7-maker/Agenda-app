package com.smartagenda.app.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.smartagenda.app.ai.GeminiClient
import com.smartagenda.app.auth.AuthState
import com.smartagenda.app.auth.GoogleAuthManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    onSyncGmailClick: (idToken: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var exactAlarmPermission by remember { mutableStateOf(true) }

    val authState by GoogleAuthManager.authState.collectAsState()
    val isSyncing by GoogleAuthManager.isSyncing.collectAsState()

    val obsidianBg = Color(0xFF0A0C10)
    val cardBg = Color(0xFF13151A)
    val cardBorder = Color(0xFF262B36)
    val accentCyan = Color(0xFF00E5FF)
    val accentLime = Color(0xFFD4FF00)
    var apiKeyRefreshTrigger by remember { mutableStateOf(0) }
    val isApiKeyActive = remember(apiKeyRefreshTrigger) { GeminiClient.isApiKeyConfigured(context) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showCustomEmailDialog by remember { mutableStateOf(false) }
    var enteredApiKey by remember { mutableStateOf("") }
    var enteredGmailAddress by remember { mutableStateOf("") }
    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GoogleAuthManager.WEB_CLIENT_ID)
            .requestEmail()
            .requestScopes(
                Scope(GoogleAuthManager.GMAIL_SCOPES[0]),
                Scope(GoogleAuthManager.GMAIL_SCOPES[1])
            )
            .build()
    }

    val googleSignInClient = remember(context) {
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null) {
                val email = account.email ?: "user@gmail.com"
                val displayName = account.displayName ?: email
                val idToken = account.idToken ?: "real_id_token"
                val photoUrl = account.photoUrl?.toString()

                coroutineScope.launch {
                    GoogleAuthManager.handleGoogleSignInAccount(
                        context = context,
                        email = email,
                        displayName = displayName,
                        idToken = idToken,
                        photoUrl = photoUrl,
                        account = account.account
                    )
                }
            } else {
                coroutineScope.launch { GoogleAuthManager.signIn(context) }
            }
        } catch (_: Exception) {
            coroutineScope.launch { GoogleAuthManager.signIn(context) }
        }
    }

    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                GoogleAuthManager.requestGmailAccessToken(context)
            }
        } else {
            GoogleAuthManager.cancelConsent()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.NeedsConsent) {
            val intent = (authState as AuthState.NeedsConsent).intent
            consentLauncher.launch(intent)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(obsidianBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings & Integrations",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Text(
            text = "Real-time Google OAuth, Gmail API Connector, Gemini AI & System Settings",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Google Account Card ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2638)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Account",
                                tint = accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Google Account",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = when (val s = authState) {
                                    is AuthState.Connected -> "${s.accountInfo.displayName} (${s.accountInfo.email})"
                                    is AuthState.NeedsConsent -> "${s.accountInfo.displayName} (Action Required)"
                                    is AuthState.Loading -> "Signing in…"
                                    is AuthState.Error -> "Error — please retry"
                                    else -> "Not Connected"
                                },
                                color = when (authState) {
                                    is AuthState.Connected -> accentCyan
                                    is AuthState.NeedsConsent -> Color(0xFFFF9800)
                                    is AuthState.Error -> Color(0xFFFF5252)
                                    else -> Color.Gray
                                },
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    when (authState) {
                        is AuthState.Connected, is AuthState.NeedsConsent -> {
                            OutlinedButton(
                                onClick = {
                                    GoogleAuthManager.signOut(context)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color.Gray)
                                )
                            ) {
                                Text("Sign Out", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                        is AuthState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = accentCyan,
                                strokeWidth = 2.dp
                            )
                        }
                        else -> {
                            Row {
                                Button(
                                    onClick = {
                                        try {
                                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                        } catch (e: Exception) {
                                            coroutineScope.launch { GoogleAuthManager.signIn(context) }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentCyan)
                                ) {
                                    Text(
                                        "Sign in with Google",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (authState !is AuthState.Connected) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = { showCustomEmailDialog = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            "Or Sign in with Custom Gmail Address →",
                            color = accentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Error detail row
                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Gmail API Connector Card ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF232D1B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Gmail",
                                tint = accentLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gmail API Connector",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            val hasAccessToken = (authState as? AuthState.Connected)?.accountInfo?.accessToken != null
                            val isConnected = hasAccessToken

                            val lastSync = (authState as? AuthState.Connected)?.accountInfo?.lastSyncedTimestamp
                            val timeStr = if (lastSync != null) {
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastSync))
                            } else "Never"

                            Text(
                                text = if (isConnected) "Active • Last synced $timeStr" else "Sign in to enable Gmail sync",
                                color = if (isConnected) accentLime else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    val isConnected = authState is AuthState.Connected
                    val currentToken = (authState as? AuthState.Connected)?.accountInfo?.accessToken ?: ""

                    if (isConnected) {
                        Button(
                            onClick = { onSyncGmailClick(currentToken) },
                            enabled = !isSyncing,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentLime)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sync",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Sync Inbox",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Syncs real inbox emails using Gmail REST API (gmail.readonly & gmail.send scopes). " +
                            "Enables AI email classification and tone-based draft composition with mandatory human approval.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── AI Configuration Card ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2638)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini",
                                tint = accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gemini 3.5 Flash Engine",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(text = "REST API Provider", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isApiKeyActive) Color(0xFF00E676).copy(alpha = 0.15f)
                                else Color(0xFFFFB74D).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isApiKeyActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Status",
                                tint = if (isApiKeyActive) Color(0xFF00E676) else Color(0xFFFFB74D),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isApiKeyActive) "Connected" else "Fallback Local NLP",
                                color = if (isApiKeyActive) Color(0xFF00E676) else Color(0xFFFFB74D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isApiKeyActive)
                        "Gemini API key is active. Natural language parsing and 9-tone Email AI draft generation utilize online Gemini model inference."
                    else
                        "No external GEMINI_API_KEY detected. Running in Smart Offline NLP mode using local heuristic parsing & structured response engine.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val currentKey = GeminiClient.getApiKey(context)
                            enteredApiKey = if (currentKey.contains("MY_GEMINI_API_KEY")) "" else currentKey
                            showApiKeyDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                    ) {
                        Text("Configure Key", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isTestingConnection = true
                                testStatusMessage = "Testing Gemini 3.5 Flash..."
                                val res = GeminiClient.testConnection(context)
                                if (res.isSuccess) {
                                    testStatusMessage = "✓ Connected to Gemini 3.5 Flash successfully"
                                } else {
                                    testStatusMessage = "✕ Connection failed: ${res.exceptionOrNull()?.message}"
                                }
                                isTestingConnection = false
                            }
                        },
                        enabled = !isTestingConnection,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentCyan)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Test Connection", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                if (testStatusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = testStatusMessage!!,
                        color = if (testStatusMessage!!.startsWith("✓")) Color(0xFF00E676) else Color(0xFFFF5252),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Notifications Card ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262B36)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notif",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Agenda Notifications",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = accentCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exact Alarm Permissions",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = exactAlarmPermission,
                        onCheckedChange = { exactAlarmPermission = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = accentCyan
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── System Architecture Info ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "App Info",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "System Architecture",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Package: com.smartagenda.app\n" +
                            "• Auth: Real Google OAuth 2.0 & Credential Manager\n" +
                            "• Gmail: REST API (gmail.readonly & gmail.send)\n" +
                            "• AI Engine: Gemini 3.5 Flash Model\n" +
                            "• Database: Room 2.6.1 SQLite\n" +
                            "• Alarm Engine: Android AlarmManager",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }

    if (showCustomEmailDialog) {
        AlertDialog(
            onDismissRequest = { showCustomEmailDialog = false },
            title = {
                Text("Sign in with Custom Gmail Address", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Enter your real Gmail address to connect your account:",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredGmailAddress,
                        onValueChange = { enteredGmailAddress = it },
                        placeholder = { Text("e.g. john.doe@gmail.com", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentCyan,
                            unfocusedBorderColor = cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredGmailAddress.isNotBlank()) {
                            coroutineScope.launch {
                                GoogleAuthManager.signIn(context, customEmail = enteredGmailAddress.trim())
                            }
                        }
                        showCustomEmailDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentCyan)
                ) {
                    Text("Connect", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomEmailDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF13151A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Text("Gemini API Key", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Enter your Google AI Studio API key for Gemini 3.5 Flash inference:",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredApiKey,
                        onValueChange = { enteredApiKey = it },
                        placeholder = { Text("Paste API key here...", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentCyan,
                            unfocusedBorderColor = cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Leave empty to restore the active project default key.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("smart_agenda_gemini_prefs", android.content.Context.MODE_PRIVATE)
                        if (enteredApiKey.isBlank()) {
                            prefs.edit().remove("gemini_api_key").apply()
                            GeminiClient.setCustomApiKey(null)
                        } else {
                            prefs.edit().putString("gemini_api_key", enteredApiKey.trim()).apply()
                            GeminiClient.setCustomApiKey(enteredApiKey.trim())
                        }
                        apiKeyRefreshTrigger++
                        testStatusMessage = null
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentCyan)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF13151A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
