package com.yarek.stubborncards.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

data class AiSentenceResponse(
    val sentences: List<AiCard>
)

data class AiCard(
    val targetWord: String,
    val targetLanguageSentence: String,
    val sentenceTranslation: String
)

sealed class AiGenerationResult {
    data class Success(val data: AiSentenceResponse) : AiGenerationResult()
    object ParsingError : AiGenerationResult()       // Gson failed to parse the LLM output
    object QuotaExceeded : AiGenerationResult()      // Out of tokens / Rate limit hit
    data class UnknownError(val error: Throwable) : AiGenerationResult() // Network issues, API key, etc.
}

class SentenceGenerator {

    private val logger = Logger.getLogger("SentenceGenerator")

    private val gson = Gson()

    /**
     * Generates practice sentences using Gemini.
     * @param apiKey The key the user saved in their settings.
     * @param targetWords List of words they need to practice.
     * @param knownWords List of words they already know.
     * @return A safely parsed Kotlin object containing the sentences.
     */
    suspend fun generatePracticeCards(
        apiKey: String,
        targetWords: List<String>,
        knownWords: List<String>,
        strictContext: Boolean = false
    ): AiGenerationResult = withContext(Dispatchers.IO) {
        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash", // The lightning-fast, cheap model
                apiKey = apiKey,
                generationConfig = generationConfig {
                    // Forces the AI to output pure JSON without markdown formatting
                    responseMimeType = "application/json"
                    // 0.0 is robotic, 1.0 is super creative
                    temperature = 0.7f
                }
            )

            val strictWordsRule = if (strictContext) {
                "The rest of words in a sentence MUST STRICTLY ONLY use words from the Known Words list. DO NOT use any outside vocabulary, except very basic simple words."
            } else {
                "The rest of words in a sentence MUST ONLY use words from the Known Words list, or words and grammar from the level below the target word."
            }

            val targetWordsString = targetWords.joinToString(" - ")
            val knownWordsString = knownWords.joinToString(" - ")

            val prompt = """
                You are a strict language learning assistant. 
                Generate practice sentences for a student.
                You will receive a list of target words with translations in format "word - translation"
                
                RULES:
                1. Generate one sentence for each target word.
                2. Each sentence MUST contain exactly one target word.
                3. The target word's form may differ from the form from the list, so that letter-case, time, gender ect correspond to the language rules and sentence context.
                4. ${strictWordsRule}
                5. Generate translation for each sentence.
                6. DO NOT output any conversational text, greetings, or explanations.
                7. The sentence must be in the same language as a target word, the translation must be in the same language as the word's translation.
                8. YOU MUST RESPOND IN VALID JSON FORMAT MATCHING THIS EXACT SCHEMA:
                {
                  "sentences": [
                    {
                      "targetWord": "...",
                      "targetLanguageSentence": "...",
                      "sentenceTranslation": "..."
                    }
                  ]
                }
                9. "targetWord" field must contain the target word, not the word's translation


                Target words and their translations:
                $targetWordsString

                Known words and their translations:
                $knownWordsString
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            logger.info("AI response ${response.text}");
            val rawJson = response.text ?: throw Exception("AI returned an empty response")

            // This will throw JsonSyntaxException if the LLM breaks the rules
            val parsedData = gson.fromJson(rawJson, AiSentenceResponse::class.java)

            AiGenerationResult.Success(parsedData)

        } catch (e: JsonSyntaxException) {
            // ERROR 1: The LLM hallucinated bad JSON
            AiGenerationResult.ParsingError

        } catch (e: Exception) {
            val errorMessage = e.message?.lowercase() ?: ""

            // ERROR 2: Google returns a 429 error code or "resource exhausted" string when out of tokens
            if (errorMessage.contains("429") || errorMessage.contains("exhausted") || errorMessage.contains("quota")) {
                AiGenerationResult.QuotaExceeded
            } else {
                // ERROR 3: No internet, invalid API key (403 Permission Denied), timeouts, etc.
                AiGenerationResult.UnknownError(e)
            }
        }
    }
}