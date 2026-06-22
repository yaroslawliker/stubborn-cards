package com.yarek.stubborncards.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yarek.stubborncards.database.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(application)

    private val _uiState = MutableStateFlow(HomeDashboardState())
    val uiState: StateFlow<HomeDashboardState> = _uiState.asStateFlow()

    fun loadDashboardStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val now = LocalDateTime.now(ZoneOffset.UTC)

            val totals = repository.getDashboardTotals(now)

            val notReady = totals.totalCards - totals.totalReady - totals.totalHidden

            _uiState.value = HomeDashboardState(
                readyToReview = totals.totalReady,
                notReady = if (notReady > 0) notReady else 0,
                hiddenUntilTest = totals.totalHidden,
                isLoading = false,
                isLoaded = true
            )
        }
    }

    data class HomeDashboardState(
        val readyToReview: Int = 0,
        val notReady: Int = 0,
        val hiddenUntilTest: Int = 0,
        val isLoading: Boolean = false,
        val isLoaded: Boolean = false
    )
}