package com.thermoheal.ai.data.bluetooth

import com.thermoheal.ai.domain.model.SensorFeatureVector
import com.thermoheal.ai.domain.model.SessionType
import com.thermoheal.ai.domain.repository.AIRepository
import com.thermoheal.ai.domain.repository.SensorRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs for the lifetime of the process (started once from ThermoHealApplication).
 * Subscribes to whatever [BleRepository] is bound (mock simulator today, real
 * hardware later) and persists every reading, then every ~15 readings derives
 * a feature vector and asks the AI engine for a fresh wellness insight — this
 * is what powers the Live Monitoring and Insights screens without any single
 * screen having to own the ingestion loop itself.
 */
@Singleton
class SensorIngestionManager @Inject constructor(
    private val bleRepository: BleRepository,
    private val sensorRepository: SensorRepository,
    private val aiRepository: AIRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var readingCount = 0
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            bleRepository.observeIncomingReadings().collect { reading ->
                sensorRepository.insertReading(reading)
                readingCount++
                if (readingCount % 15 == 0) {
                    generateInsightFromRecentWindow(reading.sessionType)
                }
            }
        }
    }

    private suspend fun generateInsightFromRecentWindow(sessionType: SessionType) {
        val since = System.currentTimeMillis() - 5 * 60_000
        val pressure = sensorRepository.observePressureReadings(since).first()
        val temperature = sensorRepository.observeTemperatureReadings(since).first()
        val moisture = sensorRepository.observeMoistureReadings(since).first()
        val gait = sensorRepository.observeGaitReadings(since).first()

        val peakPressure = pressure.maxOfOrNull { it.peak } ?: 0.0
        val avgPressure = pressure.map { it.average }.let { if (it.isEmpty()) 0.0 else it.average() }
        val features = SensorFeatureVector(
            peakPressure = peakPressure,
            averagePressure = avgPressure,
            pressureDurationMinutes = pressure.size / 6, // ~6 samples/min at 10s cadence in this window
            pressureAsymmetryPercent = kotlin.math.abs(
                (pressure.filter { it.side.name == "LEFT" }.map { it.average }.average().takeIf { !it.isNaN() } ?: 0.0) -
                    (pressure.filter { it.side.name == "RIGHT" }.map { it.average }.average().takeIf { !it.isNaN() } ?: 0.0)
            ) / (avgPressure.takeIf { it > 0 } ?: 1.0) * 100.0,
            currentTemperature = temperature.lastOrNull()?.temperature ?: 0.0,
            meanTemperature = temperature.map { it.temperature }.let { if (it.isEmpty()) 0.0 else it.average() },
            temperatureSlope = if (temperature.size >= 2) temperature.last().temperature - temperature.first().temperature else 0.0,
            currentMoisture = moisture.lastOrNull()?.moisture ?: 0.0,
            moistureSlope = if (moisture.size >= 2) moisture.last().moisture - moisture.first().moisture else 0.0,
            accumulatedMoistureMinutes = moisture.count { it.moisture > 30.0 },
            stepCount = 0,
            cadence = gait.map { it.cadence }.let { if (it.isEmpty()) 0.0 else it.average() },
            activeDurationMinutes = gait.count { it.cadence > 0 },
            standingDurationMinutes = gait.count { it.cadence == 0.0 },
            gaitBaselineDeviationPercent = gait.map { kotlin.math.abs(it.leftRightBalance) }.let { if (it.isEmpty()) 0.0 else it.average() }
        )
        aiRepository.generateWellnessInsight(features, sessionType)
    }
}
