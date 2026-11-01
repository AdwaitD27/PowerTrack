# Architecture Overview

PowerTrack follows a modular architecture separating concerns between data acquisition, logic, persistence, and presentation.

## Classes & Responsibilities

- **Launcher.java / Main.java**: `Launcher` provides a non-JavaFX entry point for the Fat JAR. `Main` initializes JavaFX, sets up the SQLite database, wires dependencies, starts the background monitoring thread, and launches the UI.
- **PowerMonitor (com.powertrack.monitor)**: Uses JNA (`GetSystemPowerStatus`) to query Windows for the current AC connection state and battery percentage. On startup, it spawns `powercfg /batteryreport /xml` via standard `ProcessBuilder` and parses the XML to extract the exact `FullChargeCapacity` (mWh) of the battery.
- **SessionManager (com.powertrack.session)**: Listens for AC power state changes. When AC is connected, it begins a session; when disconnected, it ends the session, calculates duration, utilizes `PowerEstimator` to get the estimated energy, and saves the session using `DatabaseManager`.
- **PowerEstimator (com.powertrack.estimation)**: Contains the logic to estimate battery charging power in Watts based on actual battery capacity and real-time percentage changes (`delta Wh / delta time`). If capacity is unavailable or battery is at 100%, it returns -1 to indicate telemetry is unavailable.
- **DatabaseManager (com.powertrack.database)**: Handles JDBC SQLite interactions. It initializes tables (`power_sessions`, `settings`), and performs inserts and aggregations (e.g., getting the current month's energy and cost totals).
- **SettingsManager (com.powertrack.settings)**: Manages application configuration (primarily the electricity rate in ₹/kWh) with persistence to the `settings` table.
- **Dashboard (com.powertrack.ui)**: A minimalist JavaFX interface that displays the real-time AC state, battery percentage, real-time current session battery charging power estimations, and this month's aggregated energy and cost. Uses a JavaFX `Timeline` to poll monitoring securely on the UI thread without blocking. Shows "N/A" for unavailable telemetry.

## Data Flow
1. **Startup**: `Main` triggers database initialization. `SettingsManager` loads rates. `PowerMonitor` checks initial capacity via `powercfg`.
2. **Monitoring**: A JavaFX `Timeline` polls `PowerMonitor` periodically (e.g., every 10 seconds) safely updating telemetry in `PowerEstimator`.
3. **Session Cycle**: If AC connects, `SessionManager` starts tracking. While tracking, `Dashboard` updates live estimated battery charging Watts. When AC disconnects, a session record is finalized and inserted into SQLite.
4. **UI Updates**: `Dashboard` updates periodically via `Platform.runLater()` reflecting current states and pulling updated monthly totals from `DatabaseManager` when a session concludes.
