package com.example.ai

import com.example.data.local.EmailMessageEntity
import org.json.JSONObject

object GeminiEmailAssistant {

    suspend fun classifyEmail(subject: String, body: String): String {
        if (GeminiClient.isApiKeyConfigured()) {
            try {
                val prompt = """
                    Classify the following email into exactly one of these categories:
                    - "Urgent"
                    - "Action Required"
                    - "Needs Reply"
                    - "Informational"
                    - "Newsletter"
                    
                    Subject: $subject
                    Body: $body
                    
                    Return ONLY the category name.
                """.trimIndent()

                val result = GeminiClient.generateContent(
                    prompt = prompt,
                    systemInstruction = "You are an intelligent email classifier. Answer only with one category string."
                )

                val category = result.getOrNull()?.trim()?.replace("\"", "")
                if (!category.isNullOrBlank()) {
                    val match = listOf("Urgent", "Action Required", "Needs Reply", "Informational", "Newsletter")
                        .firstOrNull { it.equals(category, ignoreCase = true) }
                    if (match != null) return match
                }
            } catch (e: Exception) {
                // Fallback to local
            }
        }

        // Local classification heuristic
        val text = "$subject $body".lowercase()
        return when {
            text.contains("urgent") || text.contains("asap") || text.contains("immediately") || text.contains("critical") -> "Urgent"
            text.contains("please review") || text.contains("action required") || text.contains("approve") || text.contains("sign") || text.contains("submit") -> "Action Required"
            text.contains("let me know") || text.contains("thoughts?") || text.contains("confirm") || text.contains("can you") || text.contains("reply") -> "Needs Reply"
            text.contains("unsubscribe") || text.contains("newsletter") || text.contains("digest") || text.contains("weekly update") -> "Newsletter"
            else -> "Informational"
        }
    }

    suspend fun generateDraftReply(
        email: EmailMessageEntity,
        tone: String = "Professional",
        customInstructions: String? = null
    ): String {
        if (GeminiClient.isApiKeyConfigured()) {
            try {
                val prompt = """
                    You are generating a draft email response on behalf of the user.
                    
                    CRITICAL CONSTRAINT: This is a draft for human review and approval. 
                    
                    Email Details:
                    From: ${email.senderName} (${email.senderEmail})
                    Subject: ${email.subject}
                    Original Message:
                    ${email.body}
                    
                    Desired Tone: $tone
                    ${if (!customInstructions.isNullOrBlank()) "Additional User Instructions: $customInstructions" else ""}
                    
                    Instructions:
                    1. Write a direct, complete, and polite response answering the key question or acknowledging the message.
                    2. Maintain a $tone tone.
                    3. Sign off warmly as "Best regards,".
                    4. Do not include subject lines or headers; output only the email body text.
                """.trimIndent()

                val result = GeminiClient.generateContent(
                    prompt = prompt,
                    systemInstruction = "You are an executive email assistant. Provide clean, professional draft replies."
                )

                val draft = result.getOrNull()?.trim()
                if (!draft.isNullOrBlank()) {
                    return draft
                }
            } catch (e: Exception) {
                // Fallback to template
            }
        }

        // Heuristic draft template
        val senderFirstName = email.senderName.split(" ").firstOrNull() ?: "there"
        return when (tone.lowercase()) {
            "concise" -> """
                Hi $senderFirstName,
                
                Thank you for the update. I have reviewed this and everything looks good on my end. Let's proceed as planned.
                
                Best regards,
            """.trimIndent()

            "friendly" -> """
                Hi $senderFirstName,
                
                Thanks so much for reaching out!
                
                I've taken a look at your message regarding "${email.subject}". Happy to help move this forward. Let me know if you need anything else from my side before we wrap this up.
                
                Have a great day!
                Warm regards,
            """.trimIndent()

            "direct" -> """
                $senderFirstName,
                
                Received and noted. I will take the necessary next steps and follow up once completed.
                
                Regards,
            """.trimIndent()

            else -> """
                Dear $senderFirstName,
                
                Thank you for your email regarding "${email.subject}".
                
                I have received your message and will review the details carefully. I will follow up shortly with our feedback and next steps.
                
                Please let me know if there are any urgent items requiring immediate attention.
                
                Best regards,
            """.trimIndent()
        }
    }
}
