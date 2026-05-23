package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.FlashCardRepository
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnitCardViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = FlashCardRepository(application)

    private val initialCardId: Long = savedStateHandle.get<Long>("cardId")?.toLong() ?: -1L
    private val levelName: String = savedStateHandle.get<String>("categoryName") ?: ProgressLevel.NEW.name
    private val currentLevel = ProgressLevel.valueOf(levelName)

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var cardIdsList = listOf<Long>()
    private var currentIndex = -1

    init {
        loadAllIdsAndCurrentCard()
    }

    private fun loadAllIdsAndCurrentCard() {
        viewModelScope.launch {
            // 1. Get all IDs in this category to establish the navigation pool
            cardIdsList = repository.getCardIdsByLevel(currentLevel)
            currentIndex = cardIdsList.indexOf(initialCardId)

            // Fallback if the card wasn't found in this specific list
            if (currentIndex == -1 && cardIdsList.isNotEmpty()) {
                currentIndex = 0
            }

            loadCurrentCardDetails()
        }
    }

    private suspend fun loadCurrentCardDetails() {
        if (currentIndex == -1 || cardIdsList.isEmpty()) {
            _uiState.value = UiState.Error("No cards available")
            return
        }

        val targetId = cardIdsList[currentIndex]
        val (card, progress) = repository.getCardDetailsWithProgress(targetId)

        if (card != null) {
            _uiState.value = UiState.Success(
                card = card,
                progress = progress,
                hasPrevious = currentIndex > 0,
                hasNext = currentIndex < cardIdsList.size - 1
            )
        } else {
            _uiState.value = UiState.Error("Failed to load card details")
        }
    }

    fun navigateToNextCard() {
        if (currentIndex < cardIdsList.size - 1) {
            currentIndex++
            updateCardState()
        }
    }

    fun navigateToPreviousCard() {
        if (currentIndex > 0) {
            currentIndex--
            updateCardState()
        }
    }

    private fun updateCardState() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            loadCurrentCardDetails()
        }
    }

    sealed interface UiState {
        object Loading : UiState
        data class Success(
            val card: FlashCard,
            val progress: LearningProgress?,
            val hasPrevious: Boolean,
            val hasNext: Boolean
        ) : UiState
        data class Error(val message: String) : UiState
    }
}