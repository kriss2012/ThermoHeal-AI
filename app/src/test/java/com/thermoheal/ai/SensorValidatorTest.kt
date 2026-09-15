package com.thermoheal.ai

import com.thermoheal.ai.domain.model.ActivityState
import com.thermoheal.ai.domain.model.SensorQuality
import com.thermoheal.ai.domain.model.SensorReading
import com.thermoheal.ai.domain.model.SessionType
import com.thermoheal.ai.domain.usecase.SensorValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SensorValidatorTest {

    private lateinit var validator: SensorValidator

    @Before
    fun setUp() {
        validator = SensorValidator()
    }

    @Test
    fun testValidReadingAcceptedWithGoodQuality() {
        val reading = SensorReading(
            timestamp = System.currentTimeMillis(),
            leftPressure = 42.0,
            rightPressure = 43.5,
            temperature = 30.5,
            moisture = 16.0,
            steps = 250,
            activityState = ActivityState.WALKING,
            batteryLevel = 90,
            sessionType = SessionType.DEMO
        )

        val result = validator.validateReading(reading)

        assertTrue(result.isValid)
        assertEquals(SensorQuality.GOOD, result.quality)
        assertNull(result.qualityWarning)
    }

    @Test
    fun testNaNValuesMarkedInvalid() {
        val reading = SensorReading(
            timestamp = System.currentTimeMillis(),
            leftPressure = Double.NaN,
            rightPressure = 40.0,
            temperature = 30.0,
            moisture = 15.0,
            steps = 0,
            activityState = ActivityState.RESTING,
            batteryLevel = 80,
            sessionType = SessionType.REAL_DEVICE
        )

        val result = validator.validateReading(reading)

        assertFalse(result.isValid)
        assertEquals(SensorQuality.INVALID, result.quality)
        assertNotNull(result.qualityWarning)
    }

    @Test
    fun testNegativePressureMarkedInvalid() {
        val reading = SensorReading(
            timestamp = System.currentTimeMillis(),
            leftPressure = -15.0,
            rightPressure = 40.0,
            temperature = 30.0,
            moisture = 15.0,
            steps = 0,
            activityState = ActivityState.RESTING,
            batteryLevel = 80,
            sessionType = SessionType.REAL_DEVICE
        )

        val result = validator.validateReading(reading)

        assertFalse(result.isValid)
        assertEquals(SensorQuality.INVALID, result.quality)
    }

    @Test
    fun testLowBatteryDowngradesQualityToFair() {
        val reading = SensorReading(
            timestamp = System.currentTimeMillis(),
            leftPressure = 40.0,
            rightPressure = 40.0,
            temperature = 30.0,
            moisture = 15.0,
            steps = 10,
            activityState = ActivityState.WALKING,
            batteryLevel = 12, // < 15%
            sessionType = SessionType.DEMO
        )

        val result = validator.validateReading(reading)

        assertTrue(result.isValid)
        assertEquals(SensorQuality.FAIR, result.quality)
        assertNotNull(result.qualityWarning)
    }

    @Test
    fun testSanitizeClampsExcessiveValues() {
        val extremeReading = SensorReading(
            timestamp = System.currentTimeMillis(),
            leftPressure = 9999.0,
            rightPressure = -50.0,
            temperature = 120.0,
            moisture = 150.0,
            steps = 0,
            activityState = ActivityState.RESTING,
            batteryLevel = 150,
            sessionType = SessionType.DEMO
        )

        val sanitized = validator.sanitizeReading(extremeReading)

        assertEquals(500.0, sanitized.leftPressure, 0.01)
        assertEquals(0.0, sanitized.rightPressure, 0.01)
        assertEquals(60.0, sanitized.temperature, 0.01)
        assertEquals(100.0, sanitized.moisture, 0.01)
        assertEquals(100, sanitized.batteryLevel)
    }
}
