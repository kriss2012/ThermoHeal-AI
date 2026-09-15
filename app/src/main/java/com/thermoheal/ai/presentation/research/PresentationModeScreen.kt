package com.thermoheal.ai.presentation.research

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.data.bluetooth.DemoDataSimulator
import com.thermoheal.ai.domain.model.SensorReading
import com.thermoheal.ai.domain.repository.DeviceRepository
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.ThermoLineChart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class PresentationPage(val title: String, val body: String)

private val pages = listOf(
    PresentationPage("The Problem", "Prolonged standing, uneven loading, heat and moisture build-up affect foot comfort and long-term wellness — and most people never see this data."),
    PresentationPage("Sustainable Solution", "A smart insole built from banana pseudostem-derived cellulose, with passive bio-based PCM thermoregulation."),
    PresentationPage("Smart Insole Architecture", "Six functional layers: comfort, thermoregulation, sensing, structural biomaterial, cushioning, and electronics."),
    PresentationPage("Sensor Data", "Pressure, temperature, moisture and gait are sampled continuously and streamed over low-power BLE."),
    PresentationPage("AI Pipeline", "Raw data → validation → preprocessing → feature extraction → pattern recognition → wellness insight."),
    PresentationPage("Live Simulation", "Research Demo Mode — realistic simulated sensor data, streamed live."),
    PresentationPage("Wellness Insights", "Personalized, explainable insights with a confidence score and contributing factors — never a diagnosis."),
    PresentationPage("Validation Roadmap", "From material characterization through thermal, sensor, and mechanical testing, to AI validation and user studies."),
    PresentationPage("Circular Economy", "Agricultural waste → bioresource → biomaterial → healthcare product → circular bioeconomy."),
    PresentationPage("Future Roadmap", "Prototype → Characterization → AI Dataset → Human Validation → Scale-up / IP."),
)

private val scriptSteps = listOf(
    "Sensor connection established.",
    "Collecting multimodal foot data.",
    "Preprocessing sensor signals.",
    "Extracting pressure, thermal and moisture features.",
    "AI pattern analysis.",
    "Generating wellness insight.",
    "Actionable recommendation."
)

@HiltViewModel
class PresentationModeViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val demoDataSimulator: DemoDataSimulator,
    sensorRepository: SensorRepository
) : ViewModel() {
    private val _pageIndex = kotlinx.coroutines.flow.MutableStateFlow(0)
    val pageIndex: StateFlow<Int> = _pageIndex

    private val _isPlaying = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    val recentReadings: StateFlow<List<SensorReading>> = sensorRepository.observeReadingsSince(0)
        .map { it.takeLast(60) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun start() {
        if (_isPlaying.value) return
        _isPlaying.value = true
        _pageIndex.value = 0
        viewModelScope.launch {
            deviceRepository.connect("DEMO-INSOLE-0001")
            demoDataSimulator.startSimulation()
            for (i in pages.indices) {
                _pageIndex.value = i
                delay(4200)
                if (!_isPlaying.value) return@launch
            }
            _isPlaying.value = false
        }
    }

    fun stop() {
        _isPlaying.value = false
        demoDataSimulator.stopSimulation()
    }

    fun goTo(index: Int) { _pageIndex.value = index.coerceIn(0, pages.lastIndex) }
}

@Composable
fun PresentationModeScreen(onExit: () -> Unit, viewModel: PresentationModeViewModel = hiltViewModel()) {
    val pageIndex by viewModel.pageIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val readings by viewModel.recentReadings.collectAsState()
    val page = pages[pageIndex]

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF07191A)).padding(28.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "THERMOHEAL-AI",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF63E6D8),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onExit) { Text("Exit", color = Color(0xFFEAF8F5)) }
            }

            Spacer(Modifier.height(24.dp))

            AnimatedContent(targetState = pageIndex, label = "presentationPage") { idx ->
                val p = pages[idx]
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "STEP ${(idx + 1).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelMedium, color = Color(0xFF9AB5B1)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(p.title, style = MaterialTheme.typography.headlineMedium, color = Color(0xFFEAF8F5), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(p.body, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFCFE7E3))
                }
            }

            Spacer(Modifier.height(28.dp))

            if (page.title == "Live Simulation" && readings.isNotEmpty()) {
                Text("LIVE SENSOR DATA", style = MaterialTheme.typography.labelMedium, color = Color(0xFF63E6D8))
                Spacer(Modifier.height(8.dp))
                ThermoLineChart(values = readings.map { it.leftPressure.toFloat() }, lineColor = Color(0xFF63E6D8), heightDp = 120)
            }

            if (page.title == "AI Pipeline") {
                Column {
                    scriptSteps.forEachIndexed { i, step ->
                        Text("• $step", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCFE7E3), modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            LinearProgressIndicator(
                progress = { (pageIndex + 1) / pages.size.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF63E6D8),
                trackColor = Color(0xFF123334)
            )
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { if (isPlaying) viewModel.stop() else viewModel.start() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20A6A0))
                ) {
                    Icon(if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isPlaying) "Stop Demonstration" else "Start Demonstration")
                }
                OutlinedButton(onClick = { viewModel.goTo(pageIndex - 1) }, enabled = pageIndex > 0) { Text("Prev") }
                OutlinedButton(onClick = { viewModel.goTo(pageIndex + 1) }, enabled = pageIndex < pages.lastIndex) { Text("Next") }
            }
        }
    }
}
