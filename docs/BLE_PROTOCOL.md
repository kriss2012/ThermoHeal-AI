# ThermoHeal-AI BLE Communication Protocol Specification

> **STATUS: HARDWARE PROTOCOL PENDING FINAL HARDWARE RATIFICATION**
> All GATT UUIDs and packet structures documented herein are implemented in `com.thermoheal.ai.data.bluetooth.BleProtocolConfig` and verified against the synthetic hardware simulator `DemoDataSimulator`. When the production custom smart insole hardware firmware is finalized, update `BleProtocolConfig.kt` to match the manufacturer's allocated 128-bit UUIDs without touching domain logic.

---

## 1. Overview

ThermoHeal-AI interfaces with a pair of instrumented smart insoles (Left Foot, Right Foot) equipped with:
- Matrix piezo-resistive pressure sensors (Hallux, Heel, First Metatarsal Head, Lateral Forefoot)
- Dual NTC thermal sensors (Foot surface temperature vs. Ambient/Reference)
- Capacitive moisture/perspiration sensor
- 6-axis IMU (3-axis Accelerometer + 3-axis Gyroscope) for gait kinematics
- Fuel gauge / Battery monitoring IC

---

## 2. GATT Service Architecture

### 2.1 Primary Service
- **Service Name**: ThermoHeal Sensor Service
- **Service UUID**: `0000TH01-0000-1000-8000-00805F9B34FB` (Pending: `0000FFE0-0000-1000-8000-00805F9B34FB` or vendor 128-bit)

### 2.2 Characteristics

| Characteristic | UUID | Properties | Format / Payload Size | Description |
|---|---|---|---|---|
| **Pressure Matrix** | `...-0001` | Read, Notify | 12 Bytes | 4x 16-bit unsigned integers (kPa * 10) + foot + timestamp |
| **Temperature** | `...-0002` | Read, Notify | 6 Bytes | 2x 16-bit signed integers (°C * 100) + status |
| **Moisture** | `...-0003` | Read, Notify | 3 Bytes | 1x 8-bit unsigned (0-100% RH) + 16-bit raw impedance |
| **Gait IMU** | `...-0004` | Read, Notify | 14 Bytes | Cadence (16b), Symmetry (16b), Pronation angle (16b), Impact (16b) |
| **Device Control** | `...-0005` | Write, Write Without Response | 4 Bytes | Calibration triggers, sampling rate control, sleep mode |
| **Battery / Status** | `00002A19-...` | Read, Notify | 2 Bytes | Standard BLE Battery Service (0-100%, charging status) |

---

## 3. Packet Binary Specifications

All multi-byte numeric values are encoded in **Little-Endian** byte order.

### 3.1 Pressure Packet (Characteristic `0001`)
```
Offset  Size  Type    Field Name       Description / Scaling
0x00    1B    uint8   Foot & Quality   Bit 0: 0=Left, 1=Right; Bits 1-3: SensorQuality (0=Valid, 1=Degraded, 2=Faulty)
0x01    2B    uint16  Hallux (Toe)     Pressure in kPa * 10 (0 - 15000 -> 0.0 - 1500.0 kPa)
0x03    2B    uint16  Metatarsal 1     Pressure in kPa * 10 (0 - 15000)
0x05    2B    uint16  Lateral Forefoot Pressure in kPa * 10 (0 - 15000)
0x07    2B    uint16  Heel (Calcaneus) Pressure in kPa * 10 (0 - 15000)
0x09    3B    uint24  TimestampDelta   Offset in milliseconds since session start
Total: 12 Bytes
```

### 3.2 Temperature Packet (Characteristic `0002`)
```
Offset  Size  Type    Field Name       Description / Scaling
0x00    1B    uint8   Sensor ID        Bit 0: 0=Left, 1=Right; Bit 7: Ambient Available flag
0x01    2B    int16   Foot Surface     Temperature in °C * 100 (e.g., 3450 = 34.50 °C)
0x03    2B    int16   Ambient / Ref    Temperature in °C * 100 (e.g., 2200 = 22.00 °C)
0x05    1B    uint8   Status / Quality 0x00 = Normal, 0x01 = Thermal Gradient Alert, 0xFF = Sensor Fault
Total: 6 Bytes
```

### 3.3 Moisture / Microclimate Packet (Characteristic `0003`)
```
Offset  Size  Type    Field Name       Description / Scaling
0x00    1B    uint8   Relative Humidity 0 to 100 (% RH)
0x01    2B    uint16  Impedance (kOhm) Raw skin-interface impedance in kOhm * 10
Total: 3 Bytes
```

### 3.4 Gait Kinematics Packet (Characteristic `0004`)
```
Offset  Size  Type    Field Name       Description / Scaling
0x00    2B    uint16  Cadence          Steps per minute * 10 (e.g., 1050 = 105.0 spm)
0x02    2B    uint16  Symmetry Index   Ratio * 1000 (1000 = 1.00 perfect symmetry)
0x04    2B    int16   Pronation Angle  Angle in degrees * 10 (-450 to +450 -> -45.0° to +45.0°)
0x06    2B    uint16  Impact Force G   Acceleration in G-forces * 100 (e.g., 140 = 1.40 G)
0x08    2B    uint16  Contact Time Ms  Stance phase duration in milliseconds
0x0A    4B    uint32  Cumulative Steps Cumulative step count since device power-on
Total: 14 Bytes
```

---

## 4. Connection State Machine & Link Management

### 4.1 Reconnection Strategy
The Android client implements a self-healing exponential backoff reconnect policy:
1. **Initial Retry**: 1,000 ms delay
2. **Subsequent Retries**: 2,000 ms, 4,000 ms, 8,000 ms, up to a maximum clamp of 30,000 ms.
3. **Scan Timeout**: 10,000 ms per active scan pass.
4. **Auto-Reconnect**: Enabled via `BluetoothGatt.connect()` with `autoConnect = true` for bonded/paired insoles.

### 4.2 MTU Negotiation
Upon successful connection (`STATE_CONNECTED`), the Android client requests an MTU of **247 bytes**:
```kotlin
gatt.requestMtu(247)
```
If the hardware device responds with a lower MTU (default ATT MTU = 23 bytes, yielding a 20-byte payload limit), the client packet parser gracefully falls back to split single-characteristic notifications.

### 4.3 Descriptor Setup
Notifications are enabled on all sensor characteristics by writing the Client Characteristic Configuration Descriptor (CCCD `0x2902`):
```kotlin
val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
gatt.writeDescriptor(descriptor)
```

---

## 5. Medical & Regulatory Boundary

> **IMPORTANT DISCLAIMER**: The raw BLE packets and derived calculations provide **wellness metrics only** and are **not diagnostic medical instruments**. They must not be used for autonomous wound staging or clinical triage. All presentation UI must display the statutory disclaimer.
