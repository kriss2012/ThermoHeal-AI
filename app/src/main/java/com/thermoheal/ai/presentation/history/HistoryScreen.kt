package com.thermoheal.ai.presentation.history

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.domain.repository.UserRepository
import com.thermoheal.ai.domain.usecase.ExportDataUseCase
import com.thermoheal.ai.ui.components.*
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class HistoryRange(val label: String, val millis: Long) {
    TODAY("Today", 24 * 60 * 60_000L),
    LAST_7("Last 7 Days", 7 * 24 * 60 * 60_000L),
    LAST_30("Last 30 Days", 30L * 24 * 60 * 60_000L)
}

enum class HistoryMetric(val label: String, val unit: String) {
    PRESSURE("Pressure", "kPa"),
    TEMPERATURE("Temperature", "°C"),
    MOISTURE("Moisture", "%"),
    GAIT("Gait Balance", "%")
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val userRepository: UserRepository,
    private val exportDataUseCase: ExportDataUseCase
) : ViewModel() {

    private val _range = MutableStateFlow(HistoryRange.TODAY)
    val range: StateFlow<HistoryRange> = _range.asStateFlow()

    private val _metric = MutableStateFlow(HistoryMetric.PRESSURE)
    val metric: StateFlow<HistoryMetric> = _metric.asStateFlow()

    val userProfile = userRepository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chartValues: StateFlow<List<Float>> = combine(_range, _metric) { r, m -> r to m }
        .flatMapLatest { (r, m) ->
            val since = System.currentTimeMillis() - r.millis
            val flow = when (m) {
                HistoryMetric.PRESSURE -> sensorRepository.observePressureReadings(since).map { list -> list.map { it.average.toFloat() } }
                HistoryMetric.TEMPERATURE -> sensorRepository.observeTemperatureReadings(since).map { list -> list.map { it.temperature.toFloat() } }
                HistoryMetric.MOISTURE -> sensorRepository.observeMoistureReadings(since).map { list -> list.map { it.moisture.toFloat() } }
                HistoryMetric.GAIT -> sensorRepository.observeGaitReadings(since).map { list -> list.map { it.leftRightBalance.toFloat() } }
            }
            flow.map { downsample(it, maxPoints = 80) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRange(r: HistoryRange) { _range.value = r }
    fun setMetric(m: HistoryMetric) { _metric.value = m }

    fun export(context: Context, format: String) {
        viewModelScope.launch {
            val readings = sensorRepository.observeRecentReadings(500).first()
            val user = userProfile.value
            val content = when (format) {
                "CSV" -> exportDataUseCase.exportToCsv(user, readings)
                "JSON" -> exportDataUseCase.exportToJson(user, readings)
                else -> ""
            }

            try {
                val fileName = "thermoheal_export_${System.currentTimeMillis()}.${format.lowercase()}"
                val file = File(context.cacheDir, fileName)
                file.writeText(content)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (format == "CSV") "text/csv" else "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "ThermoHeal-AI Sensor History")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(intent, "Share Sensor Data Export"))
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun downsample(list: List<Float>, maxPoints: Int): List<Float> {
        if (list.size <= maxPoints) return list
        val chunkSize = (list.size + maxPoints - 1) / maxPoints
        return list.chunked(chunkSize).map { chunk -> chunk.average().toFloat() }
    }
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val range by viewModel.range.collectAsStateWithLifecycle()
    val metric by viewModel.metric.collectAsStateWithLifecycle()
    val values by viewModel.chartValues.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val windowSize = LocalWindowSizeInfo.current
    val isMultiPane = windowSize.widthClass != WindowWidthClass.COMPACT || windowSize.isLandscape

    val filterControls: @Composable () -> Unit = {
        Column {
            Text("Time Range", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryRange.entries.forEach { r ->
                    FilterChip(selected = range == r, onClick = { viewModel.setRange(r) }, label = { Text(r.label) })
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Metric", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryMetric.entries.forEach { m ->
                    FilterChip(selected = metric == m, onClick = { viewModel.setMetric(m) }, label = { Text(m.label) })
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Export Dataset")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.export(context, "CSV") },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export CSV")
                }
                OutlinedButton(
                    onClick = { viewModel.export(context, "JSON") },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Filled.DataObject, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export JSON")
                }
            }
        }
    }

    val chartCard: @Composable () -> Unit = {
        ThermoCard(modifier = Modifier.fillMaxWidth()) {
            if (values.isEmpty()) {
                EmptyState(title = "No sensor history yet.", subtitle = "Data will appear here once monitoring begins for the selected range.")
            } else {
                Text("${metric.label} — ${range.label}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                ThermoLineChart(values = values, lineColor = MaterialTheme.colorScheme.primary, heightDp = windowSize.chartHeightDp, unitLabel = metric.unit)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Min: ${"%.1f %s".format(values.min(), metric.unit)}", style = MaterialTheme.typography.labelSmall)
                    Text("Avg: ${"%.1f %s".format(values.average(), metric.unit)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("Max: ${"%.1f %s".format(values.max(), metric.unit)}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .readableContentWidth(maxDp = 1100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(windowSize.contentPadding)
        ) {
            Text("Sensor History & Telemetry Logs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (isMultiPane) {
                // Two-Column layout on Tablets & Landscape
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(modifier = Modifier.weight(1.05f)) {
                        filterControls()
                    }
                    Column(modifier = Modifier.weight(1.35f)) {
                        chartCard()
                    }
                }
            } else {
                // Single Column on Compact Phones
                filterControls()
                Spacer(Modifier.height(20.dp))
                chartCard()
            }

            Spacer(Modifier.height(84.dp))
        }
    }
}
