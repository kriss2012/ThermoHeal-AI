package com.thermoheal.ai.domain.model

/** Identifies where a reading originated, so demo/real/research data is never mixed. */
enum class SessionType { DEMO, REAL_DEVICE, RESEARCH }

enum class ActivityState { RESTING, STANDING, WALKING, RUNNING, UNKNOWN }

enum class FootSide { LEFT, RIGHT, BOTH }

enum class SensorQuality { GOOD, FAIR, POOR, INVALID }

enum class CalibrationState {
    NOT_CALIBRATED, PREPARING, COLLECTING, PROCESSING, CALIBRATED, FAILED
}

data class CalibrationRecord(
    val calibrationId: String,
    val deviceId: String,
    val sensorType: String,
    val baseline: Double,
    val timestamp: Long,
    val quality: SensorQuality,
    val firmwareVersion: String
)

data class MonitoringSession(
    val sessionId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val source: SessionType,
    val createdAt: Long = System.currentTimeMillis()
)

/** Composite reading pushed from the (real or simulated) insole roughly once per second. */
data class SensorReading(
    val id: Long = 0,
    val userId: String = "local_user",
    val sessionId: String = "default_session",
    val timestamp: Long,
    val leftPressure: Double,   // kPa, aggregate
    val rightPressure: Double,  // kPa, aggregate
    val temperature: Double,    // °C
    val moisture: Double,       // % relative
    val steps: Int,
    val activityState: ActivityState,
    val batteryLevel: Int,      // %
    val sessionType: SessionType,
    val isValid: Boolean = true,
    val quality: SensorQuality = SensorQuality.GOOD,
    val qualityWarning: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/** Zone-level pressure map, one entry per anatomical zone, per foot. */
data class PressureReading(
    val id: Long = 0,
    val userId: String = "local_user",
    val sessionId: String = "default_session",
    val timestamp: Long,
    val side: FootSide,
    val heel: Double,
    val arch: Double,
    val midfoot: Double,
    val forefoot: Double,
    val bigToe: Double,
    val lesserToes: Double,
    val sessionType: SessionType,
    val createdAt: Long = System.currentTimeMillis()
) {
    val peak: Double get() = listOf(heel, arch, midfoot, forefoot, bigToe, lesserToes).max()
    val average: Double get() = listOf(heel, arch, midfoot, forefoot, bigToe, lesserToes).average()
}

data class TemperatureReading(
    val id: Long = 0,
    val userId: String = "local_user",
    val sessionId: String = "default_session",
    val timestamp: Long,
    val temperature: Double,
    val sessionType: SessionType,
    val createdAt: Long = System.currentTimeMillis()
)

data class MoistureReading(
    val id: Long = 0,
    val userId: String = "local_user",
    val sessionId: String = "default_session",
    val timestamp: Long,
    val moisture: Double,
    val sessionType: SessionType,
    val createdAt: Long = System.currentTimeMillis()
)

data class GaitReading(
    val id: Long = 0,
    val userId: String = "local_user",
    val sessionId: String = "default_session",
    val timestamp: Long,
    val leftRightBalance: Double, // -100..100, negative = left-dominant
    val cadence: Double,          // steps/min
    val stridePattern: String,    // e.g. "REGULAR", "IRREGULAR"
    val sessionType: SessionType,
    val createdAt: Long = System.currentTimeMillis()
)

/** Named anatomical zones used by the interactive foot heatmap (section 14). */
enum class FootZone(val displayName: String) {
    HEEL("Heel"),
    ARCH("Arch"),
    MIDFOOT("Midfoot"),
    FOREFOOT("Forefoot"),
    BIG_TOE("Big Toe"),
    LESSER_TOES("Lesser Toes")
}

enum class PressureLevel { LOW, MEDIUM, MEDIUM_HIGH, HIGH }
enum class Trend { INCREASING, DECREASING, STABLE }

data class ZoneDetail(
    val zone: FootZone,
    val side: FootSide,
    val level: PressureLevel,
    val durationMinutes: Int,
    val trend: Trend,
    val insight: String?
)
