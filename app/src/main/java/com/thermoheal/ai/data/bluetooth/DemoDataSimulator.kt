package com.thermoheal.ai.data.bluetooth

import com.thermoheal.ai.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin
import kotlin.random.Random

enum class DemoScenario {
    NORMAL, WALKING, STANDING, HIGH_PRESSURE, THERMAL_RISE, HIGH_MOISTURE, ABNORMAL_GAIT, MIXED
}

enum class SimulationSpeed(val intervalMillis: Long) { SLOW(3000), NORMAL(1000), FAST(300) }

/**
 * "Research Demo Mode" — the application must work even without the physical
 * insole (spec section 10). This generates realistic simulated sensor data
 * with GRADUAL transitions (never random spikes — section 68), driven by a
 * simple state machine so it can also power the scripted Presentation Mode
 * demonstration (section 47).
 *
 * All output is tagged SessionType.DEMO and every consumer downstream must
 * render the "SIMULATED SENSOR DATA" banner while this is active (section 83).
 */
@Singleton
class DemoDataSimulator @Inject constructor() : BleRepository {

    private val simulatorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _scenario = MutableStateFlow(DemoScenario.NORMAL)
    val scenario: StateFlow<DemoScenario> = _scenario.asStateFlow()

    private val _speed = MutableStateFlow(SimulationSpeed.NORMAL)
    val speed: StateFlow<SimulationSpeed> = _speed.asStateFlow()

    // Internal smoothly-evolving state (targets are approached gradually each tick)
    private var leftPressure = 38.0
    private var rightPressure = 40.0
    private var temperature = 30.4
    private var moisture = 16.0
    private var steps = 0
    private var elapsedTicks = 0

    private val _connectionState = MutableStateFlow(
        DeviceInfo(
            id = "DEMO-INSOLE-0001",
            connectionState = ConnectionState.DISCONNECTED,
            batteryPercent = 100,
            signalStrength = SignalStrength.EXCELLENT,
            hasImuSensor = true,
            lastSyncTimestamp = null
        )
    )

    private val _readings = MutableSharedFlow<SensorReading>(replay = 1, extraBufferCapacity = 8)

    override fun observeConnectionState(): Flow<DeviceInfo> = _connectionState.asStateFlow()
    override fun observeIncomingReadings(): Flow<SensorReading> = _readings.asSharedFlow()

    override suspend fun scan(): List<DeviceInfo> {
        delay(600) // realistic scan latency
        return listOf(_connectionState.value.copy(connectionState = ConnectionState.DISCONNECTED))
    }

