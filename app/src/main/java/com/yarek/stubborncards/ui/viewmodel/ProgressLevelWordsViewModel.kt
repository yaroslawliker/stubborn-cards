package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yarek.stubborncards.database.repository.FlashCardRepository
import com.yarek.stubborncards.model.FlashCard
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class ProgressLevelWordsViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = FlashCardRepository(application)

    // Parse the category passed from the previous screen via navigation
    val selectedLevel: ProgressLevel = ProgressLevel.valueOf(
        savedStateHandle.get<String>("categoryName") ?: ProgressLevel.NEW.name
    )

    private val _showTranslations = MutableStateFlow(false)
    val showTranslations = _showTranslations.asStateFlow()

    fun onToggleTranslations(show: Boolean) {
        _showTranslations.value = show
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedCards: StateFlow<PagingData<FlashCard>> = _searchQuery
        .flatMapLatest { query ->
            repository.getCardsByLevelPaged(selectedLevel, query)
        }
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PagingData.empty())
}