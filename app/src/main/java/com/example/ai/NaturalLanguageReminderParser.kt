package com.example.ai

import com.example.util.DateTimeUtils
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedReminder(
    val title: String,
    val dateString: String,
    val timeString: String,
    val category: String,
    val notes: String = "",
    val confidence: String = "AI" // "Gemini" or "Local NLP"
)

object NaturalLanguageReminderParser {

    suspend fun parse(input: String): ParsedReminder {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return ParsedReminder(
                title = "New Reminder",
                dateString = DateTimeUtils.getTodayDateString(),
                timeString = "09:00",
                category = "General",
                confidence = "Default"
            )
        }

        // Try Gemini AI if API key is configured
        if (GeminiClient.isApiKeyConfigured()) {
            try {
                val parsedWithGemini = parseWithGemini(trimmed)
                if (parsedWithGemini != null) {
                    return parsedWithGemini
                }
            } catch (e: Exception) {
                // Fallback to local heuristic parser
            }
        }

        // Offline Smart Heuristic Fallback
        return parseLocally(trimmed)
    }

    private suspend fun parseWithGemini(input: String): ParsedReminder? {
        val todayStr = DateTimeUtils.getTodayDateString()
        val currentTime = DateTimeUtils.getCurrentTimeString()
        val calendar = Calendar.getInstance()
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.US).format(calendar.time)

        val prompt = """
            Context:
            - Current Date: $todayStr ($dayOfWeek)
            - Current Time: $currentTime (24h)
            
            Task:
            Parse the following user text into a scheduled agenda reminder:
            "$input"
            
            Return strictly valid JSON with this exact schema (no markdown, no backticks, no explanations):
            {
              "title": "Clean concise task title without reminder phrases",
              "date": "yyyy-MM-dd",
              "time": "HH:mm",
              "category": "Work|Personal|Urgent|General",
              "notes": "Any additional context or details from input"
            }
        """.trimIndent()

        val result = GeminiClient.generateContent(
            prompt = prompt,
            systemInstruction = "You are a precise calendar and agenda parsing assistant. You output only raw valid JSON."
        )

        return result.getOrNull()?.let { raw ->
            val jsonStr = raw.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(jsonStr)
            val title = json.optString("title", "").ifBlank { input }
            val date = json.optString("date", todayStr).ifBlank { todayStr }
            val time = json.optString("time", "09:00").ifBlank { "09:00" }
            val category = json.optString("category", "General").ifBlank { "General" }
            val notes = json.optString("notes", "")

            ParsedReminder(
                title = title,
                dateString = date,
                timeString = time,
                category = category,
                notes = notes,
                confidence = "Gemini 3.5"
            )
        }
    }

    fun parseLocally(input: String): ParsedReminder {
        val lower = input.lowercase()
        val today = Calendar.getInstance()
        val targetCal = Calendar.getInstance()

        // 1. Detect Category
        val category = when {
            lower.contains("#urgent") || lower.contains("urgent") || lower.contains("asap") || lower.contains("emergency") -> "Urgent"
            lower.contains("#work") || lower.contains("meeting") || lower.contains("sync") || lower.contains("presentation") || lower.contains("deadline") || lower.contains("client") || lower.contains("project") -> "Work"
            lower.contains("#personal") || lower.contains("doctor") || lower.contains("gym") || lower.contains("groceries") || lower.contains("call mom") || lower.contains("family") || lower.contains("buy") -> "Personal"
            else -> "General"
        }

        // 2. Detect Date
        var dateDetected = false
        if (lower.contains("tomorrow")) {
            targetCal.add(Calendar.DAY_OF_YEAR, 1)
            dateDetected = true
        } else if (lower.contains("day after tomorrow")) {
            targetCal.add(Calendar.DAY_OF_YEAR, 2)
            dateDetected = true
        } else if (lower.contains("today") || lower.contains("tonight")) {
            dateDetected = true
        } else {
            // Check day of week (e.g. "on friday", "next monday")
            val days = listOf(
                "sunday" to Calendar.SUNDAY,
                "monday" to Calendar.MONDAY,
                "tuesday" to Calendar.TUESDAY,
                "wednesday" to Calendar.WEDNESDAY,
                "thursday" to Calendar.THURSDAY,
                "friday" to Calendar.FRIDAY,
                "saturday" to Calendar.SATURDAY
            )
            for ((dayName, dayConstant) in days) {
                if (lower.contains(dayName)) {
                    val currentDay = today.get(Calendar.DAY_OF_WEEK)
                    var daysToAdd = dayConstant - currentDay
                    if (daysToAdd <= 0) daysToAdd += 7
                    targetCal.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    dateDetected = true
                    break
                }
            }
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = dateFormat.format(targetCal.time)

        // 3. Detect Time
        var timeString = "09:00"
        val timeRegex = Pattern.compile("(?i)(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        val matcher = timeRegex.matcher(input)

        var foundTime = false
        while (matcher.find()) {
            val hourStr = matcher.group(1)
            val minStr = matcher.group(2)
            val amPmStr = matcher.group(3)

            // If it matched a number with am/pm or preceded by "at"
            val fullMatch = matcher.group(0).orEmpty()
            if (amPmStr != null || fullMatch.lowercase().contains("at ") || minStr != null) {
                var hour = hourStr?.toIntOrNull() ?: 9
                val min = minStr?.toIntOrNull() ?: 0
                if (amPmStr != null) {
                    if (amPmStr.equals("pm", ignoreCase = true) && hour < 12) hour += 12
                    if (amPmStr.equals("am", ignoreCase = true) && hour == 12) hour = 0
                }
                timeString = String.format(Locale.US, "%02d:%02d", hour, min)
                foundTime = true
                break
            }
        }

        if (!foundTime) {
            if (lower.contains("tonight")) {
                timeString = "20:00"
            } else if (lower.contains("afternoon")) {
                timeString = "14:00"
            } else if (lower.contains("morning")) {
                timeString = "09:00"
            } else if (lower.contains("evening")) {
                timeString = "18:00"
            } else if (lower.contains("noon")) {
                timeString = "12:00"
            }
        }

        // 4. Clean Title
        var cleanTitle = input
            .replace(Regex("(?i)^remind\\s+me\\s+(?:to\\s+)?"), "")
            .replace(Regex("(?i)^to\\s+"), "")
            .replace(Regex("(?i)\\b(tomorrow|today|tonight|yesterday)\\b"), "")
            .replace(Regex("(?i)\\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b"), "")
            .replace(Regex("(?i)\\bat\\s+\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?\\b"), "")
            .replace(Regex("(?i)\\b\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)\\b"), "")
            .replace(Regex("(?i)#(urgent|work|personal|general)\\b"), "")
            .trim()

        if (cleanTitle.isBlank()) {
            cleanTitle = input
        } else {
            // Capitalize first letter
            cleanTitle = cleanTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }

        return ParsedReminder(
            title = cleanTitle,
            dateString = dateString,
            timeString = timeString,
            category = category,
            notes = "Parsed from: \"$input\"",
            confidence = "Smart NLP"
        )
    }
}
