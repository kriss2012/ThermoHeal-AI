# ThermoHeal-AI Database Migration Guide

---

## 1. Schema Version History

| Version | Description | Key Changes |
|---|---|---|
| **1** | Initial MVP Schema | Basic tables for users, devices, sensor readings, pressure, temperature, moisture, gait, summaries, insights. |
| **2** | Production Health Data Architecture | Added `MonitoringSessionEntity` and `SyncQueueEntity`. Added provenance columns (`userId`, `sessionId`, `createdAt`, `quality`) to all reading tables. Added AI versioning columns (`modelVersion`, `algorithmVersion`, `featureVersion`) to insights. |

---

## 2. Non-Destructive Migration Strategy (`MIGRATION_1_2`)

Defined in `com.thermoheal.ai.data.local.ThermoHealDatabase`:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create new table: monitoring_sessions
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `monitoring_sessions` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `startTime` INTEGER NOT NULL,
                `endTime` INTEGER,
                `activityType` TEXT NOT NULL,
                `stepCount` INTEGER NOT NULL,
                `avgWellnessScore` REAL NOT NULL,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        // 2. Create new table: sync_queue
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `sync_queue` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `entityType` TEXT NOT NULL,
                `entityId` TEXT NOT NULL,
                `payloadJson` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `attempts` INTEGER NOT NULL,
                `lastAttempt` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
        """.trimIndent())

        // 3. Add provenance columns to sensor_readings
        db.execSQL("ALTER TABLE `sensor_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `sensor_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `sensor_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

        // 4. Add provenance & quality to pressure_readings
        db.execSQL("ALTER TABLE `pressure_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `pressure_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `pressure_readings` ADD COLUMN `quality` TEXT NOT NULL DEFAULT 'VALID'")
        db.execSQL("ALTER TABLE `pressure_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

        // 5. Add provenance & quality to temperature_readings
        db.execSQL("ALTER TABLE `temperature_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `temperature_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `temperature_readings` ADD COLUMN `quality` TEXT NOT NULL DEFAULT 'VALID'")
        db.execSQL("ALTER TABLE `temperature_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

        // 6. Add provenance & quality to moisture_readings
        db.execSQL("ALTER TABLE `moisture_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `moisture_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `moisture_readings` ADD COLUMN `quality` TEXT NOT NULL DEFAULT 'VALID'")
        db.execSQL("ALTER TABLE `moisture_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

        // 7. Add provenance & quality to gait_readings
        db.execSQL("ALTER TABLE `gait_readings` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `gait_readings` ADD COLUMN `sessionId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `gait_readings` ADD COLUMN `quality` TEXT NOT NULL DEFAULT 'VALID'")
        db.execSQL("ALTER TABLE `gait_readings` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

        // 8. Add AI versioning columns to ai_insights
        db.execSQL("ALTER TABLE `ai_insights` ADD COLUMN `modelVersion` TEXT NOT NULL DEFAULT '1.0.0'")
        db.execSQL("ALTER TABLE `ai_insights` ADD COLUMN `algorithmVersion` TEXT NOT NULL DEFAULT '1.0.0'")
        db.execSQL("ALTER TABLE `ai_insights` ADD COLUMN `featureVersion` TEXT NOT NULL DEFAULT '1.0.0'")

        // 9. Add provenance to daily_summaries
        db.execSQL("ALTER TABLE `daily_summaries` ADD COLUMN `algorithmVersion` TEXT NOT NULL DEFAULT '1.0.0'")
        db.execSQL("ALTER TABLE `daily_summaries` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
    }
}
```

---

## 3. Schema Export Verification
Room schema export is enabled in `app/build.gradle.kts`:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```
Each build generates a JSON representation of the database schema in `app/schemas/com.thermoheal.ai.data.local.ThermoHealDatabase/` to enable automated migration test verification via `androidx.room.testing.MigrationTestHelper`.
