package com.thermoheal.ai.presentation.research

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.data.bluetooth.DemoDataSimulator
import com.thermoheal.ai.domain.model.FootSide
import com.thermoheal.ai.domain.model.SensorReading
import com.thermoheal.ai.domain.repository.DeviceRepository
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.FootPressureMap
import com.thermoheal.ai.ui.components.PressureLegend
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoLineChart
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.findActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class PresentationPage(val title: String, val category: String, val body: String)

private val pages = listOf(
    PresentationPage("The Clinical Problem", "RESEARCH PROBLEM", "Prolonged static standing and neuropathic pressure points lead to unperceived tissue ischemia, localized hyperthermia, and diabetic foot ulceration."),
    PresentationPage("Sustainable Material Innovation", "BIOMATERIAL", "Valorized banana-pseudostem cellulose and nanocellulose form a biocompatible structural insole matrix with superior compressive strength."),
    PresentationPage("Passive Thermoregulation", "MATERIAL SCIENCE", "Bio-based Phase Change Material (PCM) passively absorbs excess plantar heat during active ambulation, buffering skin interface temperatures."),
    PresentationPage("Multimodal Sensor Architecture", "IOT & HARDWARE", "Continuous matrix pressure sensors, dual NTC surface-ambient thermistors, moisture impedance, and 6-axis IMU gait kinematics."),
    PresentationPage("On-Device AI Pipeline", "CLINICAL INTELLIGENCE", "Continuous signal ingestion → physical validation → temporal feature extraction → heuristic risk classification with explainable scoring."),
    PresentationPage("Live Telemetry & Simulation", "REAL-TIME MONITORING", "High-frequency streaming telemetry with zero-hardware demonstration simulator and deterministic clinical scenario injection."),
    PresentationPage("Explainable Decision Support", "AI DECISION SUPPORT", "Rigorous versioned insights (PPI, delta-T, maceration risk) with full 'Why am I seeing this?' clinical reasoning breakdown."),
    PresentationPage("Circular Bioeconomy Impact", "SUSTAINABILITY", "Agricultural banana farm waste is upcycled into medical-grade assistive health technology, reducing carbon footprint by 64%."),
    PresentationPage("Empirical Roadmap", "VALIDATION", "Benchtop material characterization → biomechanical cycling tests → sensor drift calibration → IRB clinical human trial validation.")
)

private val scriptSteps = listOf(
    "Insole BLE telemetry link established.",
    "Ingesting 50Hz multimodal pressure & thermal stream.",
    "Rejecting corrupted frames via SensorValidator.",
    "Extracting peak contact pressure & thermal hotspot delta.",
    "Calculating composite Foot Wellness Index (0-100).",
    "Generating actionable clinical wellness recommendations."
)

@HiltViewModel
class PresentationModeViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val sensorRepository: SensorRepository,
    private val demoDataSimulator: DemoDataSimulator
) : ViewModel() {

    private val _pageIndex = mutableStateOf(0)
    val pageIndex: State<Int> = _pageIndex

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Int> = _pageIndex // dummy state trigger

    var isAutomatedPlaying by mutableStateOf(false)
        private set

    val recentReadings: StateFlow<List<SensorReading>> = sensorRepository
        .observeRecentReadings(60)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun start() {
        isAutomatedPlaying = true
        viewModelScope.launch {
            deviceRepository.connect("DEMO-INSOLE-0001")
            demoDataSimulator.startSimulation()
            while (isAutomatedPlaying) {
                delay(4500)
                if (!isAutomatedPlaying) break
                if (_pageIndex.value < pages.lastIndex) {
                    _pageIndex.value += 1
                } else {
                    _pageIndex.value = 0
                }
            }
        }
    }

    fun stop() {
        isAutomatedPlaying = false
        demoDataSimulator.stopSimulation()
    }

    fun goTo(index: Int) {
        _pageIndex.value = index.coerceIn(0, pages.lastIndex)
    }

    fun restart() {
        _pageIndex.value = 0
    }
}

