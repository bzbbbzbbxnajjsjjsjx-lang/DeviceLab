package com.example.devicelab.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.devicelab.DeviceLabApp
import com.example.devicelab.domain.model.CapabilityStatus
import com.example.devicelab.domain.model.DiagnosticSeverity
import com.example.devicelab.domain.model.HardwareIntelligenceSnapshot
import com.example.devicelab.domain.model.SensorCategory
import com.example.devicelab.domain.model.SensorDescriptor
import com.example.devicelab.domain.util.TelemetryMath

// Shared Back Navigation Icon
private val BackVectorIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ArrowBack",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = androidx.compose.ui.graphics.SolidColor(Color.White), strokeLineWidth = 2.0f) {
            moveTo(20f, 12f)
            lineTo(4f, 12f)
            moveTo(10f, 18f)
            lineTo(4f, 12f)
            lineTo(10f, 6f)
        }
    }.build()
}

// ==========================================
// 1. CPU Detail Screen
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cpu = snapshot.cpu
    val identity = snapshot.identity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CPU Intelligence", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailMetricCard(title = "PROCESSOR TOPOLOGY") {
                DetailRow("Active Cores", "${cpu.coreCount} Cores")
                DetailRow("Architecture", cpu.architecture)
                DetailRow("SoC Board Code", identity.board)
                DetailRow("Hardware Silicon", identity.hardware)
                DetailRow("Supported ABIs", cpu.supportedAbis.joinToString(", "))
            }

            DetailMetricCard(title = "CORE CLOCKS & GOVERNOR") {
                DetailRow("CPU Frequency Access", if (cpu.isFrequencyAvailable) "Available" else "Restricted by Android SELinux")
                DetailRow("CPU Governor", cpu.governor ?: "Unavailable / Inaccessible")
                if (cpu.isFrequencyAvailable) {
                    cpu.perCoreFrequenciesKHz.forEachIndexed { index, freq ->
                        DetailRow("Core #$index Frequency", freq?.let { "${it / 1000} MHz" } ?: "Idle / Offline")
                    }
                } else {
                    Text(
                        text = "Android 8+ SELinux prevents unprivileged applications from polling raw /sys/devices/system/cpu frequency nodes. Core count and architecture are hardware verified.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. Memory Detail Screen
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mem = snapshot.memory

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Diagnostics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailMetricCard(title = "PHYSICAL RAM (SYSTEM)") {
                DetailRow("Total Installed RAM", TelemetryMath.formatBytes(mem.totalBytes))
                DetailRow("Used Memory", TelemetryMath.formatBytes(mem.usedBytes))
                DetailRow("Available Free RAM", TelemetryMath.formatBytes(mem.availableBytes))
                DetailRow("Memory Pressure", "${"%.1f".format(mem.usedPercentage)}%")
                DetailRow("LMK Critical Threshold", TelemetryMath.formatBytes(mem.lowMemoryThresholdBytes))
                DetailRow("Low Memory Warning", if (mem.isLowMemory) "ACTIVE (Low Memory)" else "Nominal")
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { mem.usedPercentage / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
            }

            DetailMetricCard(title = "PROCESS JVM HEAP") {
                DetailRow("JVM Allocated Heap", TelemetryMath.formatBytes(mem.jvmTotalHeapBytes))
                DetailRow("JVM Free Heap", TelemetryMath.formatBytes(mem.jvmFreeHeapBytes))
                DetailRow("JVM Max Heap Limit", TelemetryMath.formatBytes(mem.jvmMaxHeapBytes))
            }
        }
    }
}

// ==========================================
// 3. Battery Detail Screen
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val b = snapshot.battery

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Power & Battery", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailMetricCard(title = "CELL STATE & CAPACITY") {
                DetailRow("Reserve Level", "${b.percentage}%")
                DetailRow("Charging Status", b.chargingStatus.name)
                DetailRow("Power Supply", b.pluggedType.name)
                DetailRow("Cell Health", b.health.name)
                DetailRow("Chemistry / Tech", b.technology ?: "Lithium-Ion (Standard)")
            }

            DetailMetricCard(title = "ELECTRICAL TELEMETRY") {
                DetailRow("Terminal Voltage", b.voltageMillivolts?.let { "${"%.2f".format(it / 1000f)} V ($it mV)" } ?: "Unavailable")
                DetailRow("Instantaneous Current", b.currentMicroamps?.let { "${it / 1000} mA" } ?: "Unavailable on kernel")
                DetailRow("Thermistor Temp", b.temperatureCelsius?.let { "${"%.1f".format(it)} °C" } ?: "Unavailable")
            }
        }
    }
}

