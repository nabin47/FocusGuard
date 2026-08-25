# FocusGuard

> Block distracting apps until your tasks are done.

FocusGuard is a lightweight, minimal Android productivity application built with modern Clean Architecture, Jetpack Compose, Room, Hilt, and Coroutines.

---

## Features

- **Task Management**: Define custom tasks to complete during a focus session.
- **App Blocker**: Select installed apps to block per active session.
- **Focus Session**: Locks selected apps while tasks remain incomplete.
- **Usage Monitoring**: Background foreground service continuously monitors active foreground apps using `UsageStatsManager`.
- **Fullscreen Overlay**: Displays a non-dismissible full-screen overlay when a blocked app is opened.
- **Minimal Aesthetic**: Modern dark ink-on-paper UI design with negative space and Acid Green highlights.

---

## Architecture Overview

FocusGuard strictly adheres to MVVM + Clean Architecture principles:

- **Presentation Layer**: Jetpack Compose UI + ViewModels (`StateFlow`) + Material3
- **Domain Layer**: Pure Kotlin models, repository interfaces, and isolated UseCases
- **Data Layer**: Room local SQLite persistence + Repository implementations
- **Service Layer**: `FocusMonitorService` foreground service + `UsageWatcher`
- **Dependency Injection**: Hilt (compile-time safety)

---

## Requirements

- **Target SDK:** 35 (Android 15)
- **Min SDK:** 26 (Android 8.0 Oreo)
- **Permissions:**
  - Usage Access (`PACKAGE_USAGE_STATS`)
  - Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)
  - Foreground Service (`FOREGROUND_SERVICE_SPECIAL_USE`)

---

## Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/focusguard.git
   cd focusguard
   ```
2. Open in **Android Studio** or **Antigravity IDE**.
3. Build & run on a physical Android device (min API 26):
   ```bash
   ./gradlew assembleDebug
   ```

*Note: Emulators generally do not report real-time UsageStatsManager events. A physical Android device is recommended for testing app blocking.*

---

## License

This project is licensed under the [MIT License](LICENSE).