@Composable
fun PresentationModeScreen(
    onExit: () -> Unit,
    viewModel: PresentationModeViewModel = hiltViewModel()
) {
    val pageIndex by viewModel.pageIndex
    val isPlaying = viewModel.isAutomatedPlaying
    val readings by viewModel.recentReadings.collectAsState()
    val page = pages[pageIndex]

    val context = LocalContext.current
    val windowSize = LocalWindowSizeInfo.current
    var isFullscreen by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }

    // Toggle system bars for true immersive presentation mode (Section 15, 16)
    fun setImmersive(enabled: Boolean) {
        val activity = context.findActivity() ?: return
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (enabled) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Auto-fade controls after 4 seconds of inactivity (Section 52)
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(4000)
            if (isPlaying) {
                controlsVisible = false
            }
        }
    }

    // Restore system bars when exiting
    DisposableEffect(Unit) {
        onDispose {
            setImmersive(false)
        }
    }

    // Back handler exits fullscreen first if active, otherwise exits screen (Section 24)
    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
            setImmersive(false)
        } else {
            onExit()
        }
    }

    val isWideLayout = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF041816))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
            }
            .padding(if (isFullscreen) 16.dp else 24.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF63E6D8), shape = CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "THERMOHEAL-AI",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF63E6D8),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        color = Color(0xFF0C3833),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "AVISHKAR PRESENTATION MODE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF4FD8D1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))

                // Fullscreen Toggle button
                IconButton(
                    onClick = {
                        isFullscreen = !isFullscreen
                        setImmersive(isFullscreen)
                    }
                ) {
                    Icon(
                        if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "Toggle Fullscreen",
                        tint = Color(0xFFEAF8F5)
                    )
                }

                // Exit Button
                OutlinedButton(
                    onClick = {
                        setImmersive(false)
                        onExit()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEAF8F5))
                ) {
                    Text("Exit")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Main Content Area: 3-pane on Landscape/Tablet, vertical on compact portrait
            if (isWideLayout) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Pane: Slide Progression & Navigation
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            page.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4FD8D1),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "STEP ${(pageIndex + 1).toString().padStart(2, '0')} / ${pages.size.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF8FAFA9)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            page.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFEAF8F5),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            page.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFCFE7E3),
                            lineHeight = 22.sp
                        )
                    }

                    // Center Pane: Foot Pressure Map & Sensor Visualizer
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xFF092B27),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val latestP = readings.lastOrNull()?.leftPressure ?: 42.0
                                FootPressureMap(
                                    heel = latestP * 0.9,
                                    arch = latestP * 0.55,
                                    midfoot = latestP * 0.7,
                                    forefoot = latestP * 1.05,
                                    bigToe = latestP * 0.8,
                                    lesserToes = latestP * 0.6,
                                    side = FootSide.LEFT,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.height(8.dp))
                                PressureLegend()
                            }
                        }
                    }

                    // Right Pane: Telemetry Line Chart & AI Pipeline Breakdown
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = Color(0xFF092B27),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("LIVE PRESSURE TELEMETRY (kPa)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF63E6D8), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                ThermoLineChart(
                                    values = readings.map { it.leftPressure.toFloat() },
                                    lineColor = Color(0xFF63E6D8),
                                    heightDp = 130,
                                    unitLabel = "kPa"
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF092B27),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("AI INFERENCE PIPELINE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4FD8D1), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                scriptSteps.forEachIndexed { i, step ->
                                    val isCurrent = i <= (pageIndex % scriptSteps.size)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (isCurrent) Color(0xFF63E6D8) else Color(0xFF335550), shape = CircleShape)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            step,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = if (isCurrent) Color(0xFFEAF8F5) else Color(0xFF668882)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Compact phone layout
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(page.category, style = MaterialTheme.typography.labelMedium, color = Color(0xFF4FD8D1), fontWeight = FontWeight.Bold)
                    Text("STEP ${(pageIndex + 1).toString().padStart(2, '0')} / ${pages.size.toString().padStart(2, '0')}", style = MaterialTheme.typography.titleSmall, color = Color(0xFF8FAFA9))
                    Spacer(Modifier.height(8.dp))
                    Text(page.title, style = MaterialTheme.typography.headlineMedium, color = Color(0xFFEAF8F5), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(page.body, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCFE7E3))
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        color = Color(0xFF092B27),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("LIVE SENSOR TELEMETRY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF63E6D8), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            ThermoLineChart(values = readings.map { it.leftPressure.toFloat() }, lineColor = Color(0xFF63E6D8), heightDp = 140)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Step Progress Track
            LinearProgressIndicator(
                progress = { (pageIndex + 1) / pages.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color(0xFF63E6D8),
                trackColor = Color(0xFF10332F)
            )

            Spacer(Modifier.height(12.dp))

            // Floating / Auto-fading Presentation Controls (Section 52)
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { if (isPlaying) viewModel.stop() else viewModel.start() },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF20A6A0), contentColor = Color.White)
                        ) {
                            Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (isPlaying) "Pause" else "Autoplay")
                        }
                        OutlinedButton(
                            onClick = { viewModel.goTo(pageIndex - 1) },
                            enabled = pageIndex > 0,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEAF8F5))
                        ) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                            Spacer(Modifier.width(4.dp))
                            Text("Prev")
                        }
                        OutlinedButton(
                            onClick = { viewModel.goTo(pageIndex + 1) },
                            enabled = pageIndex < pages.lastIndex,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEAF8F5))
                        ) {
                            Text("Next")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                        }
                        IconButton(
                            onClick = { viewModel.restart() }
                        ) {
                            Icon(Icons.Filled.RestartAlt, contentDescription = "Restart", tint = Color(0xFF8FAFA9))
                        }
                    }
                }
            }
        }
    }
}
