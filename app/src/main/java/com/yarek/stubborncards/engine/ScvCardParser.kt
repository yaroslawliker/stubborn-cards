package com.yarek.stubborncards.engine

import com.yarek.stubborncards.model.CardAndProgress
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

/**
 * A mapping utility.
 * It converts card and progress to CSV String Lists (Export),
 * and CSV to cards progress (Import).
 */
class CsvParser {

    companion object {
        const val HEADER_WORD = "word"
        const val HEADER_TRANSLATION = "translation"
        const val HEADER_LEVEL = "level"
        const val HEADER_SCORE = "score"
        const val HEADER_IS_ON_REVIEW = "is_on_review"
        const val HEADER_LAST_REVIEWED = "last_reviewed"
    }

    // ==========================================
    // EXPORT LOGIC
    // ==========================================

    /**
     * Generates the header row based on user configuration.
     */
    fun getHeaders(includeProgress: Boolean): List<String> {
        return if (includeProgress) {
            listOf(
                HEADER_WORD, HEADER_TRANSLATION, HEADER_LEVEL,
                HEADER_SCORE, HEADER_IS_ON_REVIEW, HEADER_LAST_REVIEWED
            )
        } else {
            listOf(HEADER_WORD, HEADER_TRANSLATION)
        }
    }

    /**
     * Converts a single CardAndProgress entity into a CSV row (List of Strings).
     */
    fun formatExportRow(card: CardAndProgress, includeProgress: Boolean): List<String> {
        val baseRow = mutableListOf(
            card.flashCard.word,
            card.flashCard.translation
        )

        if (includeProgress) {
            val progress = card.progress
            baseRow.add(progress?.level?.name ?: ProgressLevel.NEW.name)
            baseRow.add(progress?.score?.toString() ?: "0.0")
            baseRow.add(progress?.isOnReview?.toString() ?: "false")
            baseRow.add(progress?.lastReviewed?.toString() ?: "") // Standard ISO-8601 string
        }

        return baseRow
    }

    // ==========================================
    // IMPORT LOGIC
    // ==========================================

    /**
     * Maps a single CSV row (represented as a Map of Header -> Value) back into cards and progress.
     * Applies fallback rules defined in the ImportConfig.
     */
    fun parseImportRow(
        row: Map<String, String>,
        defaultLevel: ProgressLevel,
        overrideAllLevels: Boolean,
        setMissingDatesToNow: Boolean
    ): CardAndProgress? {

        // Extract required fields
        val word = row[HEADER_WORD]?.trim()
        val translation = row[HEADER_TRANSLATION]?.trim()

        if (word.isNullOrEmpty() || translation.isNullOrEmpty()) {
            return null // Skip invalid rows completely
        }

        val flashCard = FlashCard(word, translation)

        // Parse Level
        val levelString = row[HEADER_LEVEL]?.trim()?.uppercase()
        val finalLevel = if (overrideAllLevels) {
            defaultLevel
        } else {
            try {
                if (!levelString.isNullOrEmpty()) ProgressLevel.valueOf(levelString) else defaultLevel
            } catch (e: IllegalArgumentException) {
                defaultLevel
            }
        }

        // Parse Score and Review Status
        val score = row[HEADER_SCORE]?.toFloatOrNull() ?: 0f
        val isOnReview = row[HEADER_IS_ON_REVIEW]?.toBooleanStrictOrNull() ?: false

        // Parse Date
        val dateString = row[HEADER_LAST_REVIEWED]?.trim()
        val finalDate = parseDate(dateString, setMissingDatesToNow)

        val progress = LearningProgress(
            score,
            finalLevel,
            isOnReview,
            finalDate
        )

        return CardAndProgress(flashCard, progress)
    }

    private fun parseDate(dateString: String?, setToNowIfMissing: Boolean): LocalDateTime? {
        if (dateString.isNullOrEmpty()) {
            return if (setToNowIfMissing) LocalDateTime.now() else null
        }

        return try {
            LocalDateTime.parse(dateString)
        } catch (e: DateTimeParseException) {
            if (setToNowIfMissing) LocalDateTime.now() else null
        }
    }
}