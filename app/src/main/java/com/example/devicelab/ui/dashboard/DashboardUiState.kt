package com.example.devicelab.ui.dashboard

import com.example.devicelab.domain.model.DashboardTelemetry
import com.example.devicelab.domain.model.HardwareIntelligenceSnapshot

/**
 * UI State representing the state of the DeviceLab Dashboard screen.
 */
sealed interface DashboardUiState {
    data class Initializing(val message: String = "INITIALIZING HARDWARE BUSSES") : DashboardUiState

    data object Loading : DashboardUiState

    data class Success(
        val telemetry: DashboardTelemetry,
        val intelligence: HardwareIntelligenceSnapshot? = null,
        val isRefreshing: Boolean = false
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}
