package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.FlashCardRepository
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.LearningProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnitCardViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = FlashCardRepository(application)

    // Grab the ID passed from the list page click action
    private val cardId: Long = savedStateHandle.get<Long>("cardId") ?: -1

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadCardDetails()
    }

    private fun loadCardDetails() {
        if (cardId == -1L) {
            _uiState.value = UiState.Error("Invalid Card ID")
            return
        }

        viewModelScope.launch {
            val (card, progress) = repository.getCardDetailsWithProgress(cardId)
            if (card != null) {
                _uiState.value = UiState.Success(card, progress)
            } else {
                _uiState.value = UiState.Error("Card not found")
            }
        }
    }

    sealed interface UiState {
        object Loading : UiState
        data class Success(val card: FlashCard, val progress: LearningProgress?) : UiState
        data class Error(val message: String) : UiState
    }
}