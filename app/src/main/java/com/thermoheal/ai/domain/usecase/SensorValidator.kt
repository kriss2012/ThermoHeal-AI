package com.thermoheal.ai.domain.usecase

import com.thermoheal.ai.domain.model.SensorQuality
import com.thermoheal.ai.domain.model.SensorReading
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production Sensor Validator & Sanitizer (Phase 13).
 * Rejects NaN, Infinity, corrupted packets, impossible sensor values,
 * and assigns a deterministic SensorQuality grade.
 */
@Singleton
class SensorValidator @Inject constructor() {

    fun validateReading(reading: SensorReading): SensorReading {
        // 1. Check for NaN or Infinite values
        val hasCorruptedNumbers = reading.leftPressure.isNaN() || reading.leftPressure.isInfinite() ||
            reading.rightPressure.isNaN() || reading.rightPressure.isInfinite() ||
            reading.temperature.isNaN() || reading.temperature.isInfinite() ||
            reading.moisture.isNaN() || reading.moisture.isInfinite()

        if (hasCorruptedNumbers) {
            return reading.copy(
                isValid = false,
                quality = SensorQuality.INVALID,
                qualityWarning = "Corrupted numerical data (NaN or Infinity) detected."
            )
        }

        // 2. Check for impossible physical limits
        val impossiblePressure = reading.leftPressure < 0.0 || reading.leftPressure > 600.0 ||
            reading.rightPressure < 0.0 || reading.rightPressure > 600.0
        val impossibleTemperature = reading.temperature < -20.0 || reading.temperature > 75.0
        val impossibleMoisture = reading.moisture < 0.0 || reading.moisture > 100.0
        val impossibleBattery = reading.batteryLevel < 0 || reading.batteryLevel > 100

        if (impossiblePressure || impossibleTemperature || impossibleMoisture || impossibleBattery) {
            val warning = when {
                impossiblePressure -> "Pressure reading outside physical range (0-600 kPa)."
                impossibleTemperature -> "Temperature outside operational sensor limits (-20 to 75°C)."
                impossibleMoisture -> "Moisture outside valid percentage bounds (0-100%)."
                else -> "Battery level out of range (0-100%)."
            }
            return reading.copy(
                isValid = false,
                quality = SensorQuality.INVALID,
                qualityWarning = warning
            )
        }

        // 3. Determine Sensor Quality Grade
        val warnings = mutableListOf<String>()
        var quality = SensorQuality.GOOD

        if (reading.batteryLevel < 15) {
            warnings.add("Low battery (<15%), sensor precision may degrade.")
            quality = SensorQuality.FAIR
        }
        if (reading.batteryLevel < 5) {
            warnings.add("Critical battery (<5%), telemetry unreliable.")
            quality = SensorQuality.POOR
        }
        if (reading.temperature > 48.0 || reading.temperature < 10.0) {
            warnings.add("Unusual temperature boundary reached.")
            if (quality == SensorQuality.GOOD) quality = SensorQuality.FAIR
        }
        if (reading.leftPressure > 250.0 || reading.rightPressure > 250.0) {
            warnings.add("High sensor stress detected.")
            if (quality == SensorQuality.GOOD) quality = SensorQuality.FAIR
        }

        val qualityWarning = if (warnings.isNotEmpty()) warnings.joinToString("; ") else null

        return reading.copy(
            isValid = quality != SensorQuality.POOR && quality != SensorQuality.INVALID,
            quality = quality,
            qualityWarning = qualityWarning
        )
    }

    /**
     * Sanitizes raw sensor values by clamping to safe operational ranges to prevent UI crash or ML corruption.
     */
    fun sanitizeReading(reading: SensorReading): SensorReading {
        val safeLeft = if (reading.leftPressure.isNaN() || reading.leftPressure.isInfinite()) 0.0 else reading.leftPressure.coerceIn(0.0, 500.0)
        val safeRight = if (reading.rightPressure.isNaN() || reading.rightPressure.isInfinite()) 0.0 else reading.rightPressure.coerceIn(0.0, 500.0)
        val safeTemp = if (reading.temperature.isNaN() || reading.temperature.isInfinite()) 30.0 else reading.temperature.coerceIn(-10.0, 60.0)
        val safeMoist = if (reading.moisture.isNaN() || reading.moisture.isInfinite()) 15.0 else reading.moisture.coerceIn(0.0, 100.0)
        val safeBattery = reading.batteryLevel.coerceIn(0, 100)

        return reading.copy(
            leftPressure = safeLeft,
            rightPressure = safeRight,
            temperature = safeTemp,
            moisture = safeMoist,
            batteryLevel = safeBattery
        )
    }
}
