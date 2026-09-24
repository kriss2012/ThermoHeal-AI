package com.thermoheal.ai.domain.model

/** Never "diagnosis" — see spec sections 20, 41, 61, 82. */
enum class InsightSeverity(val label: String) {
    NORMAL("Normal"),
    OBSERVATION("Observation"),
    ATTENTION("Attention"),
    HIGH_ATTENTION("High Attention")
}

enum class InsightCategory { PRESSURE, TEMPERATURE, MOISTURE, GAIT, ACTIVITY, THERMAL }

/** One explainability factor contributing to an insight (section 23). */
data class ContributingFactor(
    val label: String,
    val weightPercent: Int // 0-100, relative contribution
)

data class AIInsight(
    val id: Long = 0,
    val userId: String = "local_user",
    val sessionId: String = "default_session",
    val timestamp: Long,
    val category: InsightCategory,
    val title: String,
    val severity: InsightSeverity,
    val confidencePercent: Int,       // "pattern confidence", not diagnostic accuracy
    val observation: String,
    val recommendation: String,
    val contributingFactors: List<ContributingFactor> = emptyList(),
    val sessionType: SessionType = SessionType.DEMO,
    val modelVersion: String = "rule-engine-1.0",
    val algorithmVersion: String = "FWI-1.0-PROD",
    val featureVersion: String = "v1",
    val disclaimer: String = "Wellness insight only — not a medical diagnosis.",
    val createdAt: Long = System.currentTimeMillis()
)

/** Feature vector consumed by the rule-based AI engine (section 21). */
data class SensorFeatureVector(
    val peakPressure: Double,
    val averagePressure: Double,
    val pressureDurationMinutes: Int,
    val pressureAsymmetryPercent: Double, // |left-right| relative diff
    val currentTemperature: Double,
    val meanTemperature: Double,
    val temperatureSlope: Double,         // °C / hour
    val currentMoisture: Double,
    val moistureSlope: Double,
    val accumulatedMoistureMinutes: Int,
    val stepCount: Int,
    val cadence: Double,
    val activeDurationMinutes: Int,
    val standingDurationMinutes: Int,
    val gaitBaselineDeviationPercent: Double
)

data class PersonalBaseline(
    val pressureStable: Boolean,
    val temperatureDeltaFromBaseline: Double,
    val moistureStatus: String,
    val gaitDeviationPercent: Double,
    val establishedAt: Long?
)

data class WellnessIndex(
    val score: Int, // 0-100, "Foot Wellness Index" — never "medical score"
    val pressureComponent: Int,
    val temperatureComponent: Int,
    val moistureComponent: Int,
    val activityComponent: Int,
    val gaitComponent: Int,
    val algorithmVersion: String = "FWI-1.0-PROD"
) {
    val label: String get() = when {
        score >= 90 -> "Excellent"
        score >= 75 -> "Good"
        score >= 60 -> "Moderate"
        else -> "Attention"
    }
}
