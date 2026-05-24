package com.yarek.stubborncards.engine

import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import java.time.Duration
import java.time.LocalDateTime

data class ProgressLevelConfig(
    val requiredScore: Float,
    val optimalIntervalSeconds: Long,
    val testIntervalSeconds: Long
)

object PromotionEngine {

    // Action enum representing the user's explicit interactive card feedback
    enum class ReviewResult {
        CORRECT,
        ALMOST_CORRECT,
        WRONG
    }

    // Default config values mapping
    val defaultProfileJson = """
        {
          "NEW":         {"requiredScore": 5.0, "optimalIntervalSeconds": 20,    "testIntervalSeconds": 300},
          "NEW_BATCH":   {"requiredScore": 5.0, "optimalIntervalSeconds": 20,    "testIntervalSeconds": 300},
          "CLEAN_UP":    {"requiredScore": 5.0, "optimalIntervalSeconds": 600,   "testIntervalSeconds": 64800},
          "KNOWN":       {"requiredScore": 8.0, "optimalIntervalSeconds": 172800,"testIntervalSeconds": 604800},
          "LEARNED":     {"requiredScore": 4.0, "optimalIntervalSeconds": 3888000,"testIntervalSeconds": 15552000},
          "MASTERED":    {"requiredScore": 999.0,"optimalIntervalSeconds": 31536000,"testIntervalSeconds": 31536000}
        }
    """.trimIndent()

    val parsedConfig: Map<ProgressLevel, ProgressLevelConfig> = mapOf(
        ProgressLevel.NEW to ProgressLevelConfig(5f, 20, 300),
        ProgressLevel.NEW_BATCH to ProgressLevelConfig(5f, 20, 300),
        ProgressLevel.CLEAN_UP to ProgressLevelConfig(5f, 600, 64800), // 18 Hours
        ProgressLevel.KNOWN to ProgressLevelConfig(8f, 172800, 604800), // 2 days / 7 days
        ProgressLevel.LEARNED to ProgressLevelConfig(4f, 3888000, 15552000), // 1.5 Months / 6 Months
        ProgressLevel.MASTERED to ProgressLevelConfig(999f, 31536000, 31536000)
    )

    fun gradeCard(progress: LearningProgress, result: ReviewResult): LearningProgress {
        val currentLevel = progress.level
        val config = parsedConfig[currentLevel]!!
        val now = LocalDateTime.now()

        when(result) {
            ReviewResult.CORRECT -> {
                val intervalSeconds =
                    if (progress.lastReviewed != null) Duration.between(progress.lastReviewed, now).seconds
                    else config.optimalIntervalSeconds

                // Score tracking assignment using the dynamic s(interval) mathematical optimization rule
                val scoreIncrement = if (intervalSeconds < config.optimalIntervalSeconds) {
                    intervalSeconds.toFloat() / config.optimalIntervalSeconds.toFloat()
                } else {
                    1.0f
                }

                val tentativeScore = progress.score + scoreIncrement

                if (tentativeScore >= config.requiredScore) {
                    // If the card hits the required score threshold while on a test state, promote it
                    if (progress.score >= config.requiredScore) {
                        progress.level = getNextLevel(currentLevel)
                        progress.score = 0f
                        progress.isOnReview = false
                    } else {
                        // Enter initial test review blackout state
                        progress.score = config.requiredScore
                    }
                } else {
                    progress.score = tentativeScore
                }
            }
            ReviewResult.WRONG -> {
                // Demotion Logic rules execution engine
                if (currentLevel == ProgressLevel.NEW || currentLevel == ProgressLevel.NEW_BATCH || currentLevel == ProgressLevel.CLEAN_UP) {
                    val intervalSeconds = if (progress.lastReviewed != null) Duration.between(progress.lastReviewed, now).seconds else 0
                    val penalty = if (intervalSeconds < (config.testIntervalSeconds / 2)) 1.0f else 0.5f
                    progress.score = (progress.score - penalty).coerceAtLeast(0f)
                } else {
                    if (progress.isOnReview) {
                        // Demote down a tier if missed while already on active technical review status
                        progress.level = getPreviousLevel(currentLevel)
                        progress.score = 0f
                        progress.isOnReview = false
                    } else {
                        progress.isOnReview = true
                        progress.score = 0f
                    }
                }
            }
            ReviewResult.ALMOST_CORRECT -> { /* The time is updated before return */ }
        }

        progress.lastReviewed = now
        return progress
    }

    private fun getNextLevel(level: ProgressLevel): ProgressLevel = when(level) {
        ProgressLevel.NEW, ProgressLevel.NEW_BATCH -> ProgressLevel.CLEAN_UP
        ProgressLevel.CLEAN_UP -> ProgressLevel.KNOWN
        ProgressLevel.KNOWN -> ProgressLevel.LEARNED
        ProgressLevel.LEARNED, ProgressLevel.MASTERED -> ProgressLevel.MASTERED
    }

    private fun getPreviousLevel(level: ProgressLevel): ProgressLevel = when(level) {
        ProgressLevel.MASTERED -> ProgressLevel.LEARNED
        ProgressLevel.LEARNED -> ProgressLevel.KNOWN
        ProgressLevel.KNOWN -> ProgressLevel.CLEAN_UP
        ProgressLevel.CLEAN_UP, ProgressLevel.NEW_BATCH, ProgressLevel.NEW -> ProgressLevel.NEW_BATCH
    }
}