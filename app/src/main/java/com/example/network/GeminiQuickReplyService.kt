package com.example.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object GeminiQuickReplyService {

    private const val TAG = "GeminiQuickReply"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Analyzes the last message in a conversation and generates 3 contextual quick reply options using Gemini 3.5 Flash.
     */
    suspend fun generateQuickReplies(
        lastMessageText: String,
        senderName: String = "Sender",
        conversationHistory: List<String> = emptyList()
    ): List<String> = withContext(Dispatchers.IO) {
        val trimmedMsg = lastMessageText.trim()
        if (trimmedMsg.isBlank()) {
            return@withContext getDefaultFallbackReplies(trimmedMsg)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is placeholder or empty, using contextual smart fallback replies.")
            return@withContext getDefaultFallbackReplies(trimmedMsg)
        }

        try {
            val contextSnippet = if (conversationHistory.isNotEmpty()) {
                "Recent context:\n" + conversationHistory.takeLast(3).joinToString("\n") + "\n\n"
            } else ""

            val prompt = """
                You are a smart conversational assistant analyzing a chat message.
                $contextSnippet
                The last message received from $senderName is:
                "$trimmedMsg"

                Analyze this message and generate exactly 3 natural, concise, and helpful quick reply suggestions that the recipient could send in response.
                Requirements:
                1. Make them varied in tone (e.g. affirmative/enthusiastic, casual/informative, thoughtful/questioning).
                2. Keep each reply short (under 8 words).
                3. Include a relevant emoji if fitting.
                4. Output MUST be ONLY a JSON array of 3 strings. Example: ["Sounds good! 👍", "I'll check into it.", "Could you clarify?"]
            """.trimIndent()

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                ),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are a specialized smart reply generator. Output only a raw JSON array of 3 strings."))
                )
            )

            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: return@withContext getDefaultFallbackReplies(trimmedMsg)

            parseQuickReplies(responseText, trimmedMsg)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating quick replies from Gemini API: ${e.localizedMessage}", e)
            getDefaultFallbackReplies(trimmedMsg)
        }
    }

    private fun parseQuickReplies(rawResponse: String, originalMessage: String): List<String> {
        val cleaned = rawResponse
            .replace("```json", "")
            .replace("```", "")
            .trim()

        try {
            // Try parsing JSON array directly
            val jsonArray = json.parseToJsonElement(cleaned).jsonArray
            val list = jsonArray.mapNotNull { it.jsonPrimitive.content.trim() }.filter { it.isNotBlank() }
            if (list.size >= 3) {
                return list.take(3)
            } else if (list.isNotEmpty()) {
                val fallbacks = getDefaultFallbackReplies(originalMessage)
                return (list + fallbacks).distinct().take(3)
            }
        } catch (e: Exception) {
            Log.w(TAG, "JSON parsing failed for: $rawResponse, attempting line/bullet regex parsing")
        }

        // Fallback: Line-by-line / bullet points parsing
        val lines = cleaned.lines()
            .map { line ->
                line.trim()
                    .replace(Regex("^[0-9]+[.\\)]\\s*"), "") // "1. ", "1) "
                    .replace(Regex("^[-*•]\\s*"), "")        // "- ", "* ", "• "
                    .replace(Regex("^\"|\"$"), "")            // quotes
                    .trim()
            }
            .filter { it.isNotBlank() && it.length > 1 }

        if (lines.size >= 3) {
            return lines.take(3)
        }

        val fallbacks = getDefaultFallbackReplies(originalMessage)
        return (lines + fallbacks).distinct().take(3)
    }

    /**
     * Provides smart context-aware default replies when offline, key is not set, or API call fails.
     */
    fun getDefaultFallbackReplies(lastMessage: String): List<String> {
        val lower = lastMessage.lowercase()
        return when {
            lower.endsWith("?") || lower.contains("what") || lower.contains("when") || lower.contains("where") || lower.contains("how") -> listOf(
                "Yes, absolutely! 👍",
                "Let me check and get back to you.",
                "Not sure yet, what do you think?"
            )
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> listOf(
                "Hey! How's it going? 👋",
                "Hello! Great to hear from you.",
                "Hey there! What's up?"
            )
            lower.contains("thank") -> listOf(
                "You're welcome! 😊",
                "Anytime! Glad to help.",
                "No problem at all! 👍"
            )
            lower.contains("see you") || lower.contains("bye") || lower.contains("later") -> listOf(
                "See you soon! 👋",
                "Have a great day! ✨",
                "Talk to you later!"
            )
            lower.contains("ready") || lower.contains("done") || lower.contains("updated") || lower.contains("design") -> listOf(
                "Looks great! 🔥",
                "Awesome, let me take a look.",
                "Good work! Let's proceed. 🚀"
            )
            else -> listOf(
                "Sounds great! 👍",
                "Thanks for the update!",
                "Let's discuss this soon."
            )
        }
    }
}
