package com.thermoheal.ai.data.repository

import com.thermoheal.ai.data.local.entity.*
import com.thermoheal.ai.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

fun SensorReading.toEntity() = SensorReadingEntity(
    timestamp = timestamp, leftPressure = leftPressure, rightPressure = rightPressure,
    temperature = temperature, moisture = moisture, steps = steps,
    activityState = activityState.name, batteryLevel = batteryLevel,
    sessionType = sessionType.name, isValid = isValid, qualityWarning = qualityWarning
)

fun SensorReadingEntity.toDomain() = SensorReading(
    id = id, timestamp = timestamp, leftPressure = leftPressure, rightPressure = rightPressure,
    temperature = temperature, moisture = moisture, steps = steps,
    activityState = runCatching { ActivityState.valueOf(activityState) }.getOrDefault(ActivityState.UNKNOWN),
    batteryLevel = batteryLevel,
    sessionType = runCatching { SessionType.valueOf(sessionType) }.getOrDefault(SessionType.DEMO),
    isValid = isValid, qualityWarning = qualityWarning
)

fun PressureReading.toEntity() = PressureReadingEntity(
    timestamp = timestamp, side = side.name, heel = heel, arch = arch, midfoot = midfoot,
    forefoot = forefoot, bigToe = bigToe, lesserToes = lesserToes, sessionType = sessionType.name
)

fun PressureReadingEntity.toDomain() = PressureReading(
    id = id, timestamp = timestamp, side = FootSide.valueOf(side), heel = heel, arch = arch,
    midfoot = midfoot, forefoot = forefoot, bigToe = bigToe, lesserToes = lesserToes,
    sessionType = SessionType.valueOf(sessionType)
)

fun TemperatureReading.toEntity() = TemperatureReadingEntity(timestamp = timestamp, temperature = temperature, sessionType = sessionType.name)
fun TemperatureReadingEntity.toDomain() = TemperatureReading(id, timestamp, temperature, SessionType.valueOf(sessionType))

fun MoistureReading.toEntity() = MoistureReadingEntity(timestamp = timestamp, moisture = moisture, sessionType = sessionType.name)
fun MoistureReadingEntity.toDomain() = MoistureReading(id, timestamp, moisture, SessionType.valueOf(sessionType))

fun GaitReading.toEntity() = GaitReadingEntity(
    timestamp = timestamp, leftRightBalance = leftRightBalance, cadence = cadence,
    stridePattern = stridePattern, sessionType = sessionType.name
)
fun GaitReadingEntity.toDomain() = GaitReading(id, timestamp, leftRightBalance, cadence, stridePattern, SessionType.valueOf(sessionType))

fun List<ContributingFactor>.toJson(): String {
    val arr = JSONArray()
    forEach {
        arr.put(JSONObject().apply { put("label", it.label); put("weight", it.weightPercent) })
    }
    return arr.toString()
}

fun String.toFactors(): List<ContributingFactor> = runCatching {
    val arr = JSONArray(this)
    (0 until arr.length()).map {
        val obj = arr.getJSONObject(it)
        ContributingFactor(obj.getString("label"), obj.getInt("weight"))
    }
}.getOrDefault(emptyList())

fun AIInsight.toEntity() = AIInsightEntity(
    timestamp = timestamp, category = category.name, title = title, severity = severity.name,
    confidencePercent = confidencePercent, observation = observation, recommendation = recommendation,
    contributingFactorsJson = contributingFactors.toJson(), sessionType = sessionType.name, disclaimer = disclaimer
)

fun AIInsightEntity.toDomain() = AIInsight(
    id = id, timestamp = timestamp, category = InsightCategory.valueOf(category), title = title,
    severity = InsightSeverity.valueOf(severity), confidencePercent = confidencePercent,
    observation = observation, recommendation = recommendation,
    contributingFactors = contributingFactorsJson.toFactors(),
    sessionType = SessionType.valueOf(sessionType), disclaimer = disclaimer
)

fun UserEntity.toDomain() = UserProfile(
    id = id, name = name, email = email, ageYears = ageYears, heightCm = heightCm, weightKg = weightKg,
    footSizeEu = footSizeEu, dominantFoot = DominantFoot.valueOf(dominantFoot),
    activityLevel = ActivityLevel.valueOf(activityLevel), occupation = occupation,
    isDemoUser = isDemoUser, createdAt = createdAt
)

fun DeviceEntity.toDomain() = DeviceInfo(
    id = id, name = name, connectionState = ConnectionState.valueOf(connectionState),
    batteryPercent = batteryPercent, signalStrength = SignalStrength.valueOf(signalStrength),
    firmwareVersion = firmwareVersion, hasPressureSensor = hasPressureSensor,
    hasTemperatureSensor = hasTemperatureSensor, hasMoistureSensor = hasMoistureSensor,
    hasImuSensor = hasImuSensor, lastSyncTimestamp = lastSyncTimestamp, packetsReceived = packetsReceived
)

fun DeviceInfo.toEntity() = DeviceEntity(
    id = id, name = name, connectionState = connectionState.name, batteryPercent = batteryPercent,
    signalStrength = signalStrength.name, firmwareVersion = firmwareVersion,
    hasPressureSensor = hasPressureSensor, hasTemperatureSensor = hasTemperatureSensor,
    hasMoistureSensor = hasMoistureSensor, hasImuSensor = hasImuSensor,
    lastSyncTimestamp = lastSyncTimestamp, packetsReceived = packetsReceived
)

fun DailySummaryEntity.toDomain() = DailySummary(
    date = date, wellnessIndex = wellnessIndex, avgTemperature = avgTemperature, avgMoisture = avgMoisture,
    pressureBalanceLeftPercent = pressureBalanceLeftPercent, steps = steps, activeMinutes = activeMinutes,
    insightCount = insightCount, summarySentence = summarySentence, sessionType = SessionType.valueOf(sessionType)
)
