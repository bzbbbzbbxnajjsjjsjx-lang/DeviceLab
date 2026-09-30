package com.example.devicelab.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.toShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.devicelab.DeviceLabApp
import com.example.devicelab.domain.model.BatteryHealth
import com.example.devicelab.domain.model.BatterySpecs
import androidx.navigation3.runtime.NavKey
import com.example.devicelab.BatteryDetail
import com.example.devicelab.CapabilitiesDetail
import com.example.devicelab.CpuDetail
import com.example.devicelab.DiagnosticsDetail
import com.example.devicelab.DisplayDetail
import com.example.devicelab.MemoryDetail
import com.example.devicelab.NetworkDetail
import com.example.devicelab.SensorsDetail
import com.example.devicelab.StorageDetail
import com.example.devicelab.domain.model.CapabilityStatus
import com.example.devicelab.domain.model.ChargingStatus
import com.example.devicelab.domain.model.DashboardTelemetry
import com.example.devicelab.domain.model.DeviceSpecs
import com.example.devicelab.domain.model.DiagnosticSeverity
import com.example.devicelab.domain.model.DisplayBasicSpecs
import com.example.devicelab.domain.model.HardwareIntelligenceSnapshot
import com.example.devicelab.domain.model.MemorySpecs
import com.example.devicelab.domain.model.PluggedType
import com.example.devicelab.domain.model.StorageSpecs
import com.example.devicelab.domain.model.ThermalSpecs
import com.example.devicelab.domain.model.ThermalStatus
import com.example.devicelab.domain.util.TelemetryMath
import com.example.devicelab.theme.DeviceLabTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel? = null,
    onNavigate: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext as DeviceLabApp
    val vm: DashboardViewModel = viewModel ?: viewModel(
        factory = DashboardViewModel.provideFactory(context.container.dashboardRepository)
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onRefresh = { vm.refresh() },
        onNavigate = onNavigate,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
    onNavigate: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPreviewLoading by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = uiState,
        transitionSpec = {
            fadeIn(animationSpec = tween(400, easing = EaseInOutCubic)) togetherWith
            fadeOut(animationSpec = tween(300, easing = EaseInOutCubic))
        },
        label = "DashboardStateTransition",
        modifier = modifier.fillMaxSize()
    ) { state ->
        when (state) {
            is DashboardUiState.Initializing -> {
                DeviceLabEntranceScreen(
                    statusMessage = state.message,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is DashboardUiState.Loading -> {
                DeviceLabEntranceScreen(
                    statusMessage = "CALIBRATING HARDWARE SENSORS",
                    modifier = Modifier.fillMaxSize()
                )
            }
            is DashboardUiState.Error -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "Telemetry Sensor Fault",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Text("Retry Acquisition")
                                }
                            }
                        }
                    }
                }
            }
            is DashboardUiState.Success -> {
                val isRefreshing = state.isRefreshing
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "DeviceLab",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier
                                            .padding(top = 1.dp)
                                            .clickable { isPreviewLoading = true }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                            Text(
                                                text = "HARDWARE LAB",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.8.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = onRefresh,
                                    enabled = !isRefreshing,
                                    colors = IconButtonDefaults.filledTonalIconButtonColors()
                                ) {
                                    if (isRefreshing) {
                                        LoadingIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = RefreshVectorIcon,
                                            contentDescription = "Refresh Telemetry"
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    if (isPreviewLoading) {
                        DashboardLoadingContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            isPreview = true,
                            onDismissPreview = { isPreviewLoading = false }
                        )
                    } else {
                        DashboardExpressiveContent(
                            telemetry = state.telemetry,
                            intelligence = state.intelligence,
                            onNavigate = onNavigate,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeviceLabEntranceScreen(
    statusMessage: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Official Material 3 Expressive Morphing & Rotating Loading Indicator
                LoadingIndicator(
                    modifier = Modifier.size(76.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Branded App Title
                Text(
                    text = "DeviceLab",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hardware Lab Eyebrow Capsule
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "HARDWARE LAB",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.0.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Status Tracking Label
                Text(
                    text = statusMessage.uppercase(Locale.US),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DashboardLoadingContent(
    modifier: Modifier = Modifier,
    isPreview: Boolean = false,
    onDismissPreview: (() -> Unit)? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Official Material 3 Expressive morphing polygon loading indicator
            LoadingIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Interrogating Hardware Busses...",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isPreview && onDismissPreview != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    TextButton(onClick = onDismissPreview) {
                        Text(
                            text = "Exit Preview Mode",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardExpressiveContent(
    telemetry: DashboardTelemetry,
    intelligence: HardwareIntelligenceSnapshot? = null,
    onNavigate: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High-Priority Diagnostics Alert Banner (if issues present)
        intelligence?.diagnostics?.takeIf { it.isNotEmpty() }?.let { issues ->
            val highest = issues.maxByOrNull { it.severity.ordinal }?.severity ?: DiagnosticSeverity.INFO
            val (bannerBg, bannerFg) = when (highest) {
                DiagnosticSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                DiagnosticSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                DiagnosticSeverity.INFO -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bannerBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(DiagnosticsDetail) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${issues.size} Hardware Diagnostics Alert${if (issues.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = bannerFg
                        )
                        Text(
                            text = issues.first().title,
                            style = MaterialTheme.typography.bodySmall,
                            color = bannerFg.copy(alpha = 0.85f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = bannerFg.copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "VIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = bannerFg,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 1. Dominant Live Device State Hero
        Box(modifier = Modifier.clickable { onNavigate(BatteryDetail) }) {
            LiveDeviceStateHero(
                battery = telemetry.battery,
                thermal = telemetry.thermal
            )
        }

        // 2. Secondary Compute Bento (Memory & Storage)
        ComputeBentoRow(
            memory = telemetry.memory,
            storage = telemetry.storage,
            onMemoryClick = { onNavigate(MemoryDetail) },
            onStorageClick = { onNavigate(StorageDetail) }
        )

        // 3. Platform Silicon & Physical Display Capsule
        Box(modifier = Modifier.clickable { onNavigate(DisplayDetail) }) {
            SiliconDisplayCapsule(
                device = telemetry.device,
                display = telemetry.display
            )
        }

        // 4. Hardware Subsystems & Diagnostics Section
        HardwareSubsystemsSection(
            intelligence = intelligence,
            onNavigate = onNavigate
        )

        // 5. Offline Diagnostic Stamp
        TelemetryExpressiveFooter(timestampMillis = telemetry.timestampMillis)

        Spacer(modifier = Modifier.navigationBarsPadding().height(20.dp))
    }
}

/**
 * Primary Visual Anchor: Live Physical Device State Hero
 * Features Material 3 Expressive CircularWavyProgressIndicator, thermal status badge,
 * and high-contrast electrical stats.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LiveDeviceStateHero(
    battery: BatterySpecs,
    thermal: ThermalSpecs,
    modifier: Modifier = Modifier
) {
    val isCharging = battery.chargingStatus == ChargingStatus.CHARGING
    var isStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isStarted = true }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isStarted) (battery.percentage / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "batteryProgressAnimation"
    )

    val animatedPercentage by animateFloatAsState(
        targetValue = if (isStarted) battery.percentage.toFloat() else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "batteryPercentageAnimation"
    )

    Surface(
        shape = RoundedCornerShape(36.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Row: System Status Pill & Thermal Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isCharging) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50))
                    )
                    Text(
                        text = if (isCharging) "CELL INGRESS ACTIVE" else "ENERGY SYSTEM ONLINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Geometric Expressive Thermal Badge
                ThermalExpressiveBadge(status = thermal.status)
            }

            // Central Hero Graphic & Metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "RESERVE CAPACITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${animatedPercentage.roundToInt()}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 66.sp,
                                lineHeight = 66.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-2.5).sp,
                                fontFamily = FontFamily.Default
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 24.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = if (isCharging) "Charging • ${battery.pluggedType.name}" else "Discharging • ${battery.pluggedType.name}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCharging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Cell Health: ${battery.health.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Official CircularWavyProgressIndicator from Material 3 Expressive
                val calibratedStrokeWidth = with(LocalDensity.current) { 6.dp.toPx() }
                val wavyStroke = remember(calibratedStrokeWidth) {
                    Stroke(width = calibratedStrokeWidth, cap = StrokeCap.Round)
                }

                CircularWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(96.dp),
                    stroke = wavyStroke,
                    trackStroke = wavyStroke,
                    color = if (battery.percentage <= 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    amplitude = { 1.0f },
                    wavelength = 24.dp
                )
            }

            // Bottom Metric Pill Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val voltageVolts = (battery.voltageMillivolts?.toDouble() ?: 0.0) / 1000.0
                MetricCapsule(
                    label = "TERMINAL VOLTAGE",
                    primaryValue = String.format(Locale.US, "%.2f V", voltageVolts),
                    secondaryValue = if (battery.voltageMillivolts != null) "${battery.voltageMillivolts} mV" else null,
                    modifier = Modifier.weight(1f)
                )
                MetricCapsule(
                    label = "THERMISTOR",
                    primaryValue = TelemetryMath.formatCelsius(battery.temperatureCelsius),
                    secondaryValue = if (thermal.status == ThermalStatus.NONE) "NOMINAL" else thermal.status.name,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Geometric Thermal Status Badge using Material 3 Expressive Shapes
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThermalExpressiveBadge(status: ThermalStatus) {
    val (bgColor, textColor, shape) = when (status) {
        ThermalStatus.NONE, ThermalStatus.LIGHT ->
            Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, CircleShape)
        ThermalStatus.MODERATE ->
            Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, RoundedCornerShape(12.dp))
        ThermalStatus.SEVERE, ThermalStatus.CRITICAL, ThermalStatus.EMERGENCY, ThermalStatus.SHUTDOWN ->
            Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, MaterialShapes.Cookie4Sided.toShape())
        ThermalStatus.NOT_SUPPORTED ->
            Triple(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
    }

    Surface(
        shape = shape,
        color = bgColor,
        modifier = Modifier.border(1.dp, textColor.copy(alpha = 0.2f), shape)
    ) {
        Text(
            text = "THERMAL: ${status.name}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/**
 * Asymmetric Bento Row for Compute Resources (RAM & Flash Storage)
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ComputeBentoRow(
    memory: MemorySpecs,
    storage: StorageSpecs,
    onMemoryClick: () -> Unit = {},
    onStorageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isStarted = true }

    val animatedRamProgress by animateFloatAsState(
        targetValue = if (isStarted) (memory.usedPercentage / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "ramProgressAnimation"
    )

    val animatedStorageProgress by animateFloatAsState(
        targetValue = if (isStarted) (storage.usedPercentage / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "storageProgressAnimation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // High-Watermark RAM Bento Tile
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onMemoryClick)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RAM PRESSURE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (memory.isLowMemory) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = "LOW",
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = TelemetryMath.formatPercentage(memory.usedPercentage),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (memory.isLowMemory) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )

                // LinearWavyProgressIndicator from Material 3 Expressive
                LinearWavyProgressIndicator(
                    progress = { animatedRamProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = if (memory.isLowMemory) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    amplitude = { if (memory.isLowMemory) 1.0f else 0.5f },
                    wavelength = 20.dp
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Free: ${TelemetryMath.formatBytes(memory.availableBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: ${TelemetryMath.formatBytes(memory.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Internal Storage Bento Tile
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onStorageClick)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "INTERNAL FLASH",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = TelemetryMath.formatPercentage(storage.usedPercentage),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                LinearProgressIndicator(
                    progress = { animatedStorageProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Free: ${TelemetryMath.formatBytes(storage.availableBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: ${TelemetryMath.formatBytes(storage.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Silicon Hardware & Physical Display Capsule
 */
@Composable
private fun SiliconDisplayCapsule(
    device: DeviceSpecs,
    display: DisplayBasicSpecs,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Platform & Model Identity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${device.manufacturer} ${device.model}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Board Code: ${device.device} • ${device.processorCount} Active Cores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "API ${device.apiLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Display Physical Telemetry Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PHYSICAL DISPLAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${display.widthPixels} × ${display.heightPixels} px",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = TelemetryMath.formatHz(display.refreshRateHz),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${display.densityDpi} dpi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "(${display.densityScale}x)",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Hardware Subsystems & Diagnostics Interactive Grid (Phase 1.7)
 */
@Composable
private fun HardwareSubsystemsSection(
    intelligence: HardwareIntelligenceSnapshot?,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "HARDWARE SUBSYSTEMS & DIAGNOSTICS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.outline
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SubsystemTile(
                title = "CPU Silicon",
                subtitle = "${intelligence?.cpu?.coreCount ?: 8} Cores • ${intelligence?.cpu?.architecture ?: "arm64"}",
                tag = "Topology",
                onClick = { onNavigate(CpuDetail) },
                modifier = Modifier.weight(1f)
            )
            SubsystemTile(
                title = "Sensors",
                subtitle = "${intelligence?.sensors?.size ?: 0} Discovered",
                tag = "Live Stream",
                onClick = { onNavigate(SensorsDetail) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SubsystemTile(
                title = "Network Link",
                subtitle = intelligence?.network?.transport?.name ?: "Wi-Fi",
                tag = if (intelligence?.network?.isValidated == true) "Online" else "Interface",
                onClick = { onNavigate(NetworkDetail) },
                modifier = Modifier.weight(1f)
            )
            SubsystemTile(
                title = "Capabilities",
                subtitle = "${intelligence?.capabilities?.count { it.status == CapabilityStatus.SUPPORTED } ?: 0} Verified",
                tag = "Matrix",
                onClick = { onNavigate(CapabilitiesDetail) },
                modifier = Modifier.weight(1f)
            )
        }

        // Diagnostics Tile
        val issueCount = intelligence?.diagnostics?.size ?: 0
        val isNominal = issueCount == 0
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isNominal) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(DiagnosticsDetail) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Diagnostics Engine",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isNominal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = if (isNominal) "All hardware systems operating within nominal parameters" else "$issueCount subsystem anomalies detected",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isNominal) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = if (isNominal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (isNominal) "NOMINAL" else "$issueCount ALERTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isNominal) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubsystemTile(
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun MetricCapsule(
    label: String,
    primaryValue: String,
    secondaryValue: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (secondaryValue != null) {
                    Text(
                        text = secondaryValue,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryExpressiveFooter(
    timestampMillis: Long,
    modifier: Modifier = Modifier
) {
    val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestampMillis))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Telemetry Synchronized • $formattedTime",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "100% On-Device Instrumentation • Zero Network Permissions",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

private val RefreshVectorIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Refresh",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null,
            strokeLineWidth = 0f
        ) {
            moveTo(17.65f, 6.35f)
            curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f)
            curveToRelative(-4.42f, 0f, -7.99f, 3.58f, -7.99f, 8f)
            reflectiveCurveToRelative(3.57f, 8f, 7.99f, 8f)
            curveToRelative(3.73f, 0f, 6.84f, -2.55f, 7.73f, -6f)
            horizontalLineToRelative(-2.08f)
            curveToRelative(-0.82f, 2.33f, -3.04f, 4f, -5.65f, 4f)
            curveToRelative(-3.31f, 0f, -6f, -2.69f, -6f, -6f)
            reflectiveCurveToRelative(2.69f, -6f, 6f, -6f)
            curveToRelative(1.66f, 0f, 3.14f, 0.69f, 4.22f, 1.78f)
            lineTo(13f, 11f)
            horizontalLineToRelative(7f)
            verticalLineTo(4f)
            lineToRelative(-2.35f, 2.35f)
            close()
        }
    }.build()
}

@Preview(showBackground = true)
@Composable
fun DashboardExpressivePreview() {
    val sampleTelemetry = DashboardTelemetry(
        device = DeviceSpecs(
            manufacturer = "Google",
            model = "Pixel 8 Pro",
            device = "husky",
            androidVersion = "15",
            apiLevel = 35,
            supportedAbis = listOf("arm64-v8a"),
            processorCount = 8
        ),
        memory = MemorySpecs(
            totalBytes = 12_884_901_888L,
            availableBytes = 4_509_715_660L,
            usedBytes = 8_375_186_228L,
            usedPercentage = 65.0f,
            isLowMemory = false
        ),
        storage = StorageSpecs(
            totalBytes = 256_000_000_000L,
            availableBytes = 140_800_000_000L,
            usedBytes = 115_200_000_000L,
            usedPercentage = 45.0f
        ),
        battery = BatterySpecs(
            percentage = 84,
            chargingStatus = ChargingStatus.CHARGING,
            pluggedType = PluggedType.AC,
            health = BatteryHealth.GOOD,
            temperatureCelsius = 29.4f,
            voltageMillivolts = 4150
        ),
        thermal = ThermalSpecs(ThermalStatus.NONE),
        display = DisplayBasicSpecs(
            widthPixels = 1344,
            heightPixels = 2992,
            densityDpi = 480,
            densityScale = 3.0f,
            refreshRateHz = 120.0f
        ),
        timestampMillis = System.currentTimeMillis()
    )

    DeviceLabTheme {
        DashboardScreen(
            uiState = DashboardUiState.Success(sampleTelemetry),
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardLoadingPreview() {
    DeviceLabTheme {
        DashboardScreen(
            uiState = DashboardUiState.Loading,
            onRefresh = {}
        )
    }
}

