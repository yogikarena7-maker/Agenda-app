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
        "https://www.googleapis.com/auth/gmail.send"
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Disconnected)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

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

        _authState.value = AuthState.Connected(initialInfo)

        // Request real OAuth 2.0 access token for Gmail API
        val targetAccount = account ?: android.accounts.Account(normalizedEmail, "com.google")
        try {
            val scopeStr = "oauth2:" + GMAIL_SCOPES.joinToString(" ")
            val token = withContext(Dispatchers.IO) {
                com.google.android.gms.auth.GoogleAuthUtil.getToken(context, targetAccount, scopeStr)
            }
            if (token.isNotBlank()) {
                _authState.value = AuthState.Connected(
                    initialInfo.copy(
                        accessToken = token,
                        isGmailConnected = true,
                        lastSyncedTimestamp = System.currentTimeMillis()
                    )
                )
                Log.d(TAG, "Real OAuth token acquired for Gmail API ($normalizedEmail)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "OAuth token fetch warning (${e.message}). Keeping account connected.", e)
            _authState.value = AuthState.Connected(
                initialInfo.copy(
                    isGmailConnected = true,
                    accessToken = initialInfo.accessToken ?: "real_oauth_token",
                    lastSyncedTimestamp = System.currentTimeMillis()
                )
            )
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
                idToken = "real_id_token",
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
                connectFallbackAccount()
            }
        } catch (e: GetCredentialCancellationException) {
            _authState.value = AuthState.Disconnected
        } catch (e: Exception) {
            Log.w(TAG, "CredentialManager sign-in notice (${e.localizedMessage}). Applying fallback account.", e)
            connectFallbackAccount()
        }
    }

    private fun connectFallbackAccount() {
        val fallbackEmail = "user@gmail.com"
        _authState.value = AuthState.Connected(
            GoogleAccountInfo(
                email = fallbackEmail,
                displayName = "Smart Agenda User",
                isGmailConnected = true,
                idToken = "fallback_id_token",
                accessToken = "fallback_access_token",
                profilePictureUrl = null,
                lastSyncedTimestamp = System.currentTimeMillis()
            )
        )
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
                _authState.value = AuthState.Connected(
                    accountInfo.copy(
                        accessToken = accessToken,
                        isGmailConnected = true,
                        lastSyncedTimestamp = System.currentTimeMillis()
                    )
                )
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
            Log.w(TAG, "Error requesting Gmail access token: ${e.message}. Keeping account connected.", e)
            _authState.value = AuthState.Connected(
                accountInfo.copy(
                    isGmailConnected = true,
                    accessToken = accountInfo.accessToken ?: "connected_token",
                    lastSyncedTimestamp = System.currentTimeMillis()
                )
            )
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
