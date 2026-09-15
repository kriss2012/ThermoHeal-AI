package com.thermoheal.ai.data.repository

import com.thermoheal.ai.data.bluetooth.BleRepository
import com.thermoheal.ai.data.local.dao.DeviceDao
import com.thermoheal.ai.domain.model.DeviceInfo
import com.thermoheal.ai.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val bleRepository: BleRepository,
    private val deviceDao: DeviceDao
) : DeviceRepository {

    override fun observeDeviceState(): Flow<DeviceInfo> =
        bleRepository.observeConnectionState()

    override suspend fun scanForDevices(): List<DeviceInfo> = bleRepository.scan()

    override suspend fun connect(deviceId: String): Result<Unit> {
        val result = bleRepository.connect(deviceId)
        return result
    }

    override suspend fun disconnect() = bleRepository.disconnect()

    override suspend fun testSensors(): Map<String, Boolean> = bleRepository.testSensors()

    override suspend fun calibrate(): Result<Unit> = bleRepository.calibrate()
}
