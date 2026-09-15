package com.thermoheal.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thermoheal.ai.data.local.dao.*
import com.thermoheal.ai.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class,
        SensorReadingEntity::class,
        PressureReadingEntity::class,
        TemperatureReadingEntity::class,
        MoistureReadingEntity::class,
        GaitReadingEntity::class,
        AIInsightEntity::class,
        DailySummaryEntity::class,
        WeeklySummaryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ThermoHealDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun pressureReadingDao(): PressureReadingDao
    abstract fun temperatureReadingDao(): TemperatureReadingDao
    abstract fun moistureReadingDao(): MoistureReadingDao
    abstract fun gaitReadingDao(): GaitReadingDao
    abstract fun aiInsightDao(): AIInsightDao
    abstract fun summaryDao(): SummaryDao

    companion object {
        const val DATABASE_NAME = "thermoheal_database"
    }
}
