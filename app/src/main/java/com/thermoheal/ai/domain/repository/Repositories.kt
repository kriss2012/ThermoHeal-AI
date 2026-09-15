package com.thermoheal.ai.domain.repository

import com.thermoheal.ai.domain.model.*
import kotlinx.coroutines.flow.Flow

/** Abstracts local persistence + (future) cloud sync of raw sensor data. */
interface SensorRepository {
    fun observeLatestReading(): Flow<SensorReading?>
    fun observeReadingsSince(sinceEpochMillis: Long): Flow<List<SensorReading>>
    fun observePressureReadings(since: Long): Flow<List<PressureReading>>
    fun observeTemperatureReadings(since: Long): Flow<List<TemperatureReading>>
    fun observeMoistureReadings(since: Long): Flow<List<MoistureReading>>
    fun observeGaitReadings(since: Long): Flow<List<GaitReading>>

    suspend fun insertReading(reading: SensorReading)
    suspend fun insertPressureReading(reading: PressureReading)
    suspend fun insertTemperatureReading(reading: TemperatureReading)
    suspend fun insertMoistureReading(reading: MoistureReading)
    suspend fun insertGaitReading(reading: GaitReading)

    suspend fun getZoneDetail(zone: FootZone, side: FootSide): ZoneDetail
    suspend fun clearAllLocalData()
}

/** Abstracts the AI analysis pipeline (currently rule-based, swappable for a trained model). */
interface AIRepository {
    fun observeInsights(): Flow<List<AIInsight>>
    suspend fun generateWellnessInsight(features: SensorFeatureVector, sessionType: SessionType): AIInsight
    suspend fun calculateWellnessIndex(features: SensorFeatureVector): WellnessIndex
    suspend fun getPersonalBaseline(): PersonalBaseline
    suspend fun clearInsights()
}

interface UserRepository {
    fun observeUser(): Flow<UserProfile?>
    suspend fun signUp(profile: UserProfile, password: String): Result<UserProfile>
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun continueAsDemoUser(): Result<UserProfile>
    suspend fun logout()
    suspend fun updateProfile(profile: UserProfile)
    suspend fun isLoggedIn(): Boolean
}

interface DeviceRepository {
    fun observeDeviceState(): Flow<DeviceInfo>
    suspend fun scanForDevices(): List<DeviceInfo>
    suspend fun connect(deviceId: String): Result<Unit>
    suspend fun disconnect()
    suspend fun testSensors(): Map<String, Boolean>
    suspend fun calibrate(): Result<Unit>
}

interface SummaryRepository {
    fun observeDailySummary(date: String): Flow<DailySummary?>
    fun observeWeeklySummaries(): Flow<List<WeeklySummary>>
    suspend fun recomputeDailySummary(date: String, sessionType: SessionType): DailySummary
}
