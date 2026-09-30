package com.example.devicelab.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.devicelab.domain.model.SensorDescriptor
import com.example.devicelab.domain.model.SensorReading
import com.example.devicelab.domain.repository.DashboardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SensorDetailUiState(
    val selectedSensor: SensorDescriptor? = null,
    val latestReading: SensorReading? = null,
    val isStreaming: Boolean = false
)

class SensorsViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorDetailUiState())
    val uiState: StateFlow<SensorDetailUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null

    fun selectSensor(sensor: SensorDescriptor) {
        if (_uiState.value.selectedSensor?.type == sensor.type) return

        streamingJob?.cancel()
        _uiState.value = SensorDetailUiState(
            selectedSensor = sensor,
            latestReading = null,
            isStreaming = true
        )

        streamingJob = viewModelScope.launch {
            repository.observeLiveSensor(sensor.type).collect { reading ->
                _uiState.value = _uiState.value.copy(
                    latestReading = reading
                )
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
        _uiState.value = _uiState.value.copy(isStreaming = false)
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }

    companion object {
        fun provideFactory(repository: DashboardRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SensorsViewModel(repository) as T
                }
            }
    }
}
