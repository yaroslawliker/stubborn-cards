package com.yarek.stubborncards.engine

import com.yarek.stubborncards.config.AppConfigManager
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

data class ProgressLevelConfig(
    val requiredScore: Int,
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

    public fun getPromotionTable(): Map<ProgressLevel, ProgressLevelConfig> {
        return AppConfigManager.getInstance().currentPromotionTable
    }

    public fun getLevelConfig(level: ProgressLevel): ProgressLevelConfig {
        return getPromotionTable()[level] ?: throw IllegalStateException(
            "No config found for level $level")
    }


    fun gradeCard(progress: LearningProgress, result: ReviewResult): LearningProgress {
        val currentLevel = progress.level
        val config = getLevelConfig(currentLevel)
        val now = LocalDateTime.now(UTC)

        when(result) {
            ReviewResult.CORRECT -> {

                if (progress.isOnReview) {
                    progress.isOnReview = false
                } else {
                    val intervalSeconds = if (progress.lastReviewed != null) {
                        Duration.between(progress.lastReviewed, now).seconds
                    } else {
                        config.optimalIntervalSeconds
                    }

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
                        } else {
                            // Enter initial test review blackout state
                            progress.score = config.requiredScore.toFloat()
                        }
                    } else {
                        progress.score = tentativeScore
                    }
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
        ProgressLevel.CLEAN_UP, ProgressLevel.NEW_BATCH -> ProgressLevel.NEW_BATCH
        ProgressLevel.NEW -> ProgressLevel.NEW
    }
}