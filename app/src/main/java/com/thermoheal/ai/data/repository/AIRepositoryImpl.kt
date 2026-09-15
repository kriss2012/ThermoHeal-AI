package com.thermoheal.ai.data.repository

import com.thermoheal.ai.data.ai.AIEngine
import com.thermoheal.ai.data.local.dao.AIInsightDao
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    private val aiEngine: AIEngine,
    private val aiInsightDao: AIInsightDao
) : AIRepository {

    override fun observeInsights(): Flow<List<AIInsight>> =
        aiInsightDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun generateWellnessInsight(features: SensorFeatureVector, sessionType: SessionType): AIInsight {
        val baseline = getPersonalBaseline()
        val insight = aiEngine.generateWellnessInsight(features, baseline).copy(sessionType = sessionType)
        aiInsightDao.insert(insight.toEntity())
        return insight
    }

    override suspend fun calculateWellnessIndex(features: SensorFeatureVector): WellnessIndex =
        aiEngine.calculateWellnessIndex(features)

    override suspend fun getPersonalBaseline(): PersonalBaseline {
        // Prototype baseline: derived from the most recent insight history so it
        // still gives a stable, coherent "Personal Baseline" screen (section 24).
        val recent = aiInsightDao.observeAll().first()
        val tempInsight = recent.firstOrNull { it.category == InsightCategory.TEMPERATURE.name }
        return PersonalBaseline(
            pressureStable = recent.none { it.category == InsightCategory.PRESSURE.name && it.severity == InsightSeverity.ATTENTION.name },
            temperatureDeltaFromBaseline = if (tempInsight != null) 0.8 else 0.0,
            moistureStatus = if (recent.any { it.category == InsightCategory.MOISTURE.name }) "Elevated" else "Normal",
            gaitDeviationPercent = if (recent.any { it.category == InsightCategory.GAIT.name }) 12.0 else 3.0,
            establishedAt = recent.minOfOrNull { it.timestamp }
        )
    }

    override suspend fun clearInsights() = aiInsightDao.clearAll()
}
