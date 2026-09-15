package com.thermoheal.ai.data.repository

import com.thermoheal.ai.data.local.dao.*
import com.thermoheal.ai.data.local.entity.DailySummaryEntity
import com.thermoheal.ai.domain.model.DailySummary
import com.thermoheal.ai.domain.model.SessionType
import com.thermoheal.ai.domain.model.WeeklySummary
import com.thermoheal.ai.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepositoryImpl @Inject constructor(
    private val summaryDao: SummaryDao,
    private val sensorReadingDao: SensorReadingDao,
    private val aiInsightDao: AIInsightDao
) : SummaryRepository {

    override fun observeDailySummary(date: String): Flow<DailySummary?> =
        summaryDao.observeDaily(date).map { it?.toDomain() }

    override fun observeWeeklySummaries(): Flow<List<WeeklySummary>> =
        summaryDao.observeWeekly().map { list ->
            list.map {
                WeeklySummary(
                    weekStartDate = it.weekStartDate, avgWellnessIndex = it.avgWellnessIndex,
                    bestDay = it.bestDay, mostActiveDay = it.mostActiveDay, mostStableDay = it.mostStableDay,
                    notableTrend = it.notableTrend, sessionType = SessionType.valueOf(it.sessionType)
                )
            }
        }

    override suspend fun recomputeDailySummary(date: String, sessionType: SessionType): DailySummary {
        val dayStart = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)?.time ?: System.currentTimeMillis()
        val readings = sensorReadingDao.observeSince(dayStart).first().filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.timestamp)) == date
        }
        val insights = aiInsightDao.observeAll().first().filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.timestamp)) == date
        }

        val avgTemp = readings.map { it.temperature }.let { if (it.isEmpty()) 30.5 else it.average() }
        val avgMoist = readings.map { it.moisture }.let { if (it.isEmpty()) 16.0 else it.average() }
        val totalPressure = readings.sumOf { it.leftPressure + it.rightPressure }
        val leftPercent = if (totalPressure > 0) readings.sumOf { it.leftPressure } / totalPressure * 100.0 else 50.0
        val steps = readings.maxOfOrNull { it.steps } ?: 0
        val activeMinutes = readings.count { it.activityState != "RESTING" }

        val hadAttention = insights.any { it.severity == "ATTENTION" || it.severity == "HIGH_ATTENTION" }
        val sentence = if (hadAttention)
            "Your foot-wellness metrics remained generally stable today, with increased pressure observed during prolonged standing."
        else
            "Your foot-wellness metrics remained stable and within your typical baseline range today."

        val entity = DailySummaryEntity(
            date = date,
            wellnessIndex = (100 - (if (hadAttention) 15 else 3)).coerceIn(0, 100),
            avgTemperature = avgTemp, avgMoisture = avgMoist, pressureBalanceLeftPercent = leftPercent,
            steps = steps, activeMinutes = activeMinutes, insightCount = insights.size,
            summarySentence = sentence, sessionType = sessionType.name
        )
        summaryDao.upsertDaily(entity)
        return entity.toDomain()
    }
}
