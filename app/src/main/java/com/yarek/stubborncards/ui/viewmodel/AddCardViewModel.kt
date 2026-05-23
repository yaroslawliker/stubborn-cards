package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.FlashCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FlashCardRepository(application)

    private val _word = MutableStateFlow("")
    val word = _word.asStateFlow()

    private val _translation = MutableStateFlow("")
    val translation = _translation.asStateFlow()

    fun onWordChange(newWord: String) {
        _word.value = newWord
    }

    fun onTranslationChange(newTranslation: String) {
        _translation.value = newTranslation
    }

    fun saveCard() {
        val currentWord = _word.value
        val currentTranslation = _translation.value

        if (currentWord.isNotBlank() && currentTranslation.isNotBlank()) {
            viewModelScope.launch {
                repository.createCardWithInitialProgress(currentWord, currentTranslation)

                _word.value = ""
                _translation.value = ""
            }
        }
    }
}