package com.example.devicelab.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.devicelab.domain.repository.DashboardRepository
import kotlinx.coroutines.delay
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
    private val repository: DashboardRepository,
    private val minEntranceDurationMs: Long = 1300L
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isEntranceFinished = MutableStateFlow(minEntranceDurationMs <= 0L)

    init {
        if (minEntranceDurationMs > 0L) {
            viewModelScope.launch {
                delay(minEntranceDurationMs)
                _isEntranceFinished.value = true
            }
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeHardwareIntelligence(),
        _isRefreshing,
        _isEntranceFinished
    ) { intelligence, refreshing, entranceFinished ->
        if (!entranceFinished) {
            DashboardUiState.Initializing() as DashboardUiState
        } else {
            DashboardUiState.Success(
                telemetry = intelligence.toLegacyTelemetry(),
                intelligence = intelligence,
                isRefreshing = refreshing
            ) as DashboardUiState
        }
    }.catch { throwable ->
        emit(DashboardUiState.Error(throwable.message ?: "Failed to load device telemetry"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = if (minEntranceDurationMs > 0L) {
            DashboardUiState.Initializing()
        } else {
            repository.getInitialHardwareIntelligence()?.let {
                DashboardUiState.Success(
                    telemetry = it.toLegacyTelemetry(),
                    intelligence = it,
                    isRefreshing = false
                )
            } ?: repository.getInitialTelemetry()?.let {
                DashboardUiState.Success(telemetry = it, isRefreshing = false)
            } ?: DashboardUiState.Initializing()
        }
    )

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshHardwareIntelligence()
            } catch (e: Exception) {
                // repository refresh errors are handled gracefully
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: DashboardRepository,
            minEntranceDurationMs: Long = 1300L
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(repository, minEntranceDurationMs) as T
            }
        }
    }
}
