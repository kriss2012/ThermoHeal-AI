package com.thermoheal.ai.domain.model

enum class DominantFoot { LEFT, RIGHT, AMBIDEXTROUS }
enum class ActivityLevel { SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE }
enum class UnitSystem { METRIC, IMPERIAL }
enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val ageYears: Int?,
    val heightCm: Double?,
    val weightKg: Double?,
    val footSizeEu: Double?,
    val dominantFoot: DominantFoot,
    val activityLevel: ActivityLevel,
    val occupation: String? = null,
    val isDemoUser: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long = createdAt
)

/** Full BLE State Machine per Phase 12 */
enum class BleConnectionState(val displayName: String) {
    DISCONNECTED("Disconnected"),
    SCANNING("Scanning..."),
    CONNECTING("Connecting..."),
    CONNECTED("Connected"),
    DISCOVERING_SERVICES("Discovering Services..."),
    READY("Ready"),
    RECEIVING_DATA("Receiving Telemetry"),
    RECONNECTING("Reconnecting..."),
    ERROR("Connection Error")
}

enum class ConnectionState { CONNECTED, DISCONNECTED, CONNECTING, SCANNING, ERROR }
enum class SignalStrength { EXCELLENT, GOOD, FAIR, POOR, UNKNOWN }

data class DeviceInfo(
    val id: String,
    val name: String = "ThermoHeal-AI Insole",
    val connectionState: ConnectionState,
    val batteryPercent: Int?,
    val signalStrength: SignalStrength,
    val firmwareVersion: String = "Prototype v1.0",
    val hasPressureSensor: Boolean = true,
    val hasTemperatureSensor: Boolean = true,
    val hasMoistureSensor: Boolean = true,
    val hasImuSensor: Boolean = false, // "Hardware integration pending" until confirmed present
    val lastSyncTimestamp: Long?,
    val packetsReceived: Long = 0
)

data class DailySummary(
    val date: String, // yyyy-MM-dd
    val wellnessIndex: Int,
    val avgTemperature: Double,
    val avgMoisture: Double,
    val pressureBalanceLeftPercent: Double,
    val steps: Int,
    val activeMinutes: Int,
    val insightCount: Int,
    val summarySentence: String,
    val sessionType: SessionType
)

data class WeeklySummary(
    val weekStartDate: String,
    val avgWellnessIndex: Int,
    val bestDay: String?,
    val mostActiveDay: String?,
    val mostStableDay: String?,
    val notableTrend: String?,
    val sessionType: SessionType
)
