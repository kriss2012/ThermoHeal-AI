package com.thermoheal.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.thermoheal.ai.data.bluetooth.SensorIngestionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ThermoHealApplication : Application() {

    @Inject lateinit var sensorIngestionManager: SensorIngestionManager

    override fun onCreate() {
        super.onCreate()
        sensorIngestionManager.start()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channels = listOf(
                NotificationChannel("thermoheal_device", "Device", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel("thermoheal_sensor", "Sensor Alerts", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel("thermoheal_ai", "AI Insights", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel("thermoheal_system", "System", NotificationManager.IMPORTANCE_LOW),
            )
            channels.forEach { manager?.createNotificationChannel(it) }
        }
    }
}
