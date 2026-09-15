package com.thermoheal.ai.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.data.bluetooth.DemoDataSimulator
import com.thermoheal.ai.data.bluetooth.DemoScenario
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.repository.*
import com.thermoheal.ai.domain.usecase.CalculateWellnessIndexUseCase
import com.thermoheal.ai.domain.usecase.ExtractFeaturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val sensorRepository: SensorRepository,
    private val aiRepository: AIRepository,
    private val summaryRepository: SummaryRepository,
    private val demoDataSimulator: DemoDataSimulator,
    private val extractFeatures: ExtractFeaturesUseCase,
    private val calculateWellnessIndex: CalculateWellnessIndexUseCase
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = userRepository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val deviceState: StateFlow<DeviceInfo?> = deviceRepository.observeDeviceState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestReading: StateFlow<SensorReading?> = sensorRepository.observeLatestReading()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestInsight: StateFlow<AIInsight?> = aiRepository.observeInsights()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isDemoRunning: StateFlow<Boolean> = demoDataSimulator.isRunning
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val demoScenario: StateFlow<DemoScenario> = demoDataSimulator.scenario
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DemoScenario.NORMAL)

    private val _wellnessIndex = MutableStateFlow<WellnessIndex?>(null)
    val wellnessIndex: StateFlow<WellnessIndex?> = _wellnessIndex.asStateFlow()

    val dailySummary: StateFlow<DailySummary?> = summaryRepository
        .observeDailySummary(today())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Periodically recompute the wellness index + daily summary from the recent window,
        // so the Home screen stays live while Demo Mode (or real hardware) is streaming.
        viewModelScope.launch {
            while (true) {
                recompute()
                delay(4000)
            }
        }
    }

    private suspend fun recompute() {
        val since = System.currentTimeMillis() - 5 * 60_000
        val pressure = sensorRepository.observePressureReadings(since).first()
        val temperature = sensorRepository.observeTemperatureReadings(since).first()
        val moisture = sensorRepository.observeMoistureReadings(since).first()
        val gait = sensorRepository.observeGaitReadings(since).first()

        if (pressure.isEmpty() && temperature.isEmpty()) return // nothing streamed yet

        val features = extractFeatures(
            pressureReadings = pressure, temperatureReadings = temperature,
            moistureReadings = moisture, gaitReadings = gait,
            recentSteps = latestReading.value?.steps ?: 0,
            recentActiveMinutes = gait.count { it.cadence > 0 },
            recentStandingMinutes = gait.count { it.cadence == 0.0 }
        )
        _wellnessIndex.value = calculateWellnessIndex(features)
        summaryRepository.recomputeDailySummary(today(), latestReading.value?.sessionType ?: SessionType.DEMO)
    }

    fun connectInsole() {
        viewModelScope.launch { deviceRepository.connect(deviceState.value?.id ?: "DEMO-INSOLE-0001") }
    }

    fun startDemoMonitoring() {
        viewModelScope.launch {
            if (deviceState.value?.connectionState != ConnectionState.CONNECTED) {
                deviceRepository.connect("DEMO-INSOLE-0001")
            }
            demoDataSimulator.startSimulation()
        }
    }

    fun stopDemoMonitoring() = demoDataSimulator.stopSimulation()

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
