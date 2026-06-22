package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.ImportExportRepository
import com.yarek.stubborncards.engine.ImportExportEngine
import com.yarek.stubborncards.model.ImportConfig
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImportExportRepository(application)
    private val engine = ImportExportEngine(application, repository)

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private val _config = MutableStateFlow(
        ImportConfig(
            defaultLevel = ProgressLevel.NEW,
            overrideAllLevels = false,
            defaultLastReviewedAsNow = false,
            updateDuplicates = false
        )
    )
    val config: StateFlow<ImportConfig> = _config.asStateFlow()

    fun updateConfig(newConfig: ImportConfig) {
        _config.value = newConfig
    }

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }

    fun importCsv(uri: Uri) {
        _uiState.value = ImportUiState.Loading
        viewModelScope.launch {
            try {
                // Background processing happens safely inside the engine
                engine.importCsv(uri, _config.value)
                _uiState.value = ImportUiState.Success("Successfully imported cards!")
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    sealed class ImportUiState {
        object Idle : ImportUiState()
        object Loading : ImportUiState()
        data class Success(val message: String) : ImportUiState()
        data class Error(val message: String) : ImportUiState()
    }
}