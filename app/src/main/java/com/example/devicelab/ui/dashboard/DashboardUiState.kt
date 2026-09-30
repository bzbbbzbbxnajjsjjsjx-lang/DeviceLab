package com.example.devicelab.ui.dashboard

import com.example.devicelab.domain.model.DashboardTelemetry

/**
 * UI State representing the state of the DeviceLab Dashboard screen.
 */
sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val telemetry: DashboardTelemetry,
        val isRefreshing: Boolean = false
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}
