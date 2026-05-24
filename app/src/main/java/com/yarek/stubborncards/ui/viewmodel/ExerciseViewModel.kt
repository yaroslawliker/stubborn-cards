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
import kotlin.random.Random

class ExerciseViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = ExerciseRepository(application)

    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: "fresh_mind"
    private val levelParam: String? = savedStateHandle.get<String>("categoryName")

    private val config = if (levelParam != null) {
        ExerciseConfigRegistry.buildSingleCategoryConfig(ProgressLevel.valueOf(levelParam))
    } else {
        if (exerciseId == "recap") ExerciseConfigRegistry.RECAP else ExerciseConfigRegistry.FRESH_MIND
    }

    private val _uiState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val cardPool = mutableMapOf<ProgressLevel, ArrayDeque<CardAndProgress>>()
    private var activeCard: CardAndProgress? = null

    private var adjustedWeights = config.levelWeights.toMutableMap()

    init {
        prefillAndValidateSession()
    }

    private fun prefillAndValidateSession() {
        viewModelScope.launch {
            var totalIneffectiveWeight = 0
            val exhaustedCategories = mutableListOf<ProgressLevel>()

            for ((level, weight) in config.levelWeights) {
                cardPool[level] = ArrayDeque()
                refetchPoolForLevel(level)

                if (cardPool[level].isNullOrEmpty()) {
                    val oldestRecord = repository.peekOldestProgressInLevel(level)
                    if (oldestRecord != null) {
                        val config = PromotionEngine.parsedConfig[level]
                        val lastTime = oldestRecord.lastReviewed
                        val elapsedSeconds = if (lastTime != null) Duration.between(lastTime, LocalDateTime.now()).seconds else Long.MAX_VALUE

                        if (config != null && elapsedSeconds < config.optimalIntervalSeconds) {
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
                    message = "Warning: you've reviewed all words from most of the categories in " +
                            "this exercise. Consider changing the exercise, or taking a rest :)",
                    onProceed = {
                        rebalanceWeights(exhaustedCategories)
                        loadNextCard()
                    }
                )
            } else {
                rebalanceWeights(exhaustedCategories)
                loadNextCard()
            }
        }
    }

    private fun rebalanceWeights(exhausted: List<ProgressLevel>) {
        if (!config.skipIneffective) return
        exhausted.forEach { adjustedWeights.remove(it) }
    }

    private suspend fun refetchPoolForLevel(level: ProgressLevel) {
        val config = PromotionEngine.parsedConfig[level] ?: return
        val currentQueue = cardPool[level] ?: return

        if (currentQueue.size < 3) {
            val itemsNeeded = 10 - currentQueue.size
            val freshBatch = repository.fetchExerciseBatch(
                level = level,
                requiredScore = config.requiredScore,
                testIntervalSeconds = config.testIntervalSeconds,
                limit = itemsNeeded
            )
            currentQueue.addAll(freshBatch)
        }
    }


    fun submitAnswer(result: PromotionEngine.ReviewResult) {
        val current = activeCard ?: return
        _uiState.value = ExerciseUiState.Loading

        viewModelScope.launch {
            val updatedProgress = PromotionEngine.gradeCard(current.progress, result)
            repository.updateProgressState(updatedProgress)

            loadNextCard()
        }
    }

    private fun loadNextCard() {
        viewModelScope.launch {
            val rolledLevel = rollWeightWheel()

            if (rolledLevel == null) {
                _uiState.value = ExerciseUiState.Finished(
                    "All flashcards in the categories are reviewed. Good job!")
                return@launch
            }

            val targetQueue = cardPool[rolledLevel]
            refetchPoolForLevel(rolledLevel)

            if (targetQueue != null && targetQueue.isNotEmpty()) {
                val nextCardAndProgress = targetQueue.removeFirst()
                activeCard = nextCardAndProgress
                _uiState.value = ExerciseUiState.PresentCard(nextCardAndProgress.flashCard)
            } else {
                // Fallback Radar Engine
                var foundFallback = false
                for (level in adjustedWeights.keys.filter { it != rolledLevel }) {
                    refetchPoolForLevel(level)
                    val fallbackQueue = cardPool[level]
                    if (fallbackQueue != null && fallbackQueue.isNotEmpty()) {
                        val nextCardAndProgress = fallbackQueue.removeFirst()
                        activeCard = nextCardAndProgress
                        _uiState.value = ExerciseUiState.PresentCard(nextCardAndProgress.flashCard)
                        foundFallback = true
                        break
                    }
                }

                if (!foundFallback) {
                    _uiState.value = ExerciseUiState.Finished("Session complete! No additional cards are ready for review right now.")
                }
            }
        }
    }

    private fun rollWeightWheel(): ProgressLevel? {
        val totalWeightSum = adjustedWeights.values.sum()
        if (totalWeightSum == 0) return null

        val roll = Random.nextInt(0, totalWeightSum)
        var cumulativeSum = 0

        for ((level, weight) in adjustedWeights) {
            cumulativeSum += weight
            if (roll < cumulativeSum) {
                return level
            }
        }
        return adjustedWeights.keys.firstOrNull()
    }

    sealed interface ExerciseUiState {
        object Loading : ExerciseUiState
        data class EffectivenessWarning(val message: String, val onProceed: () -> Unit) : ExerciseUiState
        data class PresentCard(val card: FlashCard) : ExerciseUiState
        data class Finished(val summary: String) : ExerciseUiState
    }
}