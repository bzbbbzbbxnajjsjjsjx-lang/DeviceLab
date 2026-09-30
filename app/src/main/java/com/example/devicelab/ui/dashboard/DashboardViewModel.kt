package com.example.devicelab.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.devicelab.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Lifecycle-aware ViewModel driving the DeviceLab hardware telemetry dashboard.
 */
class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeDashboardTelemetry(),
        _isRefreshing
    ) { telemetry, refreshing ->
        DashboardUiState.Success(
            telemetry = telemetry,
            isRefreshing = refreshing
        ) as DashboardUiState
    }.catch { throwable ->
        emit(DashboardUiState.Error(throwable.message ?: "Failed to load device telemetry"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState.Loading
    )

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshTelemetry()
            } catch (e: Exception) {
                // repository refresh errors are handled gracefully
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: DashboardRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(repository) as T
            }
        }
    }
}
