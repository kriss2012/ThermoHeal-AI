# ThermoHeal-AI: Production-Grade Android Health Architecture

[![Android CI/CD Pipeline](https://github.com/kriss2012/ThermoHeal-AI/actions/workflows/android.yml/badge.svg)](https://github.com/kriss2012/ThermoHeal-AI/actions/workflows/android.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.06.00-brightgreen.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![MinSdk](https://img.shields.io/badge/MinSdk-26-orange.svg)](https://developer.android.com)
[![TargetSdk](https://img.shields.io/badge/TargetSdk-34-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](#disclaimer)

**ThermoHeal-AI** is a production-grade, native Android application engineered for continuous diabetic plantar ulcer prevention and personalized foot health monitoring. Built for next-generation smart insoles crafted from banana-pseudostem-derived cellulose, bio-based phase change materials (PCM), and multimodal sensor arrays.

> **LEGAL & REGULATORY NOTICE**:
> *Wellness insight only — not a medical diagnosis.*
> ThermoHeal-AI is designed for wellness monitoring, thermal-pressure trend tracking, and personal decision support. It does not provide medical diagnoses or replace licensed clinical intervention. All exports and screens strictly adhere to this boundary.

---

## Architecture & System Overview

ThermoHeal-AI is structured strictly around **Clean Architecture** principles and the Google Android App Architecture guide:

```
                  ┌─────────────────────────────────────┐
                  │      Presentation Layer (Compose)   │
                  │  HomeScreen, LiveMonitor, Insights  │
                  │  HistoryScreen, PrivacyCenter, etc. │
                  └──────────────────┬──────────────────┘
                                     │ StateFlow / Actions
                  ┌──────────────────▼──────────────────┐
                  │          ViewModel Layer            │
                  │ LiveMonitorVM, InsightsVM, PrivacyVM│
                  └──────────────────┬──────────────────┘
                                     │ Use Cases
                  ┌──────────────────▼──────────────────┐
                  │            Domain Layer             │
                  │   SensorValidator, ExportDataUseCase│
                  │   SensorModels, AIInsight, Repos    │
                  └──────────────────┬──────────────────┘
                                     │ Abstractions
                  ┌──────────────────▼──────────────────┐
                  │             Data Layer              │
                  ├──────────────────┬──────────────────┤
                  │   Local SQLite   │    BLE Stream    │
                  │  Room v2 + DAOs  │  Composite Router│
                  │  MIGRATION_1_2   │  ┌──────┴──────┐ │
                  │  SyncQueue Dao   │  │ Real BLE    │ │
                  │                  │  │ Demo Sim    │ │
                  └──────────────────┴──┴─────────────┴─┘
```

---

## Key Production Features

### 1. Data Provenance & Room v2 Migration
- Database version upgraded from `1` to `2` using non-destructive, zero-data-loss migration (`MIGRATION_1_2`).
- Every sensor reading is branded with immutable audit columns: `userId`, `sessionId`, `createdAt`, and `SensorQuality` grading (`VALID`, `DEGRADED`, `FAULTY`).
- New tables added: `monitoring_sessions` for continuous walk/run session tracking and `sync_queue` for reliable offline-first cloud synchronization.

### 2. Dual-Mode BLE Architecture (Real vs. Synthetic Separation)
- **`BleProtocolConfig`**: Centralized, hardware-agnostic BLE UUID definitions marked `HARDWARE PROTOCOL PENDING` for seamless firmware handoff.
- **`RealBleRepository`**: Complete Android BLE stack with `BluetoothLeScanner`, MTU negotiation (247 bytes), CCCD descriptor registration, and exponential backoff auto-reconnect.
- **`DemoDataSimulator`**: Synthetic hardware simulator generating deterministic normal, walking, high pressure, and thermal rise scenarios for zero-hardware evaluation.
- **`CompositeBleRepository`**: Dynamic router that transparently dispatches to `RealBleRepository` or `DemoDataSimulator` depending on user configuration. Simulated data is tagged with `SessionType.DEMO` and never pollutes clinical records.

### 3. Clinically Grounded Heuristic AI Engine
- Deterministic, explainable rule-based scoring engine (`RuleBasedAIEngine.kt`) producing:
  - **Peak Pressure Index (PPI)**: Detection of dangerous sustained localized pressure (> 350 kPa).
  - **Thermal Hotspot Differential ($\Delta T$)**: Identifies asymmetry > 2.2°C preceding ulceration.
  - **Maceration Risk Score**: Monitors excessive relative humidity (> 75% RH).
  - **Kinematic Gait Asymmetry**: Evaluates stance time imbalance (> 15%).
  - **Composite Wellness Index**: 0-100 weighted metric with explainable breakdown.
- Versioned outputs: all insights carry `modelVersion`, `algorithmVersion`, and `featureVersion` (v1.0.0).

### 4. Background Sync & Offline-First Engine
- Implemented via AndroidX WorkManager (`SyncWorker.kt` and `SyncManager.kt`).
- Queued sync entries (`SyncQueueEntity`) process in FIFO order with exponential backoff retry policies and network constraints.

### 5. Privacy Center & Data Sovereignty
- **Right to Access & Portability**: Direct one-tap export of historical readings to RFC 4180 CSV, formatted JSON, and multi-page printable medical consultation PDFs.
- **Right to Erasure ("Forget Me")**: Two-tiered deletion workflow in `presentation/privacy/PrivacyScreen.kt`:
  - *Clear Sensor History*: Purges all local readings, daily summaries, and AI insights.
  - *Delete Account*: Irrevocably purges all user data, resets secure preferences, and cancels active sync workers.

### 6. R8/ProGuard Hardening & Threat Model
- ProGuard rules (`app/proguard-rules.pro`) configured to strip debug symbols, protect Room and Hilt bindings, and obfuscate sensitive application logic.
- Cloud credentials and keys isolated into `.env` and `local.properties` (never committed to git).

---

## Project Structure

```
app/src/main/java/com/thermoheal/ai/
├── data/
│   ├── ai/              # RuleBasedAIEngine & clinical heuristics
│   ├── bluetooth/       # BleProtocolConfig, RealBleRepository, DemoDataSimulator
│   ├── local/           # Room Database v2, Entities, Daos, MIGRATION_1_2
│   ├── pdf/             # iText7 Medical PDF Report Generator
│   ├── repository/      # Repository implementations & Domain mappers
│   └── sync/            # WorkManager SyncWorker & SyncManager
├── di/                  # Hilt Dependency Injection Modules (Database, Repositories)
├── domain/
│   ├── model/           # SensorModels, AIInsight, UserProfile, BleConnectionState
│   ├── repository/      # Clean Architecture repository interfaces
│   └── usecase/         # SensorValidator, ExportDataUseCase, Scoring
├── presentation/        # Jetpack Compose screens (Home, LiveMonitor, History, Privacy, etc.)
└── ui/
    ├── components/      # Reusable UI cards, gauges, Canvas charts, disclaimers
    └── theme/           # ThermoHeal teal brand theme, Typography, Shapes
```

---

## Technical Documentation Index

Complete technical and protocol specifications are maintained in the [`docs/`](file:///c:/Users/IMRD/Documents/GitHub/ThermoHeal-AI/docs) directory:
- [BLE Protocol Specification](file:///c:/Users/IMRD/Documents/GitHub/ThermoHeal-AI/docs/BLE_PROTOCOL.md)
- [AI Engine & Clinical Decision Support](file:///c:/Users/IMRD/Documents/GitHub/ThermoHeal-AI/docs/AI_ENGINE.md)
- [Security, Privacy & Threat Model](file:///c:/Users/IMRD/Documents/GitHub/ThermoHeal-AI/docs/SECURITY.md)
- [Database Schema & Migration Guide](file:///c:/Users/IMRD/Documents/GitHub/ThermoHeal-AI/docs/DATABASE_MIGRATION.md)

---

## Building & Verification

### Prerequisites
- Android Studio Koala (2024.1+) or newer
- JDK 17 (Temurin or Android Studio JBR)
- Android SDK Platform 34

### Run Unit Tests
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew testDebugUnitTest
```

### Build Debug APK
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## CI/CD Pipeline
Continuous Integration is configured using GitHub Actions in [`.github/workflows/android.yml`](file:///c:/Users/IMRD/Documents/GitHub/ThermoHeal-AI/.github/workflows/android.yml). Every push and PR automatically verifies:
1. JDK 17 setup & Gradle package caching
2. Full JVM unit test suite (`./gradlew testDebugUnitTest`)
3. Debug APK build verification (`./gradlew assembleDebug`)
4. Artifact archival of test reports and generated APKs

---

## License & Medical Disclaimer
All software rights reserved. **Wellness insight only — not a medical diagnosis.**
