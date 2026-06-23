package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.FlashCardRepository
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FlashCardRepository(application)

    val progressLevelCounts: StateFlow<Map<ProgressLevel, Int>> = repository.progressLevelCounts
        .map { dbCountsMap ->
            // Ensure every enum tier always has a key mapping, fallback to 0
            ProgressLevel.entries.associateWith { level ->
                dbCountsMap[level] ?: 0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProgressLevel.entries.associateWith { 0 }
        )

    fun prepareNewBatch(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.promoteNewToBatch(amount)
        }
    }
}