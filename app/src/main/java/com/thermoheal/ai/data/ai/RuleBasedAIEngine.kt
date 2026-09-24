package com.thermoheal.ai.data.ai

import com.thermoheal.ai.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * ThermoHeal Core AI Engine (Phase 60).
 *
 * Every generated insight uses cautious, non-medical microcopy per
 * section 81 and always ends with the wellness-only disclaimer.
 */
@Singleton
class RuleBasedAIEngine @Inject constructor() : AIEngine {

    override val engineLabel: String = "ThermoHeal Core AI Engine"

    private val disclaimer = "Wellness insight only — not a medical diagnosis."

    override fun analyzePressure(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight? {
        val prolonged = features.pressureDurationMinutes > 15
        val elevated = features.peakPressure > 65.0
        val asymmetric = features.pressureAsymmetryPercent > 20.0
        if (!prolonged && !elevated && !asymmetric) return null

        val severity = when {
            elevated && prolonged -> InsightSeverity.ATTENTION
            asymmetric -> InsightSeverity.OBSERVATION
            else -> InsightSeverity.OBSERVATION
        }
        val factors = listOf(
            ContributingFactor("Pressure duration", pctFrom(features.pressureDurationMinutes, 30)),
            ContributingFactor("Pressure asymmetry", pctFrom(features.pressureAsymmetryPercent, 40.0)),
            ContributingFactor("Recent activity", pctFrom(features.activeDurationMinutes, 60)),
            ContributingFactor("Temperature trend", pctFrom(features.temperatureSlope, 3.0))
        ).sortedByDescending { it.weightPercent }

        return AIInsight(
            timestamp = System.currentTimeMillis(),
            category = InsightCategory.PRESSURE,
            title = "Pressure Pattern Detected",
            severity = severity,
            confidencePercent = confidenceFrom(factors),
            observation = if (asymmetric)
                "Pressure has remained elevated and uneven between feet during the recent activity period."
            else
                "Pressure has remained elevated in one region during the recent activity period.",
            recommendation = "Consider changing posture, redistributing load, or taking a short rest.",
            contributingFactors = factors,
            sessionType = SessionType.REAL_DEVICE,
            disclaimer = disclaimer
        )
    }

    override fun analyzeTemperature(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight? {
        val delta = baseline?.temperatureDeltaFromBaseline ?: 0.0
        val warming = features.temperatureSlope > 1.0 || delta > 1.5
        if (!warming) return null

        val factors = listOf(
            ContributingFactor("Temperature trend", pctFrom(features.temperatureSlope, 4.0)),
            ContributingFactor("Baseline deviation", pctFrom(delta, 3.0)),
            ContributingFactor("Recent activity", pctFrom(features.activeDurationMinutes, 60))
        ).sortedByDescending { it.weightPercent }

        return AIInsight(
            timestamp = System.currentTimeMillis(),
            category = InsightCategory.TEMPERATURE,
            title = "Thermal Trend Detected",
            severity = if (delta > 2.5) InsightSeverity.ATTENTION else InsightSeverity.OBSERVATION,
            confidencePercent = confidenceFrom(factors),
            observation = "Foot temperature has increased compared with your recent baseline.",
            recommendation = "The passive thermoregulation layer is designed to buffer this; consider a short rest if you find it uncomfortable.",
            contributingFactors = factors,
            sessionType = SessionType.REAL_DEVICE,
            disclaimer = disclaimer
        )
    }

    override fun analyzeMoisture(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight? {
        val accumulating = features.currentMoisture > 30.0 && features.moistureSlope > 3.0
        if (!accumulating) return null

        val factors = listOf(
            ContributingFactor("Moisture level", pctFrom(features.currentMoisture, 60.0)),
            ContributingFactor("Accumulation duration", pctFrom(features.accumulatedMoistureMinutes, 40)),
            ContributingFactor("Recent activity", pctFrom(features.activeDurationMinutes, 60))
        ).sortedByDescending { it.weightPercent }

        return AIInsight(
            timestamp = System.currentTimeMillis(),
            category = InsightCategory.MOISTURE,
            title = "Moisture Accumulation Detected",
            severity = if (features.currentMoisture > 40.0) InsightSeverity.ATTENTION else InsightSeverity.OBSERVATION,
            confidencePercent = confidenceFrom(factors),
            observation = "Moisture accumulation detected around the monitored region.",
            recommendation = "Consider ventilation or a short break if comfortable.",
            contributingFactors = factors,
            sessionType = SessionType.REAL_DEVICE,
            disclaimer = disclaimer
        )
    }

    override fun analyzeGait(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight? {
        val deviated = features.gaitBaselineDeviationPercent > 15.0
        if (!deviated) return null

        val factors = listOf(
            ContributingFactor("Baseline deviation", pctFrom(features.gaitBaselineDeviationPercent, 40.0)),
            ContributingFactor("Cadence variability", pctFrom(features.cadence, 140.0)),
            ContributingFactor("Standing duration", pctFrom(features.standingDurationMinutes, 60))
        ).sortedByDescending { it.weightPercent }

        return AIInsight(
            timestamp = System.currentTimeMillis(),
            category = InsightCategory.GAIT,
            title = "Gait Pattern Variation",
            severity = if (features.gaitBaselineDeviationPercent > 25.0) InsightSeverity.ATTENTION else InsightSeverity.OBSERVATION,
            confidencePercent = confidenceFrom(factors),
            observation = "Your recent gait pattern differs from your personal baseline.",
            recommendation = "Continue normal activity; this will keep refining your personal baseline over time.",
            contributingFactors = factors,
            sessionType = SessionType.REAL_DEVICE,
            disclaimer = disclaimer
        )
    }

    override fun generateWellnessInsight(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight {
        // Priority: pressure > temperature > moisture > gait, first non-null wins for the "headline" insight.
        return analyzePressure(features, baseline)
            ?: analyzeTemperature(features, baseline)
            ?: analyzeMoisture(features, baseline)
            ?: analyzeGait(features, baseline)
            ?: AIInsight(
                timestamp = System.currentTimeMillis(),
                category = InsightCategory.ACTIVITY,
                title = "Normal Pattern",
                severity = InsightSeverity.NORMAL,
                confidencePercent = 92,
                observation = "All monitored metrics are within your recent baseline range.",
                recommendation = "No action needed — continue regular monitoring.",
                contributingFactors = listOf(
                    ContributingFactor("Pressure stability", 25),
                    ContributingFactor("Thermal stability", 25),
                    ContributingFactor("Moisture stability", 25),
                    ContributingFactor("Gait consistency", 25)
                ),
                sessionType = SessionType.REAL_DEVICE,
                disclaimer = disclaimer
            )
    }

    override fun calculateWellnessIndex(features: SensorFeatureVector): WellnessIndex {
        val pressureComponent = (100 - (features.pressureAsymmetryPercent * 1.2).coerceIn(0.0, 60.0) -
            ((features.peakPressure - 45.0).coerceAtLeast(0.0) * 0.8)).coerceIn(0.0, 100.0).roundToInt()

        val tempComponent = (100 - (Math.abs(features.temperatureSlope) * 12.0)).coerceIn(0.0, 100.0).roundToInt()

        val moistureComponent = (100 - (features.currentMoisture - 15.0).coerceAtLeast(0.0) * 1.8)
            .coerceIn(0.0, 100.0).roundToInt()

        val activityComponent = (60 + (features.activeDurationMinutes * 0.6)).coerceIn(0.0, 100.0).roundToInt()

        val gaitComponent = (100 - (features.gaitBaselineDeviationPercent * 2.0)).coerceIn(0.0, 100.0).roundToInt()

        val overall = ((pressureComponent + tempComponent + moistureComponent + activityComponent + gaitComponent) / 5.0)
            .roundToInt()

        return WellnessIndex(
            score = overall,
            pressureComponent = pressureComponent,
            temperatureComponent = tempComponent,
            moistureComponent = moistureComponent,
            activityComponent = activityComponent,
            gaitComponent = gaitComponent
        )
    }

    private fun pctFrom(value: Double, scaleMax: Double): Int =
        ((value.coerceAtLeast(0.0) / scaleMax) * 100.0).roundToInt().coerceIn(5, 95)

    private fun pctFrom(value: Int, scaleMax: Int): Int =
        ((value.coerceAtLeast(0) / scaleMax.toDouble()) * 100.0).roundToInt().coerceIn(5, 95)

    private fun confidenceFrom(factors: List<ContributingFactor>): Int =
        (55 + (factors.maxOfOrNull { it.weightPercent } ?: 0) * 0.4).roundToInt().coerceIn(50, 96)
}
