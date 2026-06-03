# 🍅 FocusZone — Pomodoro Study Timer

> Stay focused. Beat distractions. Build discipline — one session at a time.

![Platform](https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android)
![Language](https://img.shields.io/badge/Language-Java-orange?style=flat-square&logo=java)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue?style=flat-square)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-yellow?style=flat-square)
![Target SDK](https://img.shields.io/badge/Target%20SDK-34-yellow?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-purple?style=flat-square)

---

## 📱 Screenshots

| Timer Screen | History Screen | Stats Screen | Settings Screen |
|---|---|---|---|
| <img src="https://github.com/user-attachments/assets/2316f337-35bc-4fe7-9df5-6e5c6a2f45f4" width="200"/> | <img src="https://github.com/user-attachments/assets/8829a268-a956-4b30-8549-4a2893e3c813" width="200"/> | <img src="https://github.com/user-attachments/assets/691a60e0-87ab-4c14-a308-2d4604e0b666" width="200"/> | <img src="https://github.com/user-attachments/assets/3e4e7af6-156d-421d-a221-3e637a27e399" width="200"/> |
---

## 📖 About

**FocusZone** is a productivity Android app based on the **Pomodoro Technique** — a time management method that uses focused work sessions followed by short breaks to maximize productivity and reduce mental fatigue.

Built entirely in **Java** with **MVVM architecture**, this app is designed to help students and professionals stay on track with deep work sessions, track their productivity history, and build consistent study habits.

---

## ✨ Features

### ⏱️ Timer
- 25-minute focus sessions with 5-minute short breaks and 15-minute long breaks
- Custom Canvas-drawn **circular progress ring** — no third-party library
- Ring color changes per mode — 🟠 Focus, 🟢 Short Break, 🔵 Long Break
- **ForegroundService** — timer keeps running even when app is closed or screen is off
- Start, Pause, Resume, Reset, and Skip controls
- Auto-advances through the full Pomodoro cycle (4 sessions → long break)
- Vibration + sound on session completion
- Motivational quotes that refresh every session

### 🔔 Notifications
- Persistent notification showing remaining time with **Pause** and **Skip** action buttons
- Session complete notification with break start action
- Break ending warning (1 minute before break ends)
- Daily reminder notification at a user-set time

### 📊 History & Stats
- Full session history stored in **Room DB**
- Sessions grouped by date with a clean RecyclerView list
- **7-day bar chart** showing sessions per day (MPAndroidChart)
- Swipe to delete a session with undo support
- Filter sessions by All / Focus / Breaks

### ⚙️ Settings
- Customisable durations for Focus, Short Break, and Long Break (SeekBar)
- Long break interval (2–6 sessions)
- Toggle notifications, vibration, and sound
- Sound selector (Bell, Digital, Soft Chime)
- Keep screen on during focus sessions
- Export session history as CSV
- Clear all history

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| UI | XML Layouts, RecyclerView, MaterialComponents |
| Custom View | Canvas API (CircularTimerView) |
| Architecture | MVVM (ViewModel + LiveData + Repository) |
| Background | ForegroundService + CountDownTimer |
| Database | Room DB (SessionEntity, SessionDao) |
| Charts | MPAndroidChart |
| Notifications | NotificationCompat, NotificationManager |
| Alarm | AlarmManager (daily reminder) |
| Preferences | SharedPreferences |
| Navigation | Navigation Component (Single Activity) |

---

## 🗂️ Project Structure

```
com.focuszone/
├── FocusZoneApplication.java        # Application class, notification channels
├── MainActivity.java                # Single activity, bottom nav, permissions
│
├── data/
│   ├── db/
│   │   ├── AppDatabase.java         # Room DB singleton
│   │   ├── SessionDao.java          # All DB queries
│   │   └── SessionEntity.java       # Room entity
│   ├── model/
│   │   ├── TimerState.java          # Enum: IDLE, RUNNING, PAUSED, COMPLETED
│   │   ├── SessionType.java         # Enum: FOCUS, SHORT_BREAK, LONG_BREAK
│   │   ├── SessionSummary.java      # Today's stats POJO
│   │   └── AllTimeStats.java        # All-time stats POJO
│   └── repository/
│       └── SessionRepository.java   # Wraps DAO, streak logic, CSV export
│
├── service/
│   └── TimerService.java            # ForegroundService, CountDownTimer, Binder
│
├── receiver/
│   ├── BootReceiver.java            # Reschedule alarm after reboot
│   └── NotificationActionReceiver.java  # Handles Pause/Skip from notification
│
├── ui/
│   ├── timer/
│   │   ├── TimerFragment.java
│   │   └── TimerViewModel.java
│   ├── history/
│   │   ├── HistoryFragment.java
│   │   ├── HistoryViewModel.java
│   │   └── SessionAdapter.java      # Grouped RecyclerView with date headers
│   ├── stats/
│   │   ├── StatsFragment.java
│   │   └── StatsViewModel.java
│   └── settings/
│       ├── SettingsFragment.java
│       └── SettingsViewModel.java
│
├── utils/
│   ├── Constants.java               # All keys and default values
│   ├── TimeUtils.java               # Time formatting helpers
│   ├── NotificationHelper.java      # Builds all notification types
│   └── PreferenceManager.java       # Typed SharedPreferences wrapper
│
└── views/
    └── CircularTimerView.java       # Custom Canvas timer ring
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17+
- Android device or emulator with API 24+

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/FocusZone.git
cd FocusZone
```

2. **Open in Android Studio**
```
File → Open → Select the FocusZone folder
```

3. **Add JitPack to settings.gradle**
```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

4. **Build the project**
```
Build → Make Project (Ctrl + F9)
```

5. **Run on device or emulator**
```
Run → Run 'app' (Shift + F10)
```

---

## 📦 Dependencies

```groovy
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.navigation:navigation-fragment:2.7.6'
implementation 'androidx.navigation:navigation-ui:2.7.6'
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
implementation 'androidx.room:room-runtime:2.6.1'
annotationProcessor 'androidx.room:room-compiler:2.6.1'
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
implementation 'androidx.work:work-runtime:2.9.0'
```

---

## 🔑 Key Implementation Highlights

### ForegroundService Timer
The timer runs inside a `ForegroundService` and binds to the UI via `ServiceConnection`. This ensures the countdown continues even when the user navigates away or locks the screen — a critical requirement for any reliable timer app.

### Custom Canvas Ring
`CircularTimerView` extends `View` and draws the progress ring manually using `Canvas` and `Paint` with `ANTI_ALIAS_FLAG`. A `ValueAnimator` ensures smooth arc transitions every second without visible jumps.

### MVVM Architecture
All business logic lives in ViewModels. Fragments only observe `LiveData` and call ViewModel methods — no direct DB access from UI layer.

### Room DB + Repository
Session history is persisted in Room DB. The `SessionRepository` handles all queries on a background thread via `ExecutorService`, keeping the main thread free.

---

## 📋 Permissions Used

| Permission | Reason |
|---|---|
| `FOREGROUND_SERVICE` | Keep timer running in background |
| `VIBRATE` | Vibrate on session complete |
| `POST_NOTIFICATIONS` | Show timer and reminder notifications |
| `RECEIVE_BOOT_COMPLETED` | Reschedule daily reminder after reboot |
| `SCHEDULE_EXACT_ALARM` | Precise daily reminder time |
| `WAKE_LOCK` | Keep screen on during focus (optional setting) |

---

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you'd like to change.

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 👨‍💻 Author

**Ayush Gupta**
MCA Student — Amity University, Noida

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=flat-square&logo=linkedin)](www.linkedin.com/in/ayush-gupta-android)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-black?style=flat-square&logo=github)]([https://github.com/yourusername](https://github.com/Ayush-2308))
[![Email](https://img.shields.io/badge/Email-Contact-red?style=flat-square&logo=gmail)](mailto:ayush.ag2308@gmail.com)

---

## 📄 License

```
MIT License

Copyright (c) 2025 Ayush Gupta

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<p align="center">Made with ❤️ and a lot of ☕ by Ayush Gupta</p>
<p align="center">⭐ Star this repo if you found it helpful!</p>
