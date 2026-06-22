package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.ImportExportRepository
import kotlinx.coroutines.launch

class DeleteAllViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImportExportRepository(application)

    /**
     * Wipes all flashcards and progress safely on a background thread.
     */
    fun deleteAllCards() {
        viewModelScope.launch {
            repository.deleteAllCards()
        }
    }
}