package com.thermoheal.ai

import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.usecase.ExportDataUseCase
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExportDataUseCaseTest {

    private lateinit var exportDataUseCase: ExportDataUseCase

    @Before
    fun setUp() {
        exportDataUseCase = ExportDataUseCase()
    }

    @Test
    fun testExportToCsvContainsExpectedHeaderAndRows() {
        val user = UserProfile(
            id = "test-user-1",
            name = "Alex Johnson",
            email = "alex@thermoheal.ai",
            ageYears = 32,
            heightCm = 175.0,
            weightKg = 72.0,
            footSizeEu = 42.0,
            dominantFoot = DominantFoot.RIGHT,
            activityLevel = ActivityLevel.ACTIVE,
            createdAt = System.currentTimeMillis()
        )

        val reading = SensorReading(
            userId = "test-user-1",
            sessionId = "session-1",
            timestamp = 1700000000000L,
            leftPressure = 45.2,
            rightPressure = 46.1,
            temperature = 31.2,
            moisture = 18.5,
            steps = 450,
            activityState = ActivityState.WALKING,
            batteryLevel = 95,
            sessionType = SessionType.DEMO
        )

        val csv = exportDataUseCase.exportToCsv(user, listOf(reading))

        assertTrue(csv.contains("Timestamp,IsoTime,UserId,SessionId"))
        assertTrue(csv.contains("Alex Johnson"))
        assertTrue(csv.contains("45.20,46.10,31.20,18.50,450"))
        assertTrue(csv.contains("WALKING"))
        assertTrue(csv.contains("research and wellness-monitoring prototype"))
    }

    @Test
    fun testExportToJsonProducesValidJsonWithDisclaimers() {
        val user = UserProfile(
            id = "test-user-2",
            name = "Jordan Lee",
            email = "jordan@thermoheal.ai",
            ageYears = 28,
            heightCm = 168.0,
            weightKg = 64.0,
            footSizeEu = 39.0,
            dominantFoot = DominantFoot.LEFT,
            activityLevel = ActivityLevel.MODERATE,
            createdAt = System.currentTimeMillis()
        )

        val reading = SensorReading(
            userId = "test-user-2",
            sessionId = "session-2",
            timestamp = 1700000000000L,
            leftPressure = 38.0,
            rightPressure = 39.0,
            temperature = 30.2,
            moisture = 14.0,
            steps = 100,
            activityState = ActivityState.STANDING,
            batteryLevel = 88,
            sessionType = SessionType.REAL_DEVICE
        )

        val jsonString = exportDataUseCase.exportToJson(user, listOf(reading))
        val json = JSONObject(jsonString)

        assertEquals("ThermoHeal-AI", json.getString("appName"))
        assertEquals("1.0.0", json.getString("version"))
        assertTrue(json.has("disclaimer"))
        assertTrue(json.has("userProfile"))
        assertTrue(json.has("sensorReadings"))

        val readings = json.getJSONArray("sensorReadings")
        assertEquals(1, readings.length())
        val first = readings.getJSONObject(0)
        assertEquals("REAL_DEVICE", first.getString("source"))
        assertEquals(38.0, first.getDouble("leftPressureKPa"), 0.01)
    }
}
