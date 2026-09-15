# ThermoHeal-AI

**Smart Foot Health. Sustainable Materials. Intelligent Insights.**

A native Android (Kotlin + Jetpack Compose) research/wellness prototype app for a conceptual
smart insole built from banana-pseudostem-derived cellulose/nanocellulose, with passive
bio-based PCM thermoregulation, multimodal sensing (pressure, temperature, moisture, gait),
BLE connectivity, and an on-device AI wellness-insight engine.

> **Research & wellness prototype only.** ThermoHeal-AI provides wellness-monitoring
> information and decision-support insights. It is not intended to diagnose, treat, cure,
> or prevent disease. See `PrivacyScreen` / `AboutScreen` in-app for the full disclaimer.

---

## 1. What's in this build

This is a full source tree, not a mockup: ~65 Kotlin files across a Clean
Architecture (data / domain / presentation) split, covering every screen and
module described in the product brief — onboarding, auth, home dashboard,
live monitoring, per-sensor detail screens, AI insights with explainability,
history, device management, biomaterial/thermoregulation/sustainability/
research modules, Presentation Mode, settings, privacy, and PDF report export.

**Fully implemented and wired end-to-end:**
- Jetpack Compose UI on Material 3, full light/dark theme system matching the
  specified color palette exactly (`ui/theme/Color.kt`)
- Navigation Compose graph connecting every screen (`navigation/ThermoHealNavGraph.kt`) —
  no dead ends, every primary button performs a real action
- Room database (9 tables) with a repository layer per spec section 32
- **Research Demo Mode**: a realistic sensor simulator (`DemoDataSimulator`) that
  generates gradually-evolving pressure/temperature/moisture/activity data
  under named scenarios (Normal, Walking, Standing, High Pressure, Thermal Rise,
  High Moisture, Abnormal Gait) — the app is fully usable with **zero hardware**
- A clean `BleRepository` abstraction so a physical insole can be wired in later
  without touching any ViewModel or screen (see §4 below)
