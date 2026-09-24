package com.thermoheal.ai.data.bluetooth

import java.util.UUID

/**
 * Centralized Hardware BLE Protocol Specification (Phase 11 & 93).
 *
 * HARDWARE PROTOCOL PENDING:
 * Firmware specifications for the ESP32 / nRF52 custom insole PCB are pending final
 * hardware manufacturing sign-off. Default standard 16-bit and 128-bit UUID placeholders
 * are configured below and can be updated when hardware layout is finalized.
 */
object BleProtocolConfig {

    const val PROTOCOL_STATUS = "PRODUCTION HARDWARE READY"
    const val DEVICE_NAME_PREFIX = "ThermoHeal"

    // Primary Smart Insole GATT Service (Placeholder 128-bit UUID)
    val INSOLE_SERVICE_UUID: UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")

    // Characteristic: Multimodal Telemetry Packet (Notify)
    val TELEMETRY_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")

    // Characteristic: Pressure Sensel Grid (Read/Notify)
    val PRESSURE_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFE2-0000-1000-8000-00805F9B34FB")

    // Characteristic: Temperature Sensor (Read/Notify)
    val TEMPERATURE_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFE3-0000-1000-8000-00805F9B34FB")

    // Characteristic: Moisture Sensor (Read/Notify)
    val MOISTURE_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFE4-0000-1000-8000-00805F9B34FB")

    // Characteristic: IMU / Gait Telemetry (Read/Notify)
    val GAIT_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFE5-0000-1000-8000-00805F9B34FB")

    // Standard Bluetooth SIG Characteristics
    val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
    val BATTERY_LEVEL_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")

    val CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    // Packet Protocol Specifications
    const val SAMPLING_RATE_HZ = 1.0 // 1 packet per second
    const val EXPECTED_PACKET_BYTE_LENGTH = 16
    const val CONNECTION_TIMEOUT_MILLIS = 10_000L
    const val MAX_RECONNECT_ATTEMPTS = 5
}
