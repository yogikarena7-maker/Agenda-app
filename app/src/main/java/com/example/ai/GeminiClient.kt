package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && !key.contains("MY_GEMINI_API_KEY")
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }

            if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
                return@withContext Result.failure(IllegalStateException("Gemini API key is not configured in Secrets"))
            }

            try {
                val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"

                val rootJson = JSONObject()

                // Contents array
                val contentsArray = JSONArray()
                val userContent = JSONObject()
                val partsArray = JSONArray()
                val textPart = JSONObject().put("text", prompt)
                partsArray.put(textPart)
                userContent.put("parts", partsArray)
                contentsArray.put(userContent)
                rootJson.put("contents", contentsArray)

                // Optional system instruction
                if (!systemInstruction.isNullOrBlank()) {
                    val sysContent = JSONObject()
                    val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
                    sysContent.put("parts", sysParts)
                    rootJson.put("systemInstruction", sysContent)
                }

                val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val bodyString = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API error: ${response.code} - $bodyString")
                    return@withContext Result.failure(Exception("Gemini API error ${response.code}: $bodyString"))
                }

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val resParts = contentObj?.optJSONArray("parts")
                    if (resParts != null && resParts.length() > 0) {
                        val text = resParts.getJSONObject(0).optString("text", "")
                        return@withContext Result.success(text)
                    }
                }

                Result.failure(Exception("No candidate content received from Gemini"))
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Gemini generateContent", e)
                Result.failure(e)
            }
        }
}
