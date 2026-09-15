package com.thermoheal.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String, // never store plaintext passwords (section 58)
    val ageYears: Int?,
    val heightCm: Double?,
    val weightKg: Double?,
    val footSizeEu: Double?,
    val dominantFoot: String,
    val activityLevel: String,
    val occupation: String?,
    val isDemoUser: Boolean,
    val createdAt: Long
)

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val connectionState: String,
    val batteryPercent: Int?,
    val signalStrength: String,
    val firmwareVersion: String,
    val hasPressureSensor: Boolean,
    val hasTemperatureSensor: Boolean,
    val hasMoistureSensor: Boolean,
    val hasImuSensor: Boolean,
    val lastSyncTimestamp: Long?,
    val packetsReceived: Long
)

@Entity(tableName = "sensor_readings")
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val leftPressure: Double,
    val rightPressure: Double,
    val temperature: Double,
    val moisture: Double,
    val steps: Int,
    val activityState: String,
    val batteryLevel: Int,
    val sessionType: String,
    val isValid: Boolean,
    val qualityWarning: String?
)

@Entity(tableName = "pressure_readings")
data class PressureReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val side: String,
    val heel: Double,
    val arch: Double,
    val midfoot: Double,
    val forefoot: Double,
    val bigToe: Double,
    val lesserToes: Double,
    val sessionType: String
)

@Entity(tableName = "temperature_readings")
data class TemperatureReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val temperature: Double,
    val sessionType: String
)

@Entity(tableName = "moisture_readings")
data class MoistureReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val moisture: Double,
    val sessionType: String
)

@Entity(tableName = "gait_readings")
data class GaitReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val leftRightBalance: Double,
    val cadence: Double,
    val stridePattern: String,
    val sessionType: String
)

@Entity(tableName = "ai_insights")
data class AIInsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val category: String,
    val title: String,
    val severity: String,
    val confidencePercent: Int,
    val observation: String,
    val recommendation: String,
    val contributingFactorsJson: String, // serialized List<ContributingFactor>
    val sessionType: String,
    val disclaimer: String
)

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val date: String,
    val wellnessIndex: Int,
    val avgTemperature: Double,
    val avgMoisture: Double,
    val pressureBalanceLeftPercent: Double,
    val steps: Int,
    val activeMinutes: Int,
    val insightCount: Int,
    val summarySentence: String,
    val sessionType: String
)

@Entity(tableName = "weekly_summaries")
data class WeeklySummaryEntity(
    @PrimaryKey val weekStartDate: String,
    val avgWellnessIndex: Int,
    val bestDay: String?,
    val mostActiveDay: String?,
    val mostStableDay: String?,
    val notableTrend: String?,
    val sessionType: String
)
