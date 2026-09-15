package com.thermoheal.ai.domain.usecase

import com.thermoheal.ai.domain.model.SensorReading
import com.thermoheal.ai.domain.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsulates privacy-compliant data export formatting (Phase 33 & 34).
 * Formats sensor readings and user profiles into standardized CSV and JSON formats.
 */
@Singleton
class ExportDataUseCase @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    fun exportToCsv(user: UserProfile?, readings: List<SensorReading>): String {
        val sb = StringBuilder()
        sb.append("# ThermoHeal-AI Data Export\n")
        sb.append("# Exported: ${dateFormat.format(Date())}\n")
        sb.append("# User: ${user?.name ?: "Anonymous"} (${user?.email ?: "N/A"})\n")
        sb.append("# Disclaimer: ThermoHeal-AI is a research and wellness-monitoring prototype. Data is intended for monitoring and decision support only.\n")
        sb.append("\n")
        sb.append("Timestamp,IsoTime,UserId,SessionId,Source,LeftPressureKPa,RightPressureKPa,TemperatureC,MoisturePercent,Steps,ActivityState,BatteryLevel,Quality,IsValid\n")

        for (r in readings) {
            val isoTime = dateFormat.format(Date(r.timestamp))
            sb.append("${r.timestamp},\"$isoTime\",\"${r.userId}\",\"${r.sessionId}\",\"${r.sessionType.name}\",")
            sb.append("%.2f,%.2f,%.2f,%.2f,%d,\"%s\",%d,\"%s\",%b\n".format(
                Locale.US,
                r.leftPressure,
                r.rightPressure,
                r.temperature,
                r.moisture,
                r.steps,
                r.activityState.name,
                r.batteryLevel,
                r.quality.name,
                r.isValid
            ))
        }
        return sb.toString()
    }

    fun exportToJson(user: UserProfile?, readings: List<SensorReading>): String {
        val root = JSONObject()
        root.put("appName", "ThermoHeal-AI")
        root.put("version", "1.0.0")
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("exportIsoDate", dateFormat.format(Date()))
        root.put("disclaimer", "ThermoHeal-AI is a research and wellness-monitoring prototype. Measurements and insights are intended for decision support only and not to diagnose, treat, cure, or prevent disease.")

        val userObj = JSONObject()
        if (user != null) {
            userObj.put("id", user.id)
            userObj.put("name", user.name)
            userObj.put("email", user.email)
            userObj.put("ageYears", user.ageYears ?: JSONObject.NULL)
            userObj.put("footSizeEu", user.footSizeEu ?: JSONObject.NULL)
            userObj.put("dominantFoot", user.dominantFoot.name)
            userObj.put("activityLevel", user.activityLevel.name)
            userObj.put("isDemoUser", user.isDemoUser)
        }
        root.put("userProfile", userObj)

        val readingsArray = JSONArray()
        for (r in readings) {
            val item = JSONObject()
            item.put("timestamp", r.timestamp)
            item.put("isoTime", dateFormat.format(Date(r.timestamp)))
            item.put("userId", r.userId)
            item.put("sessionId", r.sessionId)
            item.put("source", r.sessionType.name)
            item.put("leftPressureKPa", r.leftPressure)
            item.put("rightPressureKPa", r.rightPressure)
            item.put("temperatureC", r.temperature)
            item.put("moisturePercent", r.moisture)
            item.put("steps", r.steps)
            item.put("activityState", r.activityState.name)
            item.put("batteryLevel", r.batteryLevel)
            item.put("quality", r.quality.name)
            item.put("isValid", r.isValid)
            readingsArray.put(item)
        }
        root.put("sensorReadings", readingsArray)

        return root.toString(2)
    }
}
