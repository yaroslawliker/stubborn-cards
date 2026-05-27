package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.config.AppConfigManager
import com.yarek.stubborncards.database.repository.FlashCardRepository
import com.yarek.stubborncards.engine.PromotionEngine
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UnitCardViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = FlashCardRepository(application)

    private val initialCardId: Long = savedStateHandle.get<Long>("cardId") ?: -1L
    private val levelName: String = savedStateHandle.get<String>("categoryName") ?: ProgressLevel.NEW.name
    private val currentLevel = ProgressLevel.valueOf(levelName)

    private var cardIdsList = listOf<Long>()

    private val _currentIndex = MutableStateFlow(-1)

    init {
        loadAllIds()
    }

    private fun loadAllIds() {
        viewModelScope.launch {
            cardIdsList = repository.getCardIdsByLevel(currentLevel)
            val index = cardIdsList.indexOf(initialCardId)

            _currentIndex.value = if (index != -1) index else 0
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState> = _currentIndex
        .flatMapLatest { index ->
            if (index == -1 || cardIdsList.isEmpty()) {
                flowOf(UiState.Error("No cards available"))
            } else {
                val targetId = cardIdsList[index]

                repository.getCardDetailsFlow(targetId).map { (card, progress) ->
                    if (card != null) {
                        val targetScore = AppConfigManager.getInstance()
                            .currentPromotionTable[currentLevel]?.requiredScore ?: -1
                        UiState.Success(
                            card = card,
                            progress = progress,
                            hasPrevious = index > 0,
                            hasNext = index < cardIdsList.size - 1,
                            requiredScore = targetScore
                        )
                    } else {
                        UiState.Error("Failed to load card details")
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun navigateToNextCard() {
        if (_currentIndex.value < cardIdsList.size - 1) {
            _currentIndex.value++
        }
    }

    fun navigateToPreviousCard() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
        }
    }

    sealed interface UiState {
        object Loading : UiState
        data class Success(
            val card: FlashCard,
            val progress: LearningProgress?,
            val hasPrevious: Boolean,
            val hasNext: Boolean,
            val requiredScore: Int
        ) : UiState
        data class Error(val message: String) : UiState
    }
}