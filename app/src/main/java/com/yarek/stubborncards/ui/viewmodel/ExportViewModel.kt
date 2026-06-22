package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.ImportExportRepository
import com.yarek.stubborncards.engine.ImportExportEngine
import com.yarek.stubborncards.model.ExportConfig
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExportViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize the repository and pass it to the engine internally
    private val repository = ImportExportRepository(application)
    private val engine = ImportExportEngine(application, repository)

    private val _uiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val _config = MutableStateFlow(
        ExportConfig(
            includedLevels = ProgressLevel.values().toList(), // Default to all levels
            includeLearningProgress = true
        )
    )
    val config: StateFlow<ExportConfig> = _config.asStateFlow()

    fun updateConfig(newConfig: ExportConfig) {
        _config.value = newConfig
    }

    fun resetState() {
        _uiState.value = ExportUiState.Idle
    }

    fun exportCsv(uri: Uri) {
        _uiState.value = ExportUiState.Loading
        viewModelScope.launch {
            try {
                // Background processing happens safely inside the engine
                engine.exportCsv(uri, _config.value)
                _uiState.value = ExportUiState.Success("Successfully exported to CSV!")
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    sealed class ExportUiState {
        object Idle : ExportUiState()
        object Loading : ExportUiState()
        data class Success(val message: String) : ExportUiState()
        data class Error(val message: String) : ExportUiState()
    }
}