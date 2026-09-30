package com.example.devicelab

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.devicelab.ui.dashboard.DashboardRoute
import com.example.devicelab.ui.dashboard.DashboardUiState
import com.example.devicelab.ui.dashboard.DashboardViewModel
import com.example.devicelab.ui.details.BatteryDetailScreen
import com.example.devicelab.ui.details.CapabilitiesDetailScreen
import com.example.devicelab.ui.details.CpuDetailScreen
import com.example.devicelab.ui.details.DiagnosticsDetailScreen
import com.example.devicelab.ui.details.DisplayDetailScreen
import com.example.devicelab.ui.details.MemoryDetailScreen
import com.example.devicelab.ui.details.NetworkDetailScreen
import com.example.devicelab.ui.details.SensorsDetailScreen
import com.example.devicelab.ui.details.StorageDetailScreen
import com.example.devicelab.ui.main.MainScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Dashboard)
    val context = LocalContext.current.applicationContext as DeviceLabApp
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.provideFactory(context.container.dashboardRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val intelligence = (uiState as? DashboardUiState.Success)?.intelligence

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Dashboard> {
                DashboardRoute(
                    viewModel = viewModel,
                    onNavigate = { navKey -> backStack.add(navKey) }
                )
            }
            entry<Main> {
                MainScreen(
                    onItemClick = { navKey -> backStack.add(navKey) },
                    modifier = Modifier.safeDrawingPadding().padding(16.dp)
                )
            }
            entry<CpuDetail> {
                intelligence?.let { snapshot ->
                    CpuDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
            entry<MemoryDetail> {
                intelligence?.let { snapshot ->
                    MemoryDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
            entry<BatteryDetail> {
                intelligence?.let { snapshot ->
                    BatteryDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
            entry<StorageDetail> {
                intelligence?.let { snapshot ->
                    StorageDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
            entry<DisplayDetail> {
                intelligence?.let { snapshot ->
                    DisplayDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
            entry<SensorsDetail> {
                intelligence?.let { snapshot ->
                    SensorsDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
            entry<NetworkDetail> {
                intelligence?.let { snapshot ->
                    NetworkDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
            entry<CapabilitiesDetail> {
                intelligence?.let { snapshot ->
                    CapabilitiesDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
            entry<DiagnosticsDetail> {
                intelligence?.let { snapshot ->
                    DiagnosticsDetailScreen(snapshot = snapshot, onBack = { backStack.removeLastOrNull() })
                }
            }
        }
    )
}
