package com.thermoheal.ai.data.bluetooth

import com.thermoheal.ai.domain.model.DeviceInfo
import com.thermoheal.ai.domain.model.SensorReading
import kotlinx.coroutines.flow.Flow

/**
 * Clean abstraction between the UI/domain layer and the physical transport.
 * The UI never touches android.bluetooth.* directly — only this interface.
 *
 * Two implementations exist:
 *  - MockBleRepository: fully-featured simulator used by Research Demo Mode
 *    and whenever no physical prototype is connected.
 *  - RealBleRepository (stub below): the integration point for the actual
 *    ESP32 / nRF52-class insole firmware. Hardware protocol (GATT service /
 *    characteristic UUIDs, packet framing) is intentionally left
 *    configurable since the exact board has not been finalized — see
 *    section 95 "Future Hardware Ready" in the product spec.
 */
interface BleRepository {
    fun observeConnectionState(): Flow<DeviceInfo>
    fun observeIncomingReadings(): Flow<SensorReading>
    suspend fun scan(): List<DeviceInfo>
    suspend fun connect(deviceId: String): Result<Unit>
    suspend fun disconnect()
    suspend fun testSensors(): Map<String, Boolean>
    suspend fun calibrate(): Result<Unit>
}

/**
 * Real-hardware implementation stub. Intentionally unimplemented in this
 * prototype build — wire this up once the insole's GATT profile is fixed.
 *
 * Expected shape once implemented:
 *   BluetoothLeScanner -> ScanCallback -> BluetoothGatt -> BluetoothGattCallback
 *   -> characteristic notifications -> packet parser -> SensorReading
 */
class RealBleRepository : BleRepository {
    override fun observeConnectionState(): Flow<DeviceInfo> =
        throw NotImplementedError("Hardware integration pending / prototype interface. See BleRepository docs.")

    override fun observeIncomingReadings(): Flow<SensorReading> =
        throw NotImplementedError("Hardware integration pending / prototype interface.")

    override suspend fun scan(): List<DeviceInfo> =
        throw NotImplementedError("Hardware integration pending / prototype interface.")

    override suspend fun connect(deviceId: String): Result<Unit> =
        Result.failure(NotImplementedError("Hardware integration pending / prototype interface."))

    override suspend fun disconnect() { /* no-op until hardware is wired up */ }

    override suspend fun testSensors(): Map<String, Boolean> =
        throw NotImplementedError("Hardware integration pending / prototype interface.")

    override suspend fun calibrate(): Result<Unit> =
        Result.failure(NotImplementedError("Hardware integration pending / prototype interface."))
}
