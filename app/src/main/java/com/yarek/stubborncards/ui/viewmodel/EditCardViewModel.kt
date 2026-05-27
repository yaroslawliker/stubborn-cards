package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.config.AppConfigManager
import com.yarek.stubborncards.database.repository.FlashCardRepository
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditCardViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = FlashCardRepository(application)

    private val cardId: Long = savedStateHandle.get<Long>("cardId") ?: -1L

    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _wordInput = MutableStateFlow("")
    val wordInput = _wordInput.asStateFlow()

    private val _translationInput = MutableStateFlow("")
    val translationInput = _translationInput.asStateFlow()

    private var loadedCard: FlashCard? = null
    private var loadedProgress: LearningProgress? = null

    private var isInitialLoadComplete = false

    init {
        loadCardData()
    }

    private fun loadCardData() {
        if (cardId == -1L) {
            _uiState.value = EditUiState.Error("Invalid Card ID")
            return
        }

        viewModelScope.launch {
            repository.getCardDetailsFlow(cardId).collectLatest { (card, progress) ->
                if (card != null) {
                    loadedCard = card
                    loadedProgress = progress

                    if (!isInitialLoadComplete) {
                        _wordInput.value = card.word
                        _translationInput.value = card.translation
                        isInitialLoadComplete = true
                    }

                    val dynamicMaxScore = progress?.level?.let { level ->
                        AppConfigManager.getInstance().currentPromotionTable[level]?.requiredScore
                    } ?: -1

                    _uiState.value = EditUiState.Success(progress, dynamicMaxScore)
                } else {
                    _uiState.value = EditUiState.Error("Card missing")
                }
            }
        }
    }

    fun onWordTextChanged(newValue: String) { _wordInput.value = newValue }
    fun onTranslationTextChanged(newValue: String) { _translationInput.value = newValue }

    fun saveTextChanges(onSuccess: () -> Unit) {
        val card = loadedCard ?: return
        viewModelScope.launch {
            card.word = _wordInput.value
            card.translation = _translationInput.value
            repository.updateCardText(card)
            onSuccess()
        }
    }

    fun updateLevelInstant(newLevel: ProgressLevel) {
        viewModelScope.launch {
            // Let the live flow push changes back to the UI instead of manipulating state manually
            repository.updateProgressLevelDirectly(cardId, newLevel)
        }
    }

    fun adjustScoreInstant(amount: Int) {
        val currentScore = loadedProgress?.score ?: 0f

        val currentMaxScore = loadedProgress?.level?.let { level ->
            AppConfigManager.getInstance().currentPromotionTable[level]?.requiredScore
        } ?: 5

        val calculatedScore = (currentScore + amount).coerceIn(0f, currentMaxScore.toFloat())
        viewModelScope.launch {
            repository.updateProgressScoreDirectly(cardId, calculatedScore)
        }
    }

    sealed interface EditUiState {
        object Loading : EditUiState
        data class Success(val progress: LearningProgress?, val maxScore: Int) : EditUiState
        data class Error(val message: String) : EditUiState
    }
}