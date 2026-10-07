package com.smartagenda.app.data.remote

import android.util.Base64
import android.util.Log
import com.smartagenda.app.data.local.EmailDraftEntity
import com.smartagenda.app.data.local.EmailMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GmailApiClient {
    private const val TAG = "GmailApiClient"
    private const val BASE_URL = "https://gmail.googleapis.com/gmail/v1/users/me"

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchInboxMessages(
        accessToken: String,
        maxResults: Int = 10
    ): Result<List<EmailMessageEntity>> = withContext(Dispatchers.IO) {
        try {
            if (accessToken.isBlank()) {
                return@withContext Result.failure(IllegalStateException("No OAuth access token provided"))
            }

            val listUrl = "$BASE_URL/messages?maxResults=$maxResults&q=in:inbox"
            val listRequest = Request.Builder()
                .url(listUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            val listResponse = client.newCall(listRequest).execute()
            val listBody = listResponse.body?.string().orEmpty()

            if (!listResponse.isSuccessful) {
                Log.e(TAG, "Gmail list error: ${listResponse.code} - $listBody")
                return@withContext Result.failure(Exception("Gmail API HTTP ${listResponse.code}"))
            }

            val json = JSONObject(listBody)
            val messagesArray = json.optJSONArray("messages") ?: return@withContext Result.success(emptyList())

            val fetchedEmails = mutableListOf<EmailMessageEntity>()

            for (i in 0 until messagesArray.length()) {
                val item = messagesArray.getJSONObject(i)
                val msgId = item.getString("id")
                val threadId = item.optString("threadId", "")

                val detailUrl = "$BASE_URL/messages/$msgId?format=full"
                val detailRequest = Request.Builder()
                    .url(detailUrl)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .get()
                    .build()

                val detailResp = client.newCall(detailRequest).execute()
                if (!detailResp.isSuccessful) continue

                val detailBodyStr = detailResp.body?.string().orEmpty()
                val msgObj = JSONObject(detailBodyStr)

                val parsed = parseGmailMessageObject(msgId, threadId, msgObj)
                if (parsed != null) {
                    fetchedEmails.add(parsed)
                }
            }

            Result.success(fetchedEmails)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fetchInboxMessages", e)
            Result.failure(e)
        }
    }

    private fun parseGmailMessageObject(
        id: String,
        threadId: String,
        msgObj: JSONObject
    ): EmailMessageEntity? {
        val snippet = msgObj.optString("snippet", "")
        val internalDate = msgObj.optLong("internalDate", System.currentTimeMillis())

        val payload = msgObj.optJSONObject("payload") ?: return null
        val headers = payload.optJSONArray("headers")

        var senderName = "Gmail Sender"
        var senderEmail = "sender@gmail.com"
        var recipientEmail = "me@gmail.com"
        var subject = "(No Subject)"

        if (headers != null) {
            for (h in 0 until headers.length()) {
                val header = headers.getJSONObject(h)
                val name = header.optString("name", "")
                val value = header.optString("value", "")

                when {
                    name.equals("From", ignoreCase = true) -> {
                        val parsedAddr = parseEmailAddress(value)
                        senderName = parsedAddr.first
                        senderEmail = parsedAddr.second
                    }
                    name.equals("To", ignoreCase = true) -> {
                        val parsedAddr = parseEmailAddress(value)
                        recipientEmail = parsedAddr.second
                    }
                    name.equals("Subject", ignoreCase = true) -> {
                        subject = value
                    }
                }
            }
        }

        val bodyText = extractBodyFromPayload(payload) ?: snippet

        // Local classification
        val category = when {
            subject.lowercase().contains("urgent") || bodyText.lowercase().contains("urgent") -> "Urgent"
            subject.lowercase().contains("action") || bodyText.lowercase().contains("action required") -> "Action Required"
            subject.lowercase().contains("reply") || bodyText.lowercase().contains("please confirm") -> "Needs Reply"
            subject.lowercase().contains("digest") || bodyText.lowercase().contains("unsubscribe") -> "Newsletter"
            else -> "Informational"
        }

        return EmailMessageEntity(
            id = id,
            threadId = threadId,
            senderName = senderName,
            senderEmail = senderEmail,
            recipientEmail = recipientEmail,
            subject = subject,
            snippet = snippet,
            body = bodyText,
            receivedEpochMillis = internalDate,
            category = category,
            isRead = false,
            isStarred = false
        )
    }

    private fun extractBodyFromPayload(payload: JSONObject): String? {
        val bodyObj = payload.optJSONObject("body")
        val data = bodyObj?.optString("data")
        if (!data.isNullOrBlank()) {
            return decodeBase64Url(data)
        }

        val parts = payload.optJSONArray("parts")
        if (parts != null) {
            for (p in 0 until parts.length()) {
                val part = parts.getJSONObject(p)
                val mimeType = part.optString("mimeType", "")
                if (mimeType.equals("text/plain", ignoreCase = true)) {
                    val pData = part.optJSONObject("body")?.optString("data")
                    if (!pData.isNullOrBlank()) {
                        return decodeBase64Url(pData)
                    }
                }
            }
        }
        return null
    }

    private fun decodeBase64Url(base64Url: String): String {
        return try {
            val clean = base64Url.replace("-", "+").replace("_", "/")
            String(Base64.decode(clean, Base64.DEFAULT), Charsets.UTF_8)
        } catch (e: Exception) {
            base64Url
        }
    }

    private fun parseEmailAddress(raw: String): Pair<String, String> {
        return try {
            if (raw.contains("<") && raw.contains(">")) {
                val name = raw.substringBefore("<").trim().replace("\"", "")
                val email = raw.substringAfter("<").substringBefore(">").trim()
                Pair(if (name.isBlank()) email else name, email)
            } else {
                Pair(raw, raw)
            }
        } catch (e: Exception) {
            Pair(raw, raw)
        }
    }

    suspend fun sendEmailReply(
        accessToken: String,
        draft: EmailDraftEntity,
        originalThreadId: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (accessToken.isBlank()) {
                return@withContext Result.failure(IllegalStateException("No OAuth access token for sending"))
            }

            val rawMime = buildRawMimeEmail(
                toEmail = draft.recipientEmail,
                toName = draft.recipientName,
                subject = draft.subject,
                bodyText = draft.draftBody
            )

            val base64UrlMime = Base64.encodeToString(rawMime.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)

            val requestJson = JSONObject()
            requestJson.put("raw", base64UrlMime)
            if (!originalThreadId.isNullOrBlank()) {
                requestJson.put("threadId", originalThreadId)
            }

            val sendUrl = "$BASE_URL/messages/send"
            val request = Request.Builder()
                .url(sendUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Gmail send error ${response.code}: $respBody")
                return@withContext Result.failure(Exception("Gmail send failed HTTP ${response.code}"))
            }

            Log.d(TAG, "Email successfully sent via Gmail API: $respBody")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in sendEmailReply", e)
            Result.failure(e)
        }
    }

    suspend fun createDraftInGmail(
        accessToken: String,
        draft: EmailDraftEntity,
        originalThreadId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (accessToken.isBlank()) {
                return@withContext Result.failure(IllegalStateException("No OAuth access token for creating draft"))
            }

            val rawMime = buildRawMimeEmail(
                toEmail = draft.recipientEmail,
                toName = draft.recipientName,
                subject = draft.subject,
                bodyText = draft.draftBody
            )

            val base64UrlMime = Base64.encodeToString(rawMime.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)

            val messageObj = JSONObject()
            messageObj.put("raw", base64UrlMime)
            if (!originalThreadId.isNullOrBlank()) {
                messageObj.put("threadId", originalThreadId)
            }

            val requestJson = JSONObject()
            requestJson.put("message", messageObj)

            val draftUrl = "$BASE_URL/drafts"
            val request = Request.Builder()
                .url(draftUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Gmail create draft error ${response.code}: $respBody")
                return@withContext Result.failure(Exception("Gmail draft creation failed HTTP ${response.code}"))
            }

            val respJson = JSONObject(respBody)
            val draftId = respJson.optString("id", "")
            Log.d(TAG, "Draft successfully created in Gmail: $draftId")
            Result.success(draftId)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in createDraftInGmail", e)
            Result.failure(e)
        }
    }

    private fun buildRawMimeEmail(
        toEmail: String,
        toName: String,
        subject: String,
        bodyText: String
    ): String {
        return buildString {
            append("To: $toName <$toEmail>\r\n")
            append("Subject: $subject\r\n")
            append("Content-Type: text/plain; charset=UTF-8\r\n")
            append("MIME-Version: 1.0\r\n\r\n")
            append(bodyText)
        }
    }
}
