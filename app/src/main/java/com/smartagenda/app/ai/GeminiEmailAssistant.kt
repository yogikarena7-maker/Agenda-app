package com.smartagenda.app.ai

import com.smartagenda.app.data.local.EmailMessageEntity

object GeminiEmailAssistant {

    val SUPPORTED_TONES = listOf(
        "Professional",
        "Friendly",
        "Short & Direct",
        "Polite",
        "Warm",
        "Casual",
        "Confident",
        "Firm",
        "Detailed"
    )

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
                // Fallback to local regex matching
            }
        }

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
                    Do not invent fake facts, external links, promises, qualifications, or non-existent attachments.
                    
                    Email Details:
                    From: ${email.senderName} (${email.senderEmail})
                    Subject: ${email.subject}
                    Original Message:
                    ${email.body.take(1500)}
                    
                    Desired Mood/Tone: $tone
                    ${if (!customInstructions.isNullOrBlank()) "Additional User Instructions: $customInstructions" else ""}
                    
                    Tone Guidelines:
                    - "Friendly": Warm, cheerful, approachable language.
                    - "Professional": Formal, polished, business-appropriate executive tone.
                    - "Short & Direct": Concise, clear, single-paragraph response answering key points directly.
                    - "Polite": Courteous, highly respectful, considerate phrasing.
                    - "Warm": Empathetic, caring, welcoming tone.
                    - "Casual": Relaxed, informal conversational language.
                    - "Confident": Decisive, self-assured, proactive phrasing.
                    - "Firm": Clear boundaries, assertive, non-ambiguous posture.
                    - "Detailed": Comprehensive, structured, line-by-line coverage of points.
                    
                    Output Instructions:
                    Write ONLY the response email body. Do not include Subject line headers.
                """.trimIndent()

                val result = GeminiClient.generateContent(
                    prompt = prompt,
                    systemInstruction = "You are Smart Agenda's AI email assistant. Provide clean, tone-accurate draft replies."
                )

                val draft = result.getOrNull()?.trim()
                if (!draft.isNullOrBlank()) {
                    return draft
                }
            } catch (e: Exception) {
                // Fallback to local template
            }
        }

        val senderFirstName = email.senderName.split(" ").firstOrNull() ?: "there"
        return generateLocalFallbackDraft(senderFirstName, email.subject, tone)
    }

    private fun generateLocalFallbackDraft(senderName: String, subject: String, tone: String): String {
        return when (tone) {
            "Friendly" -> """
                Hi $senderName,
                
                Thanks so much for reaching out!
                
                I've reviewed your message regarding "$subject" and would love to move forward with this. Let me know if you need anything else from my side!
                
                Warm regards,
            """.trimIndent()

            "Short & Direct" -> """
                Hi $senderName,
                
                Received and approved. I'll take care of the next steps and confirm once completed.
                
                Best,
            """.trimIndent()

            "Polite" -> """
                Dear $senderName,
                
                Thank you kindly for your message regarding "$subject".
                
                I have reviewed the details and appreciate you sharing this update. Please let me know if I can assist with any further information.
                
                With sincere regards,
            """.trimIndent()

            "Warm" -> """
                Hi $senderName,
                
                Hope you're having a wonderful day!
                
                Thank you for sending over the details regarding "$subject". I'm happy to assist and look forward to collaborating on this.
                
                Warmly,
            """.trimIndent()

            "Casual" -> """
                Hey $senderName,
                
                Got your message about "$subject". Looks good to me! I'll dive into this soon and loop back with you.
                
                Cheers,
            """.trimIndent()

            "Confident" -> """
                Hello $senderName,
                
                Thank you for the update on "$subject".
                
                Our strategy is aligned and we are fully prepared to execute the next phase. I will ensure all action items are completed on schedule.
                
                Best regards,
            """.trimIndent()

            "Firm" -> """
                Hello $senderName,
                
                Regarding "$subject", our current priorities remain focused on our agreed deliverables. We will evaluate any adjustments after the upcoming milestone signoff.
                
                Regards,
            """.trimIndent()

            "Detailed" -> """
                Dear $senderName,
                
                Thank you for your detailed note regarding "$subject".
                
                I have carefully reviewed the proposal and noted the following key points:
                1. Alignment with our upcoming sprint milestones.
                2. Confirmation of resource allocation and dependencies.
                3. Verification of technical & operational requirements.
                
                I will follow up shortly with a comprehensive breakdown.
                
                Best regards,
            """.trimIndent()

            else -> """
                Dear $senderName,
                
                Thank you for your email regarding "$subject".
                
                I have received your message and will review the details carefully. I will follow up shortly with our feedback and next steps.
                
                Best regards,
            """.trimIndent()
        }
    }
}
