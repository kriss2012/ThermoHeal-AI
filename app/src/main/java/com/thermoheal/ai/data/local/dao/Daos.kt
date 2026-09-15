package com.thermoheal.ai.data.local.dao

import androidx.room.*
import com.thermoheal.ai.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUser(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAll()
}

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices LIMIT 1")
    fun observeDevice(): Flow<DeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceEntity)
}

@Dao
interface MonitoringSessionDao {
    @Query("SELECT * FROM monitoring_sessions ORDER BY startTime DESC LIMIT 1")
    fun observeLatest(): Flow<MonitoringSessionEntity?>

    @Query("SELECT * FROM monitoring_sessions ORDER BY startTime DESC")
    fun observeSessions(): Flow<List<MonitoringSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: MonitoringSessionEntity)

    @Query("DELETE FROM monitoring_sessions WHERE userId = :userId")
    suspend fun clearUserSessions(userId: String)
}

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPending(limit: Int = 50): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET status = :status, retryCount = retryCount + 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM sync_queue WHERE status = 'UPLOADED'")
    suspend fun clearUploaded()
}

@Dao
interface SensorReadingDao {
    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<SensorReadingEntity?>

    @Query("SELECT * FROM sensor_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 120): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings ORDER BY timestamp ASC")
    suspend fun getAllForExport(): List<SensorReadingEntity>

    @Insert
    suspend fun insert(reading: SensorReadingEntity)

    @Insert
    suspend fun insertAll(readings: List<SensorReadingEntity>)

    @Query("DELETE FROM sensor_readings WHERE userId = :userId")
    suspend fun clearUserData(userId: String)

    @Query("DELETE FROM sensor_readings")
    suspend fun clearAll()
}

@Dao
interface PressureReadingDao {
    @Query("SELECT * FROM pressure_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<PressureReadingEntity>>

    @Insert
    suspend fun insert(reading: PressureReadingEntity)

    @Insert
    suspend fun insertAll(readings: List<PressureReadingEntity>)

    @Query("DELETE FROM pressure_readings WHERE userId = :userId")
    suspend fun clearUserData(userId: String)

    @Query("DELETE FROM pressure_readings")
    suspend fun clearAll()
}

@Dao
interface TemperatureReadingDao {
    @Query("SELECT * FROM temperature_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<TemperatureReadingEntity>>

    @Insert
    suspend fun insert(reading: TemperatureReadingEntity)

    @Insert
    suspend fun insertAll(readings: List<TemperatureReadingEntity>)

    @Query("DELETE FROM temperature_readings WHERE userId = :userId")
    suspend fun clearUserData(userId: String)

    @Query("DELETE FROM temperature_readings")
    suspend fun clearAll()
}

@Dao
interface MoistureReadingDao {
    @Query("SELECT * FROM moisture_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<MoistureReadingEntity>>

    @Insert
    suspend fun insert(reading: MoistureReadingEntity)

    @Insert
    suspend fun insertAll(readings: List<MoistureReadingEntity>)

    @Query("DELETE FROM moisture_readings WHERE userId = :userId")
    suspend fun clearUserData(userId: String)

    @Query("DELETE FROM moisture_readings")
    suspend fun clearAll()
}

@Dao
interface GaitReadingDao {
    @Query("SELECT * FROM gait_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<GaitReadingEntity>>

    @Insert
    suspend fun insert(reading: GaitReadingEntity)

    @Insert
    suspend fun insertAll(readings: List<GaitReadingEntity>)

    @Query("DELETE FROM gait_readings WHERE userId = :userId")
    suspend fun clearUserData(userId: String)

    @Query("DELETE FROM gait_readings")
    suspend fun clearAll()
}

@Dao
interface AIInsightDao {
    @Query("SELECT * FROM ai_insights ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AIInsightEntity>>

    @Insert
    suspend fun insert(insight: AIInsightEntity)

    @Query("DELETE FROM ai_insights WHERE userId = :userId")
    suspend fun clearUserData(userId: String)

    @Query("DELETE FROM ai_insights")
    suspend fun clearAll()
}

@Dao
interface SummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    fun observeDaily(date: String): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM weekly_summaries ORDER BY weekStartDate DESC")
    fun observeWeekly(): Flow<List<WeeklySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDaily(summary: DailySummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeekly(summary: WeeklySummaryEntity)

    @Query("DELETE FROM daily_summaries")
    suspend fun clearDaily()

    @Query("DELETE FROM weekly_summaries")
    suspend fun clearWeekly()
}
