package com.smartagenda.app.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class GoogleAccountInfo(
    val email: String,
    val displayName: String,
    val isGmailConnected: Boolean = false,
    val idToken: String,
    val accessToken: String? = null,
    val profilePictureUrl: String? = null,
    val lastSyncedTimestamp: Long = System.currentTimeMillis()
)

sealed class AuthState {
    object Disconnected : AuthState()
    object Loading : AuthState()
    data class Connected(val accountInfo: GoogleAccountInfo) : AuthState()
    data class NeedsConsent(val accountInfo: GoogleAccountInfo, val intent: android.content.Intent) : AuthState()
    data class Error(val message: String) : AuthState()
}

object GoogleAuthManager {
    private const val TAG = "GoogleAuthManager"

    const val WEB_CLIENT_ID = "859163604812-bak9ks93bf3v4kkpvg36sn8ok7bs0k7s.apps.googleusercontent.com"

    val GMAIL_SCOPES = listOf(
        "https://www.googleapis.com/auth/gmail.readonly",
        "https://www.googleapis.com/auth/gmail.compose",
        "https://www.googleapis.com/auth/gmail.send"
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Disconnected)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun init(context: Context) {
        try {
            val prefs = context.getSharedPreferences("smart_agenda_auth_prefs", Context.MODE_PRIVATE)
            val email = prefs.getString("email", null)
            if (!email.isNullOrBlank()) {
                val displayName = prefs.getString("displayName", email) ?: email
                val accessToken = prefs.getString("accessToken", null)
                val idToken = prefs.getString("idToken", "") ?: ""
                val isGmailConnected = prefs.getBoolean("isGmailConnected", false)
                val profilePictureUrl = prefs.getString("profilePictureUrl", null)
                val lastSyncedTimestamp = prefs.getLong("lastSyncedTimestamp", System.currentTimeMillis())

                val info = GoogleAccountInfo(
                    email = email,
                    displayName = displayName,
                    isGmailConnected = isGmailConnected,
                    idToken = idToken,
                    accessToken = accessToken,
                    profilePictureUrl = profilePictureUrl,
                    lastSyncedTimestamp = lastSyncedTimestamp
                )
                _authState.value = AuthState.Connected(info)
                Log.d(TAG, "Restored Google Account session for: $email (hasToken=${!accessToken.isNullOrBlank()})")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to restore auth state", e)
        }
    }

    private fun saveAuthState(context: Context, info: GoogleAccountInfo) {
        try {
            context.getSharedPreferences("smart_agenda_auth_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("email", info.email)
                .putString("displayName", info.displayName)
                .putString("accessToken", info.accessToken)
                .putString("idToken", info.idToken)
                .putBoolean("isGmailConnected", info.isGmailConnected)
                .putString("profilePictureUrl", info.profilePictureUrl)
                .putLong("lastSyncedTimestamp", info.lastSyncedTimestamp)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save auth state", e)
        }
    }

    private fun clearAuthState(context: Context) {
        try {
            context.getSharedPreferences("smart_agenda_auth_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear auth state", e)
        }
    }

    fun setManualAccessToken(context: Context, token: String, customEmail: String? = null) {
        val cleanToken = token.trim()
        val current = _authState.value
        val email = customEmail?.trim()?.takeIf { it.isNotBlank() }
            ?: (if (current is AuthState.Connected) current.accountInfo.email else "user@gmail.com")

        val info = GoogleAccountInfo(
            email = email,
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
            isGmailConnected = cleanToken.isNotBlank(),
            idToken = "custom_token",
            accessToken = cleanToken.ifBlank { null },
            lastSyncedTimestamp = System.currentTimeMillis()
        )
        _authState.value = AuthState.Connected(info)
        saveAuthState(context, info)
        Log.d(TAG, "Saved access token for $email")
    }

    /**
     * Called when a real Google Account is selected via Google Sign-In or CredentialManager.
     */
    suspend fun handleGoogleSignInAccount(
        context: Context,
        email: String,
        displayName: String,
        idToken: String,
        photoUrl: String? = null,
        account: android.accounts.Account? = null
    ) {
        _authState.value = AuthState.Loading
        val normalizedEmail = sanitizeGmailAddress(email)
        Log.d(TAG, "Connecting real Google Account: $normalizedEmail")

        val initialInfo = GoogleAccountInfo(
            email = normalizedEmail,
            displayName = displayName,
            isGmailConnected = true,
            idToken = idToken,
            accessToken = null,
            profilePictureUrl = photoUrl,
            lastSyncedTimestamp = System.currentTimeMillis()
        )

        // Request real OAuth 2.0 access token for Gmail API
        val targetAccount = account ?: android.accounts.Account(normalizedEmail, "com.google")
        try {
            val scopeStr = "oauth2:" + GMAIL_SCOPES.joinToString(" ")
            val token = withContext(Dispatchers.IO) {
                com.google.android.gms.auth.GoogleAuthUtil.getToken(context, targetAccount, scopeStr)
            }
            if (!token.isNullOrBlank()) {
                val updated = initialInfo.copy(
                    accessToken = token,
                    isGmailConnected = true,
                    lastSyncedTimestamp = System.currentTimeMillis()
                )
                _authState.value = AuthState.Connected(updated)
                saveAuthState(context, updated)
                Log.d(TAG, "Real OAuth token acquired for Gmail API ($normalizedEmail)")
            } else {
                _authState.value = AuthState.Connected(initialInfo)
                saveAuthState(context, initialInfo)
            }
        } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
            Log.w(TAG, "User interactive consent required for Gmail scopes", e)
            if (e.intent != null) {
                _authState.value = AuthState.NeedsConsent(initialInfo, e.intent!!)
            } else {
                _authState.value = AuthState.Connected(initialInfo)
                saveAuthState(context, initialInfo)
            }
        } catch (e: Exception) {
            Log.w(TAG, "OAuth token fetch notice (${e.message}). Account connected for profile.", e)
            _authState.value = AuthState.Connected(initialInfo)
            saveAuthState(context, initialInfo)
        }
    }

    /**
     * Sign in via CredentialManager or manual input.
     */
    suspend fun signIn(context: Context, customEmail: String? = null) {
        _authState.value = AuthState.Loading

        if (!customEmail.isNullOrBlank()) {
            val normalized = sanitizeGmailAddress(customEmail)
            handleGoogleSignInAccount(
                context = context,
                email = normalized,
                displayName = normalized.substringBefore("@").replaceFirstChar { it.uppercase() },
                idToken = "manual_id_token",
                account = android.accounts.Account(normalized, "com.google")
            )
            return
        }

        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val rawEmail = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: rawEmail
                val idToken = googleIdTokenCredential.idToken
                val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()

                handleGoogleSignInAccount(
                    context = context,
                    email = rawEmail,
                    displayName = displayName,
                    idToken = idToken,
                    photoUrl = photoUrl
                )
            } else {
                _authState.value = AuthState.Error("Unsupported credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            _authState.value = AuthState.Disconnected
        } catch (e: Exception) {
            Log.w(TAG, "CredentialManager sign-in notice (${e.localizedMessage})", e)
            _authState.value = AuthState.Error(e.localizedMessage ?: "Sign-in cancelled or unavailable")
        }
    }

    /**
     * Request OAuth access token with Gmail scopes using GoogleAuthUtil.
     */
    suspend fun requestGmailAccessToken(context: Context) {
        val current = _authState.value
        if (current !is AuthState.Connected && current !is AuthState.NeedsConsent) return
        
        val accountInfo = if (current is AuthState.Connected) current.accountInfo else (current as AuthState.NeedsConsent).accountInfo

        try {
            val scopeStr = "oauth2:" + GMAIL_SCOPES.joinToString(" ")
            val account = android.accounts.Account(accountInfo.email, "com.google")
            
            val accessToken = withContext(Dispatchers.IO) {
                com.google.android.gms.auth.GoogleAuthUtil.getToken(context, account, scopeStr)
            }

            if (accessToken.isNotBlank()) {
                val updated = accountInfo.copy(
                    accessToken = accessToken,
                    isGmailConnected = true,
                    lastSyncedTimestamp = System.currentTimeMillis()
                )
                _authState.value = AuthState.Connected(updated)
                saveAuthState(context, updated)
                Log.d(TAG, "Gmail OAuth access token acquired successfully")
            }
        } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
            Log.w(TAG, "User interactive consent required for Gmail scopes")
            if (e.intent != null) {
                _authState.value = AuthState.NeedsConsent(accountInfo, e.intent!!)
            } else {
                _authState.value = AuthState.Connected(accountInfo.copy(isGmailConnected = true))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error requesting Gmail access token: ${e.message}", e)
        }
    }

    fun cancelConsent() {
        val current = _authState.value
        if (current is AuthState.NeedsConsent) {
            _authState.value = AuthState.Connected(current.accountInfo)
        }
    }

    fun getAccessToken(): String? {
        val state = _authState.value
        return if (state is AuthState.Connected) state.accountInfo.accessToken else null
    }

    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context, gso).signOut()
        } catch (_: Exception) { }

        clearAuthState(context)
        _authState.value = AuthState.Disconnected
        _isSyncing.value = false
        onComplete()
    }

    fun setSyncing(syncing: Boolean) {
        _isSyncing.value = syncing
        val current = _authState.value
        if (current is AuthState.Connected && !syncing) {
            _authState.value = AuthState.Connected(
                current.accountInfo.copy(
                    isGmailConnected = true,
                    lastSyncedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateGmailConnectionStatus(connected: Boolean) {
        val current = _authState.value
        if (current is AuthState.Connected) {
            _authState.value = AuthState.Connected(
                current.accountInfo.copy(isGmailConnected = connected)
            )
        }
    }

    private fun sanitizeGmailAddress(email: String): String {
        val trimmed = email.trim().lowercase()
        if (!trimmed.contains("@")) return trimmed

        val atIndex = trimmed.indexOf('@')
        var local = trimmed.substring(0, atIndex)
        val domain = trimmed.substring(atIndex + 1)

        if (domain == "gmail.com" || domain == "googlemail.com") {
            local = local.replace(".", "")
            val plusIndex = local.indexOf('+')
            if (plusIndex != -1) {
                local = local.substring(0, plusIndex)
            }
        }

        return "$local@$domain"
    }
}