    override suspend fun connect(deviceId: String): Result<Unit> {
        _connectionState.value = _connectionState.value.copy(connectionState = ConnectionState.CONNECTING)
        delay(500)
        _connectionState.value = _connectionState.value.copy(
            connectionState = ConnectionState.CONNECTED,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        return Result.success(Unit)
    }

    override suspend fun disconnect() {
        stopSimulation()
        _connectionState.value = _connectionState.value.copy(connectionState = ConnectionState.DISCONNECTED)
    }

    override suspend fun testSensors(): Map<String, Boolean> {
        delay(800)
        return mapOf("Pressure" to true, "Temperature" to true, "Moisture" to true, "IMU/Gait" to true)
    }

    override suspend fun calibrate(): Result<Unit> {
        delay(1200)
        leftPressure = 38.0; rightPressure = 40.0; temperature = 30.2; moisture = 15.0
        return Result.success(Unit)
    }

    // --- Demo controls (spec section 10) ---

    fun startSimulation() {
        if (_isRunning.value) return
        _isRunning.value = true
        tickerJob = simulatorScope.launch {
            while (isActive && _isRunning.value) {
                tick()
                delay(_speed.value.intervalMillis)
            }
        }
    }

    fun stopSimulation() {
        _isRunning.value = false
        tickerJob?.cancel()
    }

    fun setScenario(newScenario: DemoScenario) { _scenario.value = newScenario }
    fun setSpeed(newSpeed: SimulationSpeed) { _speed.value = newSpeed }

    fun simulateWalking() = setScenario(DemoScenario.WALKING)
    fun simulateStanding() = setScenario(DemoScenario.STANDING)
    fun simulateHighPressure() = setScenario(DemoScenario.HIGH_PRESSURE)
    fun simulateThermalRise() = setScenario(DemoScenario.THERMAL_RISE)
    fun simulateHighMoisture() = setScenario(DemoScenario.HIGH_MOISTURE)
    fun simulateAbnormalGait() = setScenario(DemoScenario.ABNORMAL_GAIT)

    fun resetData() {
        leftPressure = 38.0; rightPressure = 40.0; temperature = 30.4; moisture = 16.0
        steps = 0; elapsedTicks = 0
        _scenario.value = DemoScenario.NORMAL
    }

    /** One simulation step: nudges internal state gradually toward the active scenario's target. */
    private fun tick() {
        elapsedTicks++
        val t = elapsedTicks * 0.15

        val (targetLeft, targetRight, targetTemp, targetMoisture, activity) = when (_scenario.value) {
            DemoScenario.NORMAL -> Quint(40.0, 41.0, 30.3, 16.0, ActivityState.WALKING)
            DemoScenario.WALKING -> Quint(46.0, 47.0, 30.6, 17.0, ActivityState.WALKING)
            DemoScenario.STANDING -> Quint(58.0, 44.0, 30.8, 18.0, ActivityState.STANDING) // left forefoot loading up
            DemoScenario.HIGH_PRESSURE -> Quint(72.0, 46.0, 31.0, 18.0, ActivityState.STANDING)
            DemoScenario.THERMAL_RISE -> Quint(44.0, 45.0, 34.2, 20.0, ActivityState.STANDING)
            DemoScenario.HIGH_MOISTURE -> Quint(44.0, 45.0, 31.2, 35.0, ActivityState.WALKING)
            DemoScenario.ABNORMAL_GAIT -> Quint(62.0, 32.0, 30.9, 19.0, ActivityState.WALKING)
            DemoScenario.MIXED -> Quint(
                50.0 + 8 * sin(t), 46.0 + 6 * sin(t + 1), 31.5 + 1.2 * sin(t / 2), 22.0 + 5 * sin(t / 3),
                ActivityState.WALKING
            )
        }

        // Gradual approach (low-pass filter), not random spikes.
        leftPressure += (targetLeft - leftPressure) * 0.18 + Random.nextDouble(-0.6, 0.6)
        rightPressure += (targetRight - rightPressure) * 0.18 + Random.nextDouble(-0.6, 0.6)
        temperature += (targetTemp - temperature) * 0.08 + Random.nextDouble(-0.05, 0.05)
        moisture += (targetMoisture - moisture) * 0.06 + Random.nextDouble(-0.3, 0.3)
        moisture = moisture.coerceIn(5.0, 60.0)
        temperature = temperature.coerceIn(26.0, 38.0)

        if (activity == ActivityState.WALKING) steps += Random.nextInt(1, 3)

        val battery = (_connectionState.value.batteryPercent ?: 100)
        _connectionState.value = _connectionState.value.copy(
            lastSyncTimestamp = System.currentTimeMillis(),
            packetsReceived = _connectionState.value.packetsReceived + 1,
            batteryPercent = if (elapsedTicks % 120 == 0 && battery > 15) battery - 1 else battery
        )

        val reading = SensorReading(
            timestamp = System.currentTimeMillis(),
            leftPressure = leftPressure,
            rightPressure = rightPressure,
            temperature = temperature,
            moisture = moisture,
            steps = steps,
            activityState = activity,
            batteryLevel = _connectionState.value.batteryPercent ?: 100,
            sessionType = SessionType.DEMO,
            isValid = true
        )
        _readings.tryEmit(reading)
    }

    private data class Quint(val a: Double, val b: Double, val c: Double, val d: Double, val e: ActivityState)
}
