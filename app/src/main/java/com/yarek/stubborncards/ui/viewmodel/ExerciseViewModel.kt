package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.ExerciseRepository
import com.yarek.stubborncards.engine.PromotionEngine
import com.yarek.stubborncards.model.ExerciseConfigRegistry
import com.yarek.stubborncards.model.CardAndProgress
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.logging.Logger
import kotlin.random.Random

/**
 * This viewmodel is used for ordinary exercises.
 * See AiExerciseViewModel for AI powered exercises.
 */
class ExerciseViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = ExerciseRepository(application)

    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: "fresh_mind"
    private val levelParam: String? = savedStateHandle.get<String>("categoryName")

    private val exerciseConfig = if (levelParam != null) {
        ExerciseConfigRegistry.buildSingleCategoryConfig(ProgressLevel.valueOf(levelParam))
    } else {
        if (exerciseId == "recap") ExerciseConfigRegistry.RECAP 
        else ExerciseConfigRegistry.FRESH_MIND
    }

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val cardPool = mutableMapOf<ProgressLevel, ArrayDeque<CardAndProgress>>()
    private var activeCard: CardAndProgress? = null

    private var currentWeights = exerciseConfig.levelWeights.toMutableMap()

    init {
        prefillAndValidateSession()
    }

    private fun prefillAndValidateSession() {
        viewModelScope.launch {
            var totalIneffectiveWeight = 0
            val exhaustedCategories = mutableListOf<ProgressLevel>()

            for ((level, weight) in exerciseConfig.levelWeights) {
                cardPool[level] = ArrayDeque()
                refetchPoolForLevel(level)

                if (cardPool[level].isNullOrEmpty()) {
                    val oldestRecord = repository.peekOldestProgressInLevel(level)
                    if (oldestRecord != null) {
                        val levelConstants = PromotionEngine.getLevelConfig(level)
                        val lastTime = oldestRecord.lastReviewed
                        val elapsedSeconds = 
                            if (lastTime != null) Duration.between(
                                lastTime, LocalDateTime.now(UTC)).seconds
                            else Long.MAX_VALUE

                        if (levelConstants != null 
                            && elapsedSeconds < levelConstants.optimalIntervalSeconds) {
                            totalIneffectiveWeight += weight
                            exhaustedCategories.add(level)
                        }
                    } else {
                        totalIneffectiveWeight += weight
                        exhaustedCategories.add(level)
                    }
                }
            }

            if (totalIneffectiveWeight >= 50) {
                _uiState.value = ExerciseUiState.EffectivenessWarning(
                    message = "Warning: you've reviewed most of words from this exercise. " +
                            "Consider changing the exercise, or taking a rest :)",
                    onProceed = {
                        removeExhaustedWeightsIfConfigured(exhaustedCategories)
                        loadNextCard()
                    }
                )
            } else {
                removeExhaustedWeightsIfConfigured(exhaustedCategories)
                loadNextCard()
            }
        }
    }

    private fun removeExhaustedWeightsIfConfigured(exhausted: List<ProgressLevel>) {
        if (!exerciseConfig.skipExhausted) return
        exhausted.forEach { level -> currentWeights[level] = 0 }
    }

    private suspend fun refetchPoolForLevel(level: ProgressLevel) {
        val levelConstants = PromotionEngine.getLevelConfig(level) ?: return
        val currentQueue = cardPool[level] ?: return

        val minWords = 3;
        val maxWords = 10

        if (currentQueue.size < minWords) {
            val logger = Logger.getLogger("ExerciseViewModel")
            logger.info("Refetching for level $level, current size: ${currentQueue.size}")
            val itemsNeeded = maxWords - currentQueue.size
            val freshBatch = repository.fetchExerciseBatch(
                level = level,
                requiredScore = levelConstants.requiredScore,
                testIntervalSeconds = levelConstants.testIntervalSeconds,
                limit = itemsNeeded
            )
            logger.info("Cards: $freshBatch")
            currentQueue.addAll(freshBatch)

            // Restoring the batch if the elements become available
            if (currentWeights[level] == 0 && freshBatch.isNotEmpty()) {
                val originalWeight = exerciseConfig.levelWeights[level] ?: 0
                currentWeights[level] = originalWeight
            }

        }
    }

    fun submitAnswer(result: PromotionEngine.ReviewResult) {
        val current = activeCard ?: return

        val progressData = current.progress
        if (progressData == null) {
            loadNextCard()
            return
        }

        _uiState.value = ExerciseUiState.Loading

        viewModelScope.launch {
            val updatedProgress = PromotionEngine.gradeCard(progressData, result)
            repository.updateProgressState(updatedProgress)

            val levelConstants = PromotionEngine.getLevelConfig(updatedProgress.level)

            cardPool.values.forEach { queue ->
                queue.removeIf { cardAndProgress ->
                    val isSameCard = cardAndProgress.flashCard.id == current.flashCard.id
                    if (isSameCard) {
                        cardAndProgress.progress = updatedProgress
                        val isLockedOut = updatedProgress.score >= levelConstants.requiredScore
                        isLockedOut
                    } else {
                        false
                    }
                }
            }

            loadNextCard()
        }
    }

    private fun loadNextCard() {
        viewModelScope.launch {
            // This ensures cards whose timers just expired mid-session are instantly loaded.
            currentWeights.keys.forEach { level ->
                refetchPoolForLevel(level)
            }

            val activeLevels = currentWeights.keys.filter { level ->
                val queue = cardPool[level]
                queue != null && queue.isNotEmpty()
            }

            val rolledLevel = rollWeightWheelFromActive(activeLevels)

            if (rolledLevel == null) {
                finishSession()
                return@launch
            }

            val targetQueue = cardPool[rolledLevel]!!
            val nextCardAndProgress = targetQueue.removeFirst()

            activeCard = nextCardAndProgress
            _uiState.value = ExerciseUiState.PresentCard(nextCardAndProgress.flashCard)
        }
    }

    private fun rollWeightWheelFromActive(activeLevels: List<ProgressLevel>): ProgressLevel? {
        if (activeLevels.isEmpty()) return null

        // Calculate total weight sum using only the currently active levels
        val totalWeightSum = activeLevels.sumOf { currentWeights[it] ?: 0 }
        if (totalWeightSum == 0) return activeLevels.randomOrNull()

        val roll = Random.nextInt(0, totalWeightSum)
        var cumulativeSum = 0

        for (level in activeLevels) {
            val weight = currentWeights[level] ?: 0
            cumulativeSum += weight
            if (roll < cumulativeSum) {
                return level
            }
        }
        return activeLevels.firstOrNull()
    }

    private fun finishSession() {
        _uiState.value = ExerciseUiState.Finished(
            "All flashcards in the exercise are already reviewed. Good job!")
    }


    sealed interface ExerciseUiState {
        object Loading : ExerciseUiState
        data class EffectivenessWarning(val message: String, val onProceed: () -> Unit) : ExerciseUiState
        data class PresentCard(val card: FlashCard) : ExerciseUiState
        data class Finished(val summary: String) : ExerciseUiState
    }
}