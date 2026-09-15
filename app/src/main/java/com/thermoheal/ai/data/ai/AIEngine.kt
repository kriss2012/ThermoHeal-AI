package com.thermoheal.ai.data.ai

import com.thermoheal.ai.domain.model.*

/**
 * Modular AI service interface (spec section 2 / 60). The prototype ships
 * with [RuleBasedAIEngine]; a trained ML model can implement this same
 * interface later (e.g. TFLiteAIEngine) without touching any call site,
 * since ViewModels only depend on [com.thermoheal.ai.domain.repository.AIRepository].
 *
 * Pipeline (visualized in the AI Analysis screen):
 *   Raw Sensor Data -> Validation -> Preprocessing -> Feature Extraction
 *   -> Pattern Recognition -> Attention Classification -> Wellness Insight
 */
interface AIEngine {
    val engineLabel: String // e.g. "Prototype AI / Rule-based demonstration"

    fun analyzePressure(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight?
    fun analyzeTemperature(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight?
    fun analyzeMoisture(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight?
    fun analyzeGait(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight?
    fun generateWellnessInsight(features: SensorFeatureVector, baseline: PersonalBaseline?): AIInsight
    fun calculateWellnessIndex(features: SensorFeatureVector): WellnessIndex
}
