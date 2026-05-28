package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.ai.AiCard
import com.yarek.stubborncards.ai.AiGenerationResult
import com.yarek.stubborncards.ai.SentenceGenerator
import com.yarek.stubborncards.config.AppConfigManager
import com.yarek.stubborncards.database.repository.ExerciseRepository
import com.yarek.stubborncards.engine.PromotionEngine
import com.yarek.stubborncards.model.CardAndProgress
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiExerciseViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ExerciseRepository(application)
    private val configManager = AppConfigManager.getInstance()
    private val sentenceGenerator = SentenceGenerator()

    private val contextWordsLimit = 50

    private val _uiState = MutableStateFlow<AiExerciseUiState>(AiExerciseUiState.Initial)
    val uiState = _uiState.asStateFlow()

    // Matches the AI-generated sentence with the actual database card and progress
    private val sessionQueue = ArrayDeque<Pair<AiCard, CardAndProgress>>()
    private var activePair: Pair<AiCard, CardAndProgress>? = null

    fun prepareAiCards(
        cleanUpTargetCount: Int = 4,
        knownTargetCount: Int = 2,
        strictContext: Boolean = false
    ) {
        _uiState.value = AiExerciseUiState.Generating

        val token = configManager.androidAiToken
        if (token.isBlank()) {
            _uiState.value = AiExerciseUiState.Error("API Token is missing. Please set it in AI Settings.")
            return
        }

        viewModelScope.launch {
            val cleanUpConstants = PromotionEngine.getLevelConfig(ProgressLevel.CLEAN_UP)
            val knownConstants = PromotionEngine.getLevelConfig(ProgressLevel.KNOWN)

            // Pass the dynamic limits to the repository
            val cleanUpBatch = repository.fetchExerciseBatch(
                level = ProgressLevel.CLEAN_UP,
                requiredScore = cleanUpConstants.requiredScore,
                testIntervalSeconds = cleanUpConstants.testIntervalSeconds,
                limit = cleanUpTargetCount
            )

            val knownBatch = repository.fetchExerciseBatch(
                level = ProgressLevel.KNOWN,
                requiredScore = knownConstants.requiredScore,
                testIntervalSeconds = knownConstants.testIntervalSeconds,
                limit = knownTargetCount
            )

            val targetCards = cleanUpBatch + knownBatch

            if (targetCards.isEmpty()) {
                _uiState.value = AiExerciseUiState.Error("No words available for practice in Clean Up or Known levels.")
                return@launch
            }

            // --- Fetch Context Words (LEARNED and MASTERED) ---
            // TODO: refactor, making separate DB queries
            val learnedBatch = repository.fetchExerciseBatch(ProgressLevel.LEARNED, 999, 0, contextWordsLimit / 2)
            val masteredBatch = repository.fetchExerciseBatch(ProgressLevel.MASTERED, 999, 0, contextWordsLimit / 2)
            
            val targetWordsList = targetCards.map { it.flashCard.word }
            val contextWordsList = (learnedBatch + masteredBatch).map { it.flashCard.word }

            // --- Call the AI ---
            val result = sentenceGenerator.generatePracticeCards(
                apiKey = token,
                targetWords = targetWordsList,
                knownWords = contextWordsList,
                strictContext = strictContext
            )

            // --- Handle Result and Map back to DB entities ---
            when (result) {
                is AiGenerationResult.Success -> {
                    sessionQueue.clear()
                    
                    // Match the AI response back to our database objects
                    result.data.sentences.forEach { aiCard ->
                        val matchingDbCard = targetCards.find { it.flashCard.word == aiCard.targetWord }
                        if (matchingDbCard != null) {
                            sessionQueue.add(Pair(aiCard, matchingDbCard))
                        }
                    }
                    
                    loadNextCard()
                }
                is AiGenerationResult.ParsingError -> _uiState.value = AiExerciseUiState.Error("The AI got confused. Please try again.")
                is AiGenerationResult.QuotaExceeded -> _uiState.value = AiExerciseUiState.Error("AI token limit reached. Check billing.")
                is AiGenerationResult.UnknownError -> _uiState.value = AiExerciseUiState.Error("Unknown error. Please, try again later.")
            }
        }
    }

    /**
     * @param result Standard grading result
     * @param isWeird If true, scores as ALMOST_CORRECT but does NOT reset the lastReviewed timer
     */
    // TODO: refactor enum with inheritance
    fun submitAnswer(result: PromotionEngine.ReviewResult, isWeird: Boolean = false) {
        val currentPair = activePair ?: return
        val dbProgress = currentPair.second.progress ?: run {
            loadNextCard()
            return
        }

        viewModelScope.launch {
            val originalReviewedDate = dbProgress.lastReviewed

            if (!isWeird) {
                // Grade normally (if not weird)
                var updatedProgress = PromotionEngine.gradeCard(dbProgress, result)
                repository.updateProgressState(updatedProgress)
            }

            loadNextCard()
        }
    }

    private fun loadNextCard() {
        if (sessionQueue.isEmpty()) {
            _uiState.value = AiExerciseUiState.Finished("AI Session Complete! Great job reading in context.")
            return
        }

        val nextPair = sessionQueue.removeFirst()
        activePair = nextPair
        _uiState.value = AiExerciseUiState.PresentCard(nextPair.first, nextPair.second)
    }

    fun repeatSession() {
        _uiState.value = AiExerciseUiState.Initial
    }

    sealed interface AiExerciseUiState {
        object Initial : AiExerciseUiState
        object Generating : AiExerciseUiState
        data class PresentCard(val aiCard: AiCard, val dbCard: CardAndProgress) : AiExerciseUiState
        data class Error(val message: String) : AiExerciseUiState
        data class Finished(val summary: String) : AiExerciseUiState
    }
}