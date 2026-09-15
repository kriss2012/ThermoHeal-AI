package com.thermoheal.ai.domain.usecase

import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.repository.AIRepository
import com.thermoheal.ai.domain.repository.SensorRepository
import javax.inject.Inject
import kotlin.math.abs

/** Builds a [SensorFeatureVector] from recent raw readings (section 21: AI feature extraction). */
class ExtractFeaturesUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(
        pressureReadings: List<PressureReading>,
        temperatureReadings: List<TemperatureReading>,
        moistureReadings: List<MoistureReading>,
        gaitReadings: List<GaitReading>,
        recentSteps: Int,
        recentActiveMinutes: Int,
        recentStandingMinutes: Int
    ): SensorFeatureVector {
        val peakPressure = pressureReadings.maxOfOrNull { it.peak } ?: 0.0
        val avgPressure = pressureReadings.map { it.average }.let { if (it.isEmpty()) 0.0 else it.average() }
        val durationMinutes = pressureReadings.size // ~1 reading/min in aggregated windows
        val leftAvg = pressureReadings.filter { it.side == FootSide.LEFT }.map { it.average }.average().let { if (it.isNaN()) 0.0 else it }
        val rightAvg = pressureReadings.filter { it.side == FootSide.RIGHT }.map { it.average }.average().let { if (it.isNaN()) 0.0 else it }
        val asymmetry = if (leftAvg + rightAvg == 0.0) 0.0 else abs(leftAvg - rightAvg) / ((leftAvg + rightAvg) / 2.0) * 100.0

        val currentTemp = temperatureReadings.lastOrNull()?.temperature ?: 0.0
        val meanTemp = temperatureReadings.map { it.temperature }.let { if (it.isEmpty()) 0.0 else it.average() }
        val tempSlope = if (temperatureReadings.size >= 2) {
            val first = temperatureReadings.first()
            val last = temperatureReadings.last()
            val hours = (last.timestamp - first.timestamp) / 3_600_000.0
            if (hours > 0) (last.temperature - first.temperature) / hours else 0.0
        } else 0.0

        val currentMoisture = moistureReadings.lastOrNull()?.moisture ?: 0.0
        val moistureSlope = if (moistureReadings.size >= 2) {
            moistureReadings.last().moisture - moistureReadings.first().moisture
        } else 0.0
        val accumulatedMoistureMinutes = moistureReadings.count { it.moisture > 30.0 }

        val cadence = gaitReadings.map { it.cadence }.let { if (it.isEmpty()) 0.0 else it.average() }
        val gaitDeviation = gaitReadings.map { abs(it.leftRightBalance) }.let { if (it.isEmpty()) 0.0 else it.average() }

        return SensorFeatureVector(
            peakPressure = peakPressure,
            averagePressure = avgPressure,
            pressureDurationMinutes = durationMinutes,
            pressureAsymmetryPercent = asymmetry,
            currentTemperature = currentTemp,
            meanTemperature = meanTemp,
            temperatureSlope = tempSlope,
            currentMoisture = currentMoisture,
            moistureSlope = moistureSlope,
            accumulatedMoistureMinutes = accumulatedMoistureMinutes,
            stepCount = recentSteps,
            cadence = cadence,
            activeDurationMinutes = recentActiveMinutes,
            standingDurationMinutes = recentStandingMinutes,
            gaitBaselineDeviationPercent = gaitDeviation
        )
    }
}

class GenerateInsightUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(features: SensorFeatureVector, sessionType: SessionType): AIInsight =
        aiRepository.generateWellnessInsight(features, sessionType)
}

class CalculateWellnessIndexUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(features: SensorFeatureVector): WellnessIndex =
        aiRepository.calculateWellnessIndex(features)
}

class GetSensorHistoryUseCase @Inject constructor(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(sinceEpochMillis: Long) = sensorRepository.observeReadingsSince(sinceEpochMillis)
}
