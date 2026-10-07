package com.smartagenda.app.ai

import android.content.Context
import android.util.Log
import com.smartagenda.app.BuildConfig
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
    private val CANDIDATE_MODELS = listOf(
        "gemini-3.5-flash",
        "gemini-3.5-flash-lite",
        "gemini-flash-latest",
        "gemini-3-flash-preview"
    )
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    // Default key fallback (empty by default; configured via local.properties or in-app Settings)
    private const val DEFAULT_API_KEY = ""
    private var customApiKey: String? = null

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun setCustomApiKey(key: String?) {
        customApiKey = key?.trim()?.ifBlank { null }
    }

    fun getApiKey(context: Context? = null): String {
        // 1. In-memory custom key if set
        if (!customApiKey.isNullOrBlank()) {
            return customApiKey!!
        }

        // 2. Saved user key from SharedPreferences if available
        if (context != null) {
            try {
                val prefs = context.getSharedPreferences("smart_agenda_gemini_prefs", Context.MODE_PRIVATE)
                val savedKey = prefs.getString("gemini_api_key", null)
                if (!savedKey.isNullOrBlank() && !savedKey.contains("MY_GEMINI_API_KEY")) {
                    return savedKey.trim()
                }
            } catch (_: Exception) {}
        }

        // 3. Compiled BuildConfig key from Gradle
        try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && !buildKey.contains("MY_GEMINI_API_KEY")) {
                return buildKey.trim()
            }
        } catch (_: Throwable) {}

        // 4. Default active project key
        return DEFAULT_API_KEY
    }

    fun isApiKeyConfigured(context: Context? = null): Boolean {
        val key = getApiKey(context)
        return key.isNotBlank() && !key.contains("MY_GEMINI_API_KEY")
    }

    suspend fun testConnection(context: Context? = null): Result<String> {
        return generateContent(
            prompt = "Respond with 'Connected' in 1 word.",
            systemInstruction = "You are a test ping responder. Output only 'Connected'.",
            context = context
        )
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        context: Context? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)

        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured. Please add your key in Settings or local.properties."))
        }

        var lastError: Exception? = null

        // Try candidate flash models in order for 100% resilient failover
        for (modelName in CANDIDATE_MODELS) {
            try {
                val url = "$BASE_URL/$modelName:generateContent?key=$apiKey"

                val rootJson = JSONObject()
                val contentsArray = JSONArray()
                val userContent = JSONObject()
                val partsArray = JSONArray()
                val textPart = JSONObject().put("text", prompt)
                partsArray.put(textPart)
                userContent.put("parts", partsArray)
                contentsArray.put(userContent)
                rootJson.put("contents", contentsArray)

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
                    Log.w(TAG, "Gemini model $modelName returned HTTP ${response.code}: $bodyString. Trying next candidate...")
                    lastError = Exception("Gemini ($modelName) HTTP ${response.code}: $bodyString")
                    continue
                }

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val resParts = contentObj?.optJSONArray("parts")
                    if (resParts != null && resParts.length() > 0) {
                        val textBuilder = StringBuilder()
                        for (i in 0 until resParts.length()) {
                            val partObj = resParts.getJSONObject(i)
                            // Filter out internal reasoning/thought tokens if present
                            if (!partObj.optBoolean("thought", false)) {
                                val t = partObj.optString("text", "")
                                if (t.isNotEmpty()) textBuilder.append(t)
                            }
                        }
                        val extracted = textBuilder.toString().ifBlank {
                            resParts.getJSONObject(0).optString("text", "")
                        }
                        if (extracted.isNotBlank()) {
                            return@withContext Result.success(extracted.trim())
                        }
                    }
                }

                lastError = Exception("No valid candidate text returned by $modelName")
            } catch (e: Exception) {
                Log.w(TAG, "Error executing model $modelName: ${e.message}. Trying next candidate...")
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("All candidate Gemini models failed to generate a response"))
    }
}
