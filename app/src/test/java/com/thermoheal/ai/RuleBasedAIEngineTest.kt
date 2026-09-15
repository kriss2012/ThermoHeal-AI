package com.thermoheal.ai

import com.thermoheal.ai.data.ai.RuleBasedAIEngine
import com.thermoheal.ai.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RuleBasedAIEngineTest {

    private lateinit var aiEngine: RuleBasedAIEngine

    @Before
    fun setUp() {
        aiEngine = RuleBasedAIEngine()
    }

    @Test
    fun testNormalPatternReturnsExpectedWellnessInsight() {
        val normalFeatures = SensorFeatureVector(
            peakPressure = 42.0,
            averagePressure = 40.0,
            pressureDurationMinutes = 5,
            pressureAsymmetryPercent = 5.0,
            currentTemperature = 30.5,
            meanTemperature = 30.4,
            temperatureSlope = 0.1,
            currentMoisture = 16.0,
            moistureSlope = 0.2,
            accumulatedMoistureMinutes = 0,
            stepCount = 120,
            cadence = 105.0,
            activeDurationMinutes = 10,
            standingDurationMinutes = 2,
            gaitBaselineDeviationPercent = 3.0
        )

        val insight = aiEngine.generateWellnessInsight(normalFeatures, null)

        assertNotNull(insight)
        assertEquals(InsightSeverity.NORMAL, insight.severity)
        assertEquals(InsightCategory.ACTIVITY, insight.category)
        assertTrue(insight.confidencePercent in 50..100)
        assertTrue(insight.disclaimer.contains("not a medical diagnosis", ignoreCase = true))
    }

    @Test
    fun testElevatedProlongedPressureTriggersAttention() {
        val highPressureFeatures = SensorFeatureVector(
            peakPressure = 75.0,
            averagePressure = 68.0,
            pressureDurationMinutes = 25,
            pressureAsymmetryPercent = 28.0,
            currentTemperature = 31.0,
            meanTemperature = 30.8,
            temperatureSlope = 0.2,
            currentMoisture = 18.0,
            moistureSlope = 0.1,
            accumulatedMoistureMinutes = 0,
            stepCount = 10,
            cadence = 0.0,
            activeDurationMinutes = 0,
            standingDurationMinutes = 30,
            gaitBaselineDeviationPercent = 4.0
        )

        val insight = aiEngine.analyzePressure(highPressureFeatures, null)

        assertNotNull(insight)
        assertEquals(InsightCategory.PRESSURE, insight?.category)
        assertEquals(InsightSeverity.ATTENTION, insight?.severity)
        assertTrue(insight?.contributingFactors?.isNotEmpty() == true)
        assertTrue(insight?.observation?.contains("elevated", ignoreCase = true) == true)
    }

    @Test
    fun testThermalRiseDetection() {
        val thermalFeatures = SensorFeatureVector(
            peakPressure = 42.0,
            averagePressure = 40.0,
            pressureDurationMinutes = 5,
            pressureAsymmetryPercent = 2.0,
            currentTemperature = 34.5,
            meanTemperature = 31.0,
            temperatureSlope = 2.5,
            currentMoisture = 20.0,
            moistureSlope = 0.5,
            accumulatedMoistureMinutes = 0,
            stepCount = 200,
            cadence = 90.0,
            activeDurationMinutes = 15,
            standingDurationMinutes = 5,
            gaitBaselineDeviationPercent = 3.0
        )

        val insight = aiEngine.analyzeTemperature(thermalFeatures, null)

        assertNotNull(insight)
        assertEquals(InsightCategory.TEMPERATURE, insight?.category)
        assertTrue(insight?.observation?.contains("temperature", ignoreCase = true) == true)
    }

    @Test
    fun testMoistureAccumulationDetection() {
        val moistureFeatures = SensorFeatureVector(
            peakPressure = 40.0,
            averagePressure = 38.0,
            pressureDurationMinutes = 5,
            pressureAsymmetryPercent = 2.0,
            currentTemperature = 31.2,
            meanTemperature = 31.0,
            temperatureSlope = 0.1,
            currentMoisture = 45.0,
            moistureSlope = 5.0,
            accumulatedMoistureMinutes = 15,
            stepCount = 300,
            cadence = 100.0,
            activeDurationMinutes = 20,
            standingDurationMinutes = 2,
            gaitBaselineDeviationPercent = 2.0
        )

        val insight = aiEngine.analyzeMoisture(moistureFeatures, null)

        assertNotNull(insight)
        assertEquals(InsightCategory.MOISTURE, insight?.category)
        assertEquals(InsightSeverity.ATTENTION, insight?.severity)
    }

    @Test
    fun testWellnessIndexBoundedBetween0And100() {
        val extremeFeatures = SensorFeatureVector(
            peakPressure = 150.0,
            averagePressure = 120.0,
            pressureDurationMinutes = 60,
            pressureAsymmetryPercent = 50.0,
            currentTemperature = 40.0,
            meanTemperature = 38.0,
            temperatureSlope = 5.0,
            currentMoisture = 90.0,
            moistureSlope = 10.0,
            accumulatedMoistureMinutes = 60,
            stepCount = 0,
            cadence = 0.0,
            activeDurationMinutes = 0,
            standingDurationMinutes = 120,
            gaitBaselineDeviationPercent = 40.0
        )

        val index = aiEngine.calculateWellnessIndex(extremeFeatures)

        assertTrue(index.score in 0..100)
        assertTrue(index.pressureComponent in 0..100)
        assertTrue(index.temperatureComponent in 0..100)
        assertTrue(index.moistureComponent in 0..100)
        assertTrue(index.activityComponent in 0..100)
        assertTrue(index.gaitComponent in 0..100)
        assertEquals("FWI-1.0-prototype", index.algorithmVersion)
    }
}
