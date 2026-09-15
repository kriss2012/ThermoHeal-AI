package com.thermoheal.ai.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class HistoryRange(val label: String, val millis: Long) {
    TODAY("Today", 24 * 60 * 60_000L),
    LAST_7("Last 7 Days", 7 * 24 * 60 * 60_000L),
    LAST_30("Last 30 Days", 30L * 24 * 60 * 60_000L)
}

enum class HistoryMetric(val label: String) { PRESSURE("Pressure"), TEMPERATURE("Temperature"), MOISTURE("Moisture"), GAIT("Gait") }

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _range = MutableStateFlow(HistoryRange.TODAY)
    val range: StateFlow<HistoryRange> = _range.asStateFlow()

    private val _metric = MutableStateFlow(HistoryMetric.PRESSURE)
    val metric: StateFlow<HistoryMetric> = _metric.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chartValues: StateFlow<List<Float>> = combine(_range, _metric) { r, m -> r to m }
        .flatMapLatest { (r, m) ->
            val since = System.currentTimeMillis() - r.millis
            when (m) {
                HistoryMetric.PRESSURE -> sensorRepository.observePressureReadings(since).map { list -> list.map { it.average.toFloat() } }
                HistoryMetric.TEMPERATURE -> sensorRepository.observeTemperatureReadings(since).map { list -> list.map { it.temperature.toFloat() } }
                HistoryMetric.MOISTURE -> sensorRepository.observeMoistureReadings(since).map { list -> list.map { it.moisture.toFloat() } }
                HistoryMetric.GAIT -> sensorRepository.observeGaitReadings(since).map { list -> list.map { it.leftRightBalance.toFloat() } }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRange(r: HistoryRange) { _range.value = r }
    fun setMetric(m: HistoryMetric) { _metric.value = m }
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val range by viewModel.range.collectAsState()
    val metric by viewModel.metric.collectAsState()
    val values by viewModel.chartValues.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

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
        ThermoCard(modifier = Modifier.fillMaxWidth()) {
            if (values.isEmpty()) {
                EmptyState(title = "No sensor history yet.", subtitle = "Data will appear here once monitoring begins for the selected range.")
            } else {
                Text("${metric.label} — ${range.label}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                ThermoLineChart(values = values, lineColor = MaterialTheme.colorScheme.primary, heightDp = 220)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Min: ${"%.1f".format(values.min())}", style = MaterialTheme.typography.labelSmall)
                    Text("Avg: ${"%.1f".format(values.average())}", style = MaterialTheme.typography.labelSmall)
                    Text("Max: ${"%.1f".format(values.max())}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(90.dp))
    }
}
