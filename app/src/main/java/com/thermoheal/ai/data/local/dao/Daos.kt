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
interface SensorReadingDao {
    @Query("SELECT * FROM sensor_readings ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<SensorReadingEntity?>

    @Query("SELECT * FROM sensor_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<SensorReadingEntity>>

    @Insert
    suspend fun insert(reading: SensorReadingEntity)

    @Query("DELETE FROM sensor_readings")
    suspend fun clearAll()
}

@Dao
interface PressureReadingDao {
    @Query("SELECT * FROM pressure_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<PressureReadingEntity>>

    @Insert
    suspend fun insert(reading: PressureReadingEntity)

    @Query("DELETE FROM pressure_readings")
    suspend fun clearAll()
}

@Dao
interface TemperatureReadingDao {
    @Query("SELECT * FROM temperature_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<TemperatureReadingEntity>>

    @Insert
    suspend fun insert(reading: TemperatureReadingEntity)

    @Query("DELETE FROM temperature_readings")
    suspend fun clearAll()
}

@Dao
interface MoistureReadingDao {
    @Query("SELECT * FROM moisture_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<MoistureReadingEntity>>

    @Insert
    suspend fun insert(reading: MoistureReadingEntity)

    @Query("DELETE FROM moisture_readings")
    suspend fun clearAll()
}

@Dao
interface GaitReadingDao {
    @Query("SELECT * FROM gait_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<GaitReadingEntity>>

    @Insert
    suspend fun insert(reading: GaitReadingEntity)

    @Query("DELETE FROM gait_readings")
    suspend fun clearAll()
}

@Dao
interface AIInsightDao {
    @Query("SELECT * FROM ai_insights ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AIInsightEntity>>

    @Insert
    suspend fun insert(insight: AIInsightEntity)

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
}