- A rule-based `AIEngine` (explicitly labeled "Prototype AI / Rule-based
  demonstration") producing wellness insights with severity, confidence,
  and an explainability breakdown ("Why am I seeing this?")
- DataStore-backed persistence for theme, units, notification prefs, and
  session login — survives process death
- Hilt dependency injection throughout
- PDF weekly report generation (iText7)
- Presentation Mode: a scripted, auto-advancing 10-step demonstration flow
  with a dark "control room" visual style, sized for projector use

**Design choices worth knowing about:**
- Charts are hand-built with Compose `Canvas` (see `ui/components/Charts.kt`)
  rather than an external charting library, to keep the dependency surface
  small — the spec allowed "MPAndroidChart OR a modern Compose-compatible
  charting library," and a native Canvas implementation satisfies that while
  staying fully within Compose's theming system.
- The splash sequence (molecular → leaf → footprint → circuit → wave → logo)
  is built with native Compose animation APIs rather than a Lottie JSON asset,
  since no Lottie file was supplied — `lottie-compose` is still wired into
  Gradle if you want to drop in a real animation file later.
- Firebase Auth/Firestore/Storage dependencies are commented out in
  `app/build.gradle.kts` and the app ships with a local-first Room + DataStore
  auth implementation instead, so it runs immediately with no backend setup.
  Uncomment and add `google-services.json` to switch over — `UserRepository`
  is already an interface, so swapping the implementation doesn't touch UI code.

## 2. Important, honest caveat about this specific delivery

This project was generated in a sandboxed environment **without the Android
SDK, Gradle, or network access**, so I was not able to run `./gradlew build`
or an emulator against it here. Every file was written and cross-checked by
hand for correct Kotlin/Compose syntax, consistent types across the
data → domain → presentation layers, and consistent API usage against the
dependency versions pinned in `app/build.gradle.kts`, but **the very first
thing you should do is open it in Android Studio and let Gradle sync** — that
will surface anything a static read-through missed (a stray import, a
version mismatch, etc.) far faster than I can by inspection alone. Given the
size of this brief, treat this as a strong, working starting scaffold rather
than a guaranteed zero-error build.

## 3. Getting started

1. Open the `ThermoHealAI/` folder in **Android Studio (Koala or newer)**.
2. Let Gradle sync — it will fetch the Gradle wrapper jar automatically.
3. Run on an emulator or device (minSdk 26 / target 34).
4. On first launch: Splash → Onboarding → Login screen → **"Continue as Demo
   User"** → Device Setup → Calibration → Home.
5. On the Home screen (or Device screen), tap **Start Demo** to begin the
   sensor simulation — every screen fills with live, realistic simulated data.

No API keys, no backend, no physical hardware required to fully exercise the app.

## 4. Wiring up real hardware later

Everything hardware-facing goes through `data/bluetooth/BleRepository.kt`.
Today `RepositoryModule` binds it to `DemoDataSimulator`. To integrate a real
ESP32 / nRF52-class insole:

1. Implement `RealBleRepository` (stub already present) using
   `BluetoothLeScanner` / `BluetoothGatt` against your board's GATT profile.
2. Flip the `@Binds` target in `di/AppModules.kt` from `DemoDataSimulator` to
   `RealBleRepository`.
3. Nothing else changes — every ViewModel and screen depends only on the
   `BleRepository` interface (or the domain repositories built on top of it).

## 5. Project structure

```
app/src/main/java/com/thermoheal/ai/
 ├── data/           Room entities/DAOs, BLE + demo simulator, AI engine impl, repositories
 ├── domain/         Models, repository interfaces, use cases (no Android deps)
 ├── di/             Hilt modules
 ├── navigation/      Screen routes, NavHost graph, root theme/session ViewModel
 ├── presentation/    One package per screen/module (see spec §11)
 ├── ui/
 │    ├── theme/      Color system, typography, shapes
 │    └── components/ Shared design-system components (cards, charts, heatmap, badges…)
 └── utils/          DataStore preferences manager, PDF report generator
```

## 6. Verification checklist (spec §102)

| Item | Status |
|---|---|
| Compiles | Not run in this environment (no SDK/network) — verify via Android Studio sync, see §2 |
| Navigation — every screen reachable, no dead ends | Wired in `ThermoHealNavGraph.kt`; reviewed by hand |
| Every primary button functional | Reviewed; each button calls a real ViewModel action or navigation call |
| Dark mode | Full second color scheme (`ui/theme/Theme.kt`), togglable in Settings |
| Demo mode | `DemoDataSimulator` + `DemoModeBanner` shown wherever simulated data is displayed |
| Sensor simulation | Gradual, scenario-driven — see §92-94 in the original brief, `DemoDataSimulator.kt` |
| Data persistence | Room (sensor/AI history) + DataStore (prefs/session) |
| AI insight generation | `RuleBasedAIEngine` + `SensorIngestionManager` (auto-runs every ~15 readings) |
| Charts | Native Compose Canvas line charts, heatmap, gauges |
| Presentation Mode | `PresentationModeScreen.kt` — auto-advancing 10-step flow |
| Crash safety | Repository calls wrapped in `Result`/try-catch at the boundaries most likely to fail (auth, BLE, calibration) |
| Responsive layouts | Compose layouts use `fillMaxWidth()`/`weight()` throughout rather than fixed dp widths |
| Safety disclaimers | Present on Home, Insights, Privacy, About, and the PDF report footer |

## 7. What's intentionally left as a next step

- Real hardware GATT implementation (`RealBleRepository`) — stubbed with a
  clear `NotImplementedError` and docstring pointing at what to fill in.
- Firebase wiring (commented out, ready to enable).
- A trained ML model behind `AIEngine` — the interface is model-agnostic;
  `RuleBasedAIEngine` is explicitly labeled as a prototype throughout the UI.
- Instrumented/unit test suite: `testImplementation`/`androidTestImplementation`
  dependencies are already in `build.gradle.kts`; add test classes under
  `app/src/test` and `app/src/androidTest` as the next milestone.
