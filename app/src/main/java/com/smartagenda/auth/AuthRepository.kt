package com.smartagenda.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    
    private val serverClientId = "YOUR_BACKEND_WEB_CLIENT_ID.apps.googleusercontent.com"

    suspend fun authenticateWithGoogle(): Result<ZendaAuthSession> = withContext(Dispatchers.IO) {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return@withContext Result.failure(IllegalStateException("Invalid credential type received."))
            }

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            
            val rawEmail = googleIdTokenCredential.id
            val normalizedEmail = sanitizeGmailAddress(rawEmail)
            val idToken = googleIdTokenCredential.idToken 

            val authSession = ZendaAuthSession(
                normalizedEmail = normalizedEmail,
                rawGoogleToken = idToken,
                displayName = googleIdTokenCredential.displayName ?: "Focus Planner"
            )

            Result.success(authSession)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sanitizeGmailAddress(email: String): String {
        val trimmed = email.trim().lowercase()
        if (!trimmed.contains("@")) return trimmed
        
        val parts = trimmed.split("@")
        var localPart = parts[0]
        val domainPart = parts[1]

        if (domainPart == "gmail.com" || domainPart == "googlemail.com") {
            localPart = localPart.replace(".", "")
            val plusIndex = localPart.indexOf('+')
            if (plusIndex != -1) {
                localPart = localPart.substring(0, plusIndex)
            }
        }
        
        return "$localPart@$domainPart"
    }
}

data class ZendaAuthSession(
    val normalizedEmail: String,
    val rawGoogleToken: String,
    val displayName: String
)
