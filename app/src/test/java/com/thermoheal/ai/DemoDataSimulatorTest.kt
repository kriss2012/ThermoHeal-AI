package com.thermoheal.ai

import com.thermoheal.ai.data.bluetooth.DemoDataSimulator
import com.thermoheal.ai.data.bluetooth.DemoScenario
import com.thermoheal.ai.data.bluetooth.SimulationSpeed
import com.thermoheal.ai.domain.model.ConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DemoDataSimulatorTest {

    private lateinit var simulator: DemoDataSimulator

    @Before
    fun setUp() {
        simulator = DemoDataSimulator()
    }

    @Test
    fun testInitialStateIsDisconnectedAndNotRunning() {
        assertFalse(simulator.isRunning.value)
        assertEquals(DemoScenario.NORMAL, simulator.scenario.value)
        assertEquals(SimulationSpeed.NORMAL, simulator.speed.value)
    }

    @Test
    fun testConnectChangesStateToConnected() = runTest {
        val result = simulator.connect("DEMO-INSOLE-0001")
        assertTrue(result.isSuccess)
    }

    @Test
    fun testDisconnectStopsSimulation() = runTest {
        simulator.startSimulation()
        assertTrue(simulator.isRunning.value)

        simulator.disconnect()
        assertFalse(simulator.isRunning.value)
    }

    @Test
    fun testScenarioTransitions() {
        simulator.simulateWalking()
        assertEquals(DemoScenario.WALKING, simulator.scenario.value)

        simulator.simulateStanding()
        assertEquals(DemoScenario.STANDING, simulator.scenario.value)

        simulator.simulateHighPressure()
        assertEquals(DemoScenario.HIGH_PRESSURE, simulator.scenario.value)

        simulator.simulateThermalRise()
        assertEquals(DemoScenario.THERMAL_RISE, simulator.scenario.value)

        simulator.simulateHighMoisture()
        assertEquals(DemoScenario.HIGH_MOISTURE, simulator.scenario.value)

        simulator.resetData()
        assertEquals(DemoScenario.NORMAL, simulator.scenario.value)
    }

    @Test
    fun testSensorTestReturnsAllTrue() = runTest {
        val sensors = simulator.testSensors()
        assertEquals(4, sensors.size)
        assertTrue(sensors.values.all { it })
    }

    @Test
    fun testCalibrateSucceeds() = runTest {
        val result = simulator.calibrate()
        assertTrue(result.isSuccess)
    }
}
