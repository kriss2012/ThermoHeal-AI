package com.thermoheal.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thermoheal.ai.data.local.dao.*
import com.thermoheal.ai.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class,
        MonitoringSessionEntity::class,
        SyncQueueEntity::class,
        SensorReadingEntity::class,
        PressureReadingEntity::class,
        TemperatureReadingEntity::class,
        MoistureReadingEntity::class,
        GaitReadingEntity::class,
        AIInsightEntity::class,
        DailySummaryEntity::class,
        WeeklySummaryEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class ThermoHealDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deviceDao(): DeviceDao
    abstract fun monitoringSessionDao(): MonitoringSessionDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun pressureReadingDao(): PressureReadingDao
    abstract fun temperatureReadingDao(): TemperatureReadingDao
    abstract fun moistureReadingDao(): MoistureReadingDao
    abstract fun gaitReadingDao(): GaitReadingDao
    abstract fun aiInsightDao(): AIInsightDao
    abstract fun summaryDao(): SummaryDao

    companion object {
        const val DATABASE_NAME = "thermoheal_database"

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Create monitoring_sessions table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `monitoring_sessions` (
                        `sessionId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER,
                        `source` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`sessionId`)
                    )
                    """.trimIndent()
                )

                // 2. Create sync_queue table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sync_queue` (
                        `id` TEXT NOT NULL,
                        `entityType` TEXT NOT NULL,
                        `entityId` TEXT NOT NULL,
                        `payloadJson` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `retryCount` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                // 3. Add provenance columns to sensor_readings
                db.execSQL("ALTER TABLE `sensor_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")
                db.execSQL("ALTER TABLE `sensor_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT 'default_session'")
                db.execSQL("ALTER TABLE `sensor_readings` ADD COLUMN `quality` TEXT NOT NULL DEFAULT 'GOOD'")
                db.execSQL("ALTER TABLE `sensor_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

                // 4. Add provenance columns to pressure_readings
                db.execSQL("ALTER TABLE `pressure_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")
                db.execSQL("ALTER TABLE `pressure_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT 'default_session'")
                db.execSQL("ALTER TABLE `pressure_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

                // 5. Add provenance columns to temperature_readings
                db.execSQL("ALTER TABLE `temperature_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")
                db.execSQL("ALTER TABLE `temperature_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT 'default_session'")
                db.execSQL("ALTER TABLE `temperature_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

                // 6. Add provenance columns to moisture_readings
                db.execSQL("ALTER TABLE `moisture_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")
                db.execSQL("ALTER TABLE `moisture_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT 'default_session'")
                db.execSQL("ALTER TABLE `moisture_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

                // 7. Add provenance columns to gait_readings
                db.execSQL("ALTER TABLE `gait_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")
                db.execSQL("ALTER TABLE `gait_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT 'default_session'")
                db.execSQL("ALTER TABLE `gait_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

                // 8. Add provenance & AI model versioning columns to ai_insights
                db.execSQL("ALTER TABLE `ai_insights` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'local_user'")
                db.execSQL("ALTER TABLE `ai_insights` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT 'default_session'")
                db.execSQL("ALTER TABLE `ai_insights` ADD COLUMN `modelVersion` TEXT NOT NULL DEFAULT 'rule-engine-1.0'")
                db.execSQL("ALTER TABLE `ai_insights` ADD COLUMN `algorithmVersion` TEXT NOT NULL DEFAULT 'FWI-1.0-prototype'")
                db.execSQL("ALTER TABLE `ai_insights` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
