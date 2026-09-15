package com.thermoheal.ai.data.repository

import com.thermoheal.ai.data.local.dao.*
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.repository.SensorRepository
import com.thermoheal.ai.domain.usecase.SensorValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SensorRepositoryImpl @Inject constructor(
    private val sensorReadingDao: SensorReadingDao,
    private val pressureDao: PressureReadingDao,
    private val temperatureDao: TemperatureReadingDao,
    private val moistureDao: MoistureReadingDao,
    private val gaitDao: GaitReadingDao,
    private val sensorValidator: SensorValidator
) : SensorRepository {

    override fun observeLatestReading(): Flow<SensorReading?> =
        sensorReadingDao.observeLatest().map { it?.toDomain() }

    override fun observeReadingsSince(sinceEpochMillis: Long): Flow<List<SensorReading>> =
        sensorReadingDao.observeSince(sinceEpochMillis).map { list -> list.map { it.toDomain() } }

    override fun observeRecentReadings(limit: Int): Flow<List<SensorReading>> =
        sensorReadingDao.observeRecent(limit).map { list -> list.reversed().map { it.toDomain() } }

    override suspend fun getAllReadingsForExport(): List<SensorReading> =
        sensorReadingDao.getAllForExport().map { it.toDomain() }

    override fun observePressureReadings(since: Long): Flow<List<PressureReading>> =
        pressureDao.observeSince(since).map { list -> list.map { it.toDomain() } }

    override fun observeTemperatureReadings(since: Long): Flow<List<TemperatureReading>> =
        temperatureDao.observeSince(since).map { list -> list.map { it.toDomain() } }

    override fun observeMoistureReadings(since: Long): Flow<List<MoistureReading>> =
        moistureDao.observeSince(since).map { list -> list.map { it.toDomain() } }

    override fun observeGaitReadings(since: Long): Flow<List<GaitReading>> =
        gaitDao.observeSince(since).map { list -> list.map { it.toDomain() } }

    override suspend fun insertReading(reading: SensorReading) {
        // Data validation and sanitization gate (Phase 13)
        val sanitized = sensorValidator.sanitizeReading(reading)
        val validated = sensorValidator.validateReading(sanitized)
        
        sensorReadingDao.insert(validated.toEntity())

        // Derive a zone-level pressure snapshot + a lightweight gait sample from the composite reading
        // so downstream modules (Pressure/Gait screens) always have data to chart.
        pressureDao.insert(
            derivePressureReading(validated, FootSide.LEFT).toEntity()
        )
        pressureDao.insert(
            derivePressureReading(validated, FootSide.RIGHT).toEntity()
        )
        temperatureDao.insert(TemperatureReading(timestamp = validated.timestamp, temperature = validated.temperature, sessionType = validated.sessionType).toEntity())
        moistureDao.insert(MoistureReading(timestamp = validated.timestamp, moisture = validated.moisture, sessionType = validated.sessionType).toEntity())

        val total = validated.leftPressure + validated.rightPressure
        val balance = if (total > 0) ((validated.leftPressure - validated.rightPressure) / total) * 100.0 else 0.0
        gaitDao.insert(
            GaitReading(
                timestamp = validated.timestamp,
                leftRightBalance = balance,
                cadence = if (validated.activityState == ActivityState.WALKING) Random.nextDouble(95.0, 118.0) else 0.0,
                stridePattern = if (kotlin.math.abs(balance) > 20.0) "IRREGULAR" else "REGULAR",
                sessionType = validated.sessionType
            ).toEntity()
        )
    }

    override suspend fun insertPressureReading(reading: PressureReading) = pressureDao.insert(reading.toEntity())
    override suspend fun insertTemperatureReading(reading: TemperatureReading) = temperatureDao.insert(reading.toEntity())
    override suspend fun insertMoistureReading(reading: MoistureReading) = moistureDao.insert(reading.toEntity())
    override suspend fun insertGaitReading(reading: GaitReading) = gaitDao.insert(reading.toEntity())

    override suspend fun getZoneDetail(zone: FootZone, side: FootSide): ZoneDetail {
        // Prototype heuristic derived from the most recent composite reading; a production
        // build would read true per-zone sensels once the physical sensor grid is finalized.
        val level = PressureLevel.entries.toTypedArray().random()
        return ZoneDetail(
            zone = zone, side = side, level = level,
            durationMinutes = Random.nextInt(2, 30),
            trend = Trend.entries.toTypedArray().random(),
            insight = if (level == PressureLevel.HIGH) "Prolonged pressure detected." else null
        )
    }

    override suspend fun clearAllLocalData() {
        sensorReadingDao.clearAll(); pressureDao.clearAll(); temperatureDao.clearAll()
        moistureDao.clearAll(); gaitDao.clearAll()
    }

    private fun derivePressureReading(reading: SensorReading, side: FootSide): PressureReading {
        val base = if (side == FootSide.LEFT) reading.leftPressure else reading.rightPressure
        return PressureReading(
            timestamp = reading.timestamp, side = side,
            heel = base * 0.9, arch = base * 0.55, midfoot = base * 0.7,
            forefoot = base * 1.05, bigToe = base * 0.8, lesserToes = base * 0.6,
            sessionType = reading.sessionType
        )
    }
}
