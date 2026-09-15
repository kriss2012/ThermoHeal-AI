package com.thermoheal.ai.data.bluetooth

import com.thermoheal.ai.domain.model.DeviceInfo
import com.thermoheal.ai.domain.model.SensorReading
import com.thermoheal.ai.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Composite BLE Repository (Phase 98: Demo vs Real Separation).
 * Transparently and safely routes between [DemoDataSimulator] and [RealBleRepository]
 * according to the user's active device mode in Preferences.
 * Guarantees that simulated data NEVER contaminates real hardware sessions.
 */
@Singleton
class CompositeBleRepository @Inject constructor(
    private val realBleRepository: RealBleRepository,
    private val demoDataSimulator: DemoDataSimulator,
    private val prefs: PreferencesManager
) : BleRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val isDemoMode: StateFlow<Boolean> = prefs.demoModeEnabled
        .stateIn(scope, SharingStarted.Eagerly, false)

    override fun observeConnectionState(): Flow<DeviceInfo> = isDemoMode.flatMapLatest { demo ->
        if (demo) demoDataSimulator.observeConnectionState()
        else realBleRepository.observeConnectionState()
    }

    override fun observeIncomingReadings(): Flow<SensorReading> = isDemoMode.flatMapLatest { demo ->
        if (demo) demoDataSimulator.observeIncomingReadings()
        else realBleRepository.observeIncomingReadings()
    }

    override suspend fun scan(): List<DeviceInfo> {
        return if (isDemoMode.value) {
            demoDataSimulator.scan()
        } else {
            realBleRepository.scan()
        }
    }

    override suspend fun connect(deviceId: String): Result<Unit> {
        return if (isDemoMode.value || deviceId.startsWith("DEMO", ignoreCase = true)) {
            demoDataSimulator.connect(deviceId)
        } else {
            realBleRepository.connect(deviceId)
        }
    }

    override suspend fun disconnect() {
        if (isDemoMode.value) {
            demoDataSimulator.disconnect()
        } else {
            realBleRepository.disconnect()
        }
    }

    override suspend fun testSensors(): Map<String, Boolean> {
        return if (isDemoMode.value) {
            demoDataSimulator.testSensors()
        } else {
            realBleRepository.testSensors()
        }
    }

    override suspend fun calibrate(): Result<Unit> {
        return if (isDemoMode.value) {
            demoDataSimulator.calibrate()
        } else {
            realBleRepository.calibrate()
        }
    }
}
