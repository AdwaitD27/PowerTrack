# High-Level Architecture

This document gives a quick, broad overview of how PowerTrack is structured and how data flows through the application.

---

## The 4 Main Components

The app is divided into four simple, clear layers:

```
[ Windows System ] 
        │ (reads battery & plug state)
        ▼
 1. Power Monitor (JNA)
        │
        ▼
 2. Session & Estimation
        │
        ├───► 3. Local Database (SQLite)
        │
        ▼
 4. UI Dashboard (JavaFX)
```

1. **Monitor Layer (`com.powertrack.monitor`)**
   - Communicates with Windows using JNA (Java Native Access) to call `GetSystemPowerStatus`.
   - Checks two basic things: Is the laptop plugged into the wall? And what is the battery percentage right now?
   - On startup, it reads the battery's full charge capacity using the Windows `powercfg` tool.

2. **Session & Estimation Layer (`com.powertrack.session` & `com.powertrack.estimation`)**
   - **Session Manager:** Tracks the active charging session. When the laptop is plugged in, it starts a timer; when unplugged, it wraps up the session.
   - **Power Estimator:** Performs the basic math. It compares battery percentage changes over time to estimate the charging wattage (Watts) and accumulated energy (Watt-hours).

3. **Database Layer (`com.powertrack.database` & `com.powertrack.settings`)**
   - Connects to a local SQLite file (`powertrack.db`).
   - Saves completed sessions (start time, end time, duration, energy used, and calculated cost).
   - Also stores user settings like the electricity rate (₹/kWh) so it persists between app restarts.

4. **UI Layer (`com.powertrack.ui`)**
   - A single JavaFX dashboard window.
   - A light timer checks for updates every 10 seconds and refreshes the numbers on screen (live power, session duration, and this month's totals).

---

## How It Works in 4 Steps (The Data Flow)

1. **App Opens:** The app loads your saved electricity rate from SQLite and detects your battery's full capacity.
2. **Charger Connected:** The monitor detects AC power is active. A new charging session begins.
3. **While Charging:** Every 10 seconds, the app checks if battery percentage increased and estimates the current charging wattage on the dashboard.
4. **Charger Disconnected:** The session ends. The total time, energy (kWh), and cost are calculated and saved to the SQLite database. The monthly total updates immediately on screen.
