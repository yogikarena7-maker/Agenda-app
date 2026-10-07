package com.smartagenda.app.ai

import com.smartagenda.app.util.DateTimeUtils
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
    val confidence: String = "AI",
    val isAmbiguous: Boolean = false,
    val clarification: String? = null
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

        if (GeminiClient.isApiKeyConfigured()) {
            val parsedWithGemini = parseWithGemini(trimmed)
            if (parsedWithGemini != null) {
                return parsedWithGemini
            }
        }

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
              "category": "Work|Personal|Health|Study|Finance|General",
              "notes": "Any additional context or details from input",
              "isAmbiguous": false,
              "clarification": "E.g. 'Is 8:00 AM or 8:00 PM?' or 'Time estimated for afternoon. Please confirm.' or null if unambiguous"
            }
            Note: Set isAmbiguous to true if the time or date is vague, missing, or ambiguous (e.g. 'at 8' without am/pm, 'afternoon' without hour, 'sometime tomorrow', or no time/date provided).
        """.trimIndent()

        val result = GeminiClient.generateContent(
            prompt = prompt,
            systemInstruction = "You are a precise calendar and agenda parsing assistant. You output only raw valid JSON."
        )
        
        if (result.isFailure) {
            throw result.exceptionOrNull() ?: Exception("Unknown Gemini API Error")
        }

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
            val isAmbiguous = json.optBoolean("isAmbiguous", false)
            val clarification = json.optString("clarification", "").ifBlank { null }

            ParsedReminder(
                title = title,
                dateString = date,
                timeString = time,
                category = category,
                notes = notes,
                confidence = "Gemini 3.5",
                isAmbiguous = isAmbiguous,
                clarification = clarification
            )
        }
    }

    fun parseLocally(input: String): ParsedReminder {
        val lower = input.lowercase()
        val today = Calendar.getInstance()
        val targetCal = Calendar.getInstance()

        val category = when {
            lower.contains("#urgent") || lower.contains("urgent") || lower.contains("asap") || lower.contains("emergency") -> "Work"
            lower.contains("#work") || lower.contains("meeting") || lower.contains("sync") || lower.contains("presentation") || lower.contains("deadline") || lower.contains("client") || lower.contains("project") -> "Work"
            lower.contains("#health") || lower.contains("doctor") || lower.contains("gym") || lower.contains("workout") || lower.contains("medicine") || lower.contains("dentist") -> "Health"
            lower.contains("#study") || lower.contains("study") || lower.contains("assignment") || lower.contains("exam") || lower.contains("homework") || lower.contains("reading") -> "Study"
            lower.contains("#finance") || lower.contains("bill") || lower.contains("electricity") || lower.contains("pay") || lower.contains("rent") || lower.contains("salary") -> "Finance"
            lower.contains("#personal") || lower.contains("groceries") || lower.contains("call mom") || lower.contains("family") || lower.contains("buy") -> "Personal"
            else -> "Personal"
        }

        var dateDetected = false
        var isAmbiguous = false
        var clarification: String? = null

        // Relative time in minutes: e.g. "in 30 minutes"
        val inMinMatcher = Pattern.compile("(?i)\\bin\\s+(\\d+)\\s*(?:min|mins|minute|minutes)\\b").matcher(lower)
        // Relative time in hours: e.g. "in 2 hours"
        val inHourMatcher = Pattern.compile("(?i)\\bin\\s+(\\d+)\\s*(?:hour|hours|hr|hrs)\\b").matcher(lower)
        // Ordinal day of month: e.g. "on the 15th" or "on the 1st"
        val onDayMatcher = Pattern.compile("(?i)\\bon\\s+the\\s+(\\d{1,2})(?:st|nd|rd|th)?\\b").matcher(lower)

        var timeString = "09:00"
        var foundTime = false

        if (inMinMatcher.find()) {
            val mins = inMinMatcher.group(1)?.toIntOrNull() ?: 30
            val targetTime = Calendar.getInstance().apply { add(Calendar.MINUTE, mins) }
            timeString = SimpleDateFormat("HH:mm", Locale.US).format(targetTime.time)
            targetCal.time = targetTime.time
            dateDetected = true
            foundTime = true
        } else if (inHourMatcher.find()) {
            val hrs = inHourMatcher.group(1)?.toIntOrNull() ?: 1
            val targetTime = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, hrs) }
            timeString = SimpleDateFormat("HH:mm", Locale.US).format(targetTime.time)
            targetCal.time = targetTime.time
            dateDetected = true
            foundTime = true
        } else if (onDayMatcher.find()) {
            val dayNum = onDayMatcher.group(1)?.toIntOrNull()
            if (dayNum != null && dayNum in 1..31) {
                targetCal.set(Calendar.DAY_OF_MONTH, dayNum)
                if (targetCal.before(today)) {
                    targetCal.add(Calendar.MONTH, 1)
                }
                dateDetected = true
            }
        }

        if (!dateDetected) {
            if (lower.contains("tomorrow")) {
                targetCal.add(Calendar.DAY_OF_YEAR, 1)
                dateDetected = true
            } else if (lower.contains("day after tomorrow")) {
                targetCal.add(Calendar.DAY_OF_YEAR, 2)
                dateDetected = true
            } else if (lower.contains("today") || lower.contains("tonight")) {
                dateDetected = true
            } else {
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
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = dateFormat.format(targetCal.time)

        if (!foundTime) {
            val timeRegex = Pattern.compile("(?i)(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
            val matcher = timeRegex.matcher(input)

            while (matcher.find()) {
                val hourStr = matcher.group(1)
                val minStr = matcher.group(2)
                val amPmStr = matcher.group(3)

                val fullMatch = matcher.group(0).orEmpty()
                if (amPmStr != null || fullMatch.lowercase().contains("at ") || minStr != null) {
                    var hour = hourStr?.toIntOrNull() ?: 9
                    val min = minStr?.toIntOrNull() ?: 0

                    if (amPmStr != null) {
                        if (amPmStr.equals("pm", ignoreCase = true) && hour < 12) hour += 12
                        if (amPmStr.equals("am", ignoreCase = true) && hour == 12) hour = 0
                    } else if (hour in 1..12 && !fullMatch.lowercase().contains(":") && !lower.contains("morning") && !lower.contains("afternoon") && !lower.contains("evening") && !lower.contains("night")) {
                        // Ambiguous hour without AM/PM specified: e.g. "doctor at 8"
                        isAmbiguous = true
                        clarification = "Is this ${String.format(Locale.US, "%02d:%02d", hour, min)} AM or PM? Please confirm."
                        if (hour < 8) hour += 12 // Guess PM for early numbers, AM for 8-11
                    }

                    if (lower.contains("morning") && hour > 12) hour -= 12
                    if ((lower.contains("evening") || lower.contains("night")) && hour < 12) hour += 12

                    timeString = String.format(Locale.US, "%02d:%02d", hour, min)
                    foundTime = true
                    break
                }
            }

            if (!foundTime) {
                if (lower.contains("tonight") || lower.contains("night")) {
                    timeString = "20:00"
                    isAmbiguous = true
                    clarification = "Night scheduled for 8:00 PM. Please confirm."
                } else if (lower.contains("afternoon")) {
                    timeString = "14:00"
                    isAmbiguous = true
                    clarification = "Afternoon estimated at 2:00 PM. Please confirm."
                } else if (lower.contains("morning")) {
                    timeString = "09:00"
                    isAmbiguous = true
                    clarification = "Morning scheduled for 9:00 AM. Please confirm."
                } else if (lower.contains("evening")) {
                    timeString = "18:00"
                    isAmbiguous = true
                    clarification = "Evening scheduled for 6:00 PM. Please confirm."
                } else if (lower.contains("noon")) {
                    timeString = "12:00"
                } else {
                    // No time found at all
                    isAmbiguous = true
                    clarification = if (!dateDetected) {
                        "No date or time specified. Scheduled for today at 9:00 AM."
                    } else {
                        "No time specified. Defaulted to 9:00 AM. Please confirm."
                    }
                }
            }
        }

        var cleanTitle = input
            .replace(Regex("(?i)^remind\\s+me\\s+(?:to\\s+)?"), "")
            .replace(Regex("(?i)^to\\s+"), "")
            .replace(Regex("(?i)\\bin\\s+\\d+\\s*(?:min|mins|minute|minutes|hour|hours|hr|hrs)\\b"), "")
            .replace(Regex("(?i)\\bon\\s+the\\s+\\d{1,2}(?:st|nd|rd|th)?\\b"), "")
            .replace(Regex("(?i)\\b(tomorrow|today|tonight|yesterday)\\b"), "")
            .replace(Regex("(?i)\\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b"), "")
            .replace(Regex("(?i)\\b(every|each)\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday|weekday|day)\\b"), "")
            .replace(Regex("(?i)\\bat\\s+\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?\\b"), "")
            .replace(Regex("(?i)\\b\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)\\b"), "")
            .replace(Regex("(?i)\\b(morning|afternoon|evening|night|noon)\\b"), "")
            .replace(Regex("(?i)#(urgent|work|personal|general|health|study|finance)\\b"), "")
            .trim()

        if (cleanTitle.isBlank()) {
            cleanTitle = input
        } else {
            cleanTitle = cleanTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }

        return ParsedReminder(
            title = cleanTitle,
            dateString = dateString,
            timeString = timeString,
            category = category,
            notes = "Parsed from: \"$input\"",
            confidence = "Smart NLP",
            isAmbiguous = isAmbiguous,
            clarification = clarification
        )
    }
}