// ==========================================
// 4. Storage Detail Screen
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = snapshot.storage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Subsystem", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailMetricCard(title = "INTERNAL FLASH STORAGE (/data)") {
                DetailRow("Total Partition Size", TelemetryMath.formatBytes(s.internalTotalBytes))
                DetailRow("Used Storage", TelemetryMath.formatBytes(s.internalUsedBytes))
                DetailRow("Available Storage", TelemetryMath.formatBytes(s.internalAvailableBytes))
                DetailRow("Usage Percentage", "${"%.1f".format(s.internalUsedPercentage)}%")
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { s.internalUsedPercentage / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
            }

            DetailMetricCard(title = "SECONDARY / EXTERNAL STORAGE") {
                DetailRow("Removable Media", if (s.externalStorageAvailable) "Mounted" else "None Detected")
                if (s.externalStorageAvailable && s.externalTotalBytes != null) {
                    DetailRow("External Size", TelemetryMath.formatBytes(s.externalTotalBytes))
                    DetailRow("External Free", TelemetryMath.formatBytes(s.externalAvailableBytes ?: 0L))
                }
            }
        }
    }
}

// ==========================================
// 5. Display Detail Screen
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val d = snapshot.display

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Display Intelligence", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailMetricCard(title = "PANEL RESOLUTION & DENSITY") {
                DetailRow("Native Resolution", "${d.widthPixels} × ${d.heightPixels} px")
                DetailRow("Pixel Density", "${d.densityDpi} DPI")
                DetailRow("Density Scale", "${d.densityScale}x")
            }

            DetailMetricCard(title = "REFRESH RATE & MODES") {
                DetailRow("Current Refresh Rate", "${"%.1f".format(d.currentRefreshRateHz)} Hz")
                DetailRow("Supported Modes", d.supportedRefreshRates.joinToString { "${it.toInt()} Hz" })
            }

            DetailMetricCard(title = "COLOR PIPELINE") {
                DetailRow("HDR Rendering", if (d.isHdr) "Supported (High Dynamic Range)" else "SDR (Standard Dynamic Range)")
                DetailRow("Wide Color Gamut", if (d.isWideColorGamut) "Supported (DCI-P3)" else "Standard sRGB")
            }
        }
    }
}

// ==========================================
// 6. Sensors Detail Screen (With Live Streaming!)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext as DeviceLabApp
    val sensorsViewModel: SensorsViewModel = viewModel(
        factory = SensorsViewModel.provideFactory(context.container.dashboardRepository)
    )
    val state by sensorsViewModel.uiState.collectAsState()

    val sensors = snapshot.sensors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensors (${sensors.size} Discovered)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        sensorsViewModel.stopStreaming()
                        onBack()
                    }) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Stream Monitor Card (If a sensor is selected)
            if (state.selectedSensor != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LIVE: ${state.selectedSensor?.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "STREAMING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val reading = state.latestReading
                        if (reading != null && reading.values.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                reading.values.take(3).forEachIndexed { index, value ->
                                    val axis = when (index) { 0 -> "X"; 1 -> "Y"; 2 -> "Z"; else -> "#$index" }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = axis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            Text(
                                                text = "%.3f".format(value),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Waiting for initial hardware event...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tap any sensor below to open real-time hardware telemetry stream.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Sensors List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sensors) { sensor ->
                    val isSelected = state.selectedSensor?.type == sensor.type
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sensorsViewModel.selectSensor(sensor) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = sensor.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = sensor.category.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Vendor: ${sensor.vendor} • Power: ${sensor.powerMa} mA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. Network Detail Screen
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val net = snapshot.network

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Diagnostics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailMetricCard(title = "ACTIVE INTERFACE STATUS") {
                DetailRow("Transport Layer", net.transport.name)
                DetailRow("Physical Link", if (net.isConnected) "Connected" else "Disconnected")
                DetailRow("Internet Validation", if (net.isValidated) "Validated (Online)" else "Unvalidated / Gateway Only")
                DetailRow("Metered Interface", if (net.isMetered) "Yes (Metered)" else "No (Unmetered)")
            }

            DetailMetricCard(title = "LINK BANDWIDTH ESTIMATES") {
                DetailRow("Downstream Bandwidth", net.downlinkBandwidthKbps?.let { "${it / 1000} Mbps" } ?: "Unavailable")
                DetailRow("Upstream Bandwidth", net.uplinkBandwidthKbps?.let { "${it / 1000} Mbps" } ?: "Unavailable")
            }
        }
    }
}

// ==========================================
// 8. Capabilities Matrix Screen
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapabilitiesDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capabilities = snapshot.capabilities

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hardware Capabilities", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(capabilities) { cap ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cap.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cap.category + (cap.detail?.let { " • $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        val (pillColor, textColor) = if (cap.status == CapabilityStatus.SUPPORTED) {
                            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = pillColor
                        ) {
                            Text(
                                text = cap.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. Diagnostics Detail Screen
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsDetailScreen(
    snapshot: HardwareIntelligenceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val issues = snapshot.diagnostics

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hardware Diagnostics (${issues.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = BackVectorIcon, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (issues.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "All Subsystems Nominal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Zero hardware anomalies, thermal throttling, or critical memory events detected across all monitored busses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(issues) { issue ->
                        val (containerColor, contentColor) = when (issue.severity) {
                            DiagnosticSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                            DiagnosticSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                            DiagnosticSeverity.INFO -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = containerColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = issue.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = contentColor
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = contentColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = issue.severity.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = contentColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = issue.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = contentColor
                                )
                                Text(
                                    text = "Origin: ${issue.subsystem}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Reusable Helper Composables
// ==========================================
@Composable
private fun DetailMetricCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.0.sp,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
