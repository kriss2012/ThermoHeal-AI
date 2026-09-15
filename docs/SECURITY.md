# ThermoHeal-AI Security, Privacy & Threat Model

---

## 1. Threat Model & Mitigations

| Threat | Vector | Severity | Mitigation Strategy |
|---|---|---|---|
| **BLE Packet Sniffing / MITM** | Over-the-air radio interception | High | Future firmware binding requires BLE Secure Connections (LE SC, numeric comparison). RealBleRepository implements packet bounds validation (`SensorValidator.kt`). |
| **Data Extraction from Device** | Physical device theft or rooted access | High | Room database can be wrapped with SQLCipher if hardware keystore backing is enabled. ProGuard/R8 obfuscation strips debug symbols and inlines security constants. |
| **Unauthorized Cloud Sync** | Man-in-the-Middle network tampering | Critical | WorkManager `SyncWorker` communicates exclusively via TLS 1.3 with Certificate Pinning (configurable via `network_security_config.xml`). |
| **Data Leakage in Backups** | Android auto-cloud backup | Medium | `android:allowBackup="false"` enforced in `AndroidManifest.xml` to prevent unencrypted cloud extraction. |
| **Accidental Medical Misuse** | User treating app as physician diagnostic | Critical | Mandatory disclaimers on every export (PDF, CSV, JSON) and UI screen ("Wellness insight only — not a medical diagnosis"). |

---

## 2. Privacy Center & Data Rights (GDPR & HIPAA Alignment)

ThermoHeal-AI includes full on-device data sovereignty controls in `presentation/privacy/PrivacyScreen.kt`:
1. **Right to Access & Portability**:
   - One-tap export to standardized CSV and JSON formats.
   - Comprehensive multi-page PDF generation for clinical consultation sharing.
2. **Right to Erasure ("Forget Me")**:
   - `clearUserData()` completely purges all sensor readings (pressure, temperature, moisture, gait, summaries, and AI insights) while retaining account preferences.
   - `deleteAccount()` performs a nuclear purge: deletes all tables, resets SharedPreferences/EncryptedSharedPreferences, and safely cancels background sync workers.
3. **Data Provenance & Auditability**:
   - Every reading includes `userId`, `sessionId`, `createdAt`, and `SensorQuality` grading (VALID, DEGRADED, FAULTY).

---

## 3. Build & Release Hardening

1. **R8 / ProGuard Configuration**:
   - Managed via `app/proguard-rules.pro`.
   - Strips unused code, shrinks resources, and obfuscates class names and reflection paths.
   - Explicit keep rules protect Room entities, DAOs, Hilt components, and Coroutine dispatchers.
2. **Secrets Protection**:
   - `local.properties` and `.env` are strictly excluded in `.gitignore`.
   - Template provided in `.env.example` and `local.properties.example`.
   - Never commit production keystore keys or backend API tokens to version control.
