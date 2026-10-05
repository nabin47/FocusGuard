# FocusGuard

> Block distracting apps until your tasks are done.

FocusGuard is a lightweight, minimal Android productivity application built with modern Clean Architecture, Jetpack Compose, Room, Hilt, and Coroutines.

---

## Features

- **Task Management**: Add tasks (quickly add several in a row), check them off, undo a completion or deletion from the snackbar.
- **App Blocker**: Pick installed apps (with icons and search) to block during a focus session.
- **Focus Session**: Locks selected apps until every task is done, then ends on its own. Ending early asks for confirmation, and tasks can't be deleted or apps unblocked mid-session.
- **Time Tracking**: Live session stopwatch, today's focus time, day streak, a 7-day focus chart, distractions blocked and a history of recent sessions.
- **Survives restarts**: The running session is stored in Room, so it resumes if the app process is killed.
- **Usage Monitoring**: A foreground service watches the foreground app via `UsageStatsManager`; its notification shows a live timer and the tasks left.
- **Fullscreen Overlay**: Shows the blocked app, how long you've been focused and your remaining tasks (which you can complete right there).
- **Modern Minimal UI**: Dark ink-on-paper Material 3 design with Acid Green highlights, guided permission setup and helpful empty states.

---

## Architecture Overview

FocusGuard strictly adheres to MVVM + Clean Architecture principles:

- **Presentation Layer**: Jetpack Compose UI + ViewModels (`StateFlow`) + Material3, with shared components in `presentation/component`
- **Domain Layer**: Pure Kotlin models, repository interfaces and use cases that hold real logic (`AddTaskUseCase`, `GetFocusStatsUseCase`)
- **Data Layer**: Room local SQLite persistence (tasks, blocked apps, focus sessions) + Repository implementations
- **Session State**: `FocusSessionRepository` is the single source of truth for the live session. It syncs tasks and blocked apps itself, persists every session and ends it when all tasks are done
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
  - Notifications (`POST_NOTIFICATIONS`, optional, Android 13+)

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
4. Run the unit tests:
   ```bash
   ./gradlew test
   ```

*Note: Emulators generally do not report real-time UsageStatsManager events. A physical Android device is recommended for testing app blocking.*

---

## License

This project is licensed under the [MIT License](LICENSE).
