# ⚡ PowerTrack

> A clean, lightweight Windows desktop app that tracks your laptop's charging sessions, estimates power consumption, and calculates your monthly electricity costs.

---

## 💡 Why PowerTrack?

Most of us leave our laptops plugged in for hours every day without thinking twice about electricity usage. But unlike smart appliances, laptops don’t come with a built-in meter telling you how much power they’re drawing or how much they add to your electricity bill.

**PowerTrack** solves this problem quietly in the background. It automatically detects when your laptop is plugged in, measures your battery's charging rate in real time, and logs your energy usage session by session—so you know exactly what your machine is costing you each month.

No extra hardware, no cloud accounts, and no bloated telemetry. Everything runs locally on your PC.

---

## ✨ Features

- **🔌 Automatic AC Detection:** Senses when your laptop is plugged into the wall and starts tracking automatically. When you unplug, it wraps up the session and saves your stats.
- **⚡ Live Power Estimation:** Calculates your battery's real-time charging power (in Watts) based on actual capacity and percentage changes.
- **💰 Monthly Cost Tracking:** Aggregates your power consumption (kWh) for the current month and calculates the total cost based on your local electricity tariff.
- **⚙️ Custom Electricity Rate:** Set your rate per kWh (e.g., ₹/kWh) right on the dashboard—it saves automatically to your local settings.
- **🔒 100% Offline & Private:** Stored locally in a lightweight SQLite database (`powertrack.db`). No servers, no tracking, no internet required.
- **🪶 Minimal Footprint:** Low-frequency 10-second polling keeps CPU and memory overhead practically unnoticeable.

---

## 🖥️ What the Dashboard Shows

```
+-------------------------------------------------------+
|                    PLUGGED IN                         |
|                   Battery: 78%                        |
|                                                       |
|   Current Session:                 0h 42m             |
|   Est. Battery Charging Power:     32.4 W             |
|   Est. Battery Charging Energy:    22.7 Wh            |
|   Cost (Battery Charging):         ₹0.16              |
|                                                       |
|   ----------------- THIS MONTH --------------------   |
|   Total Est. Battery Energy:       4.85 kWh           |
|   Total Cost (Battery Charging):   ₹33.95             |
|                                                       |
|   Rate (₹/kWh): [ 7.0 ] [ Save ]                      |
+-------------------------------------------------------+
```

---

## 🛠️ Tech Stack

- **Language:** Java 22
- **GUI:** JavaFX 22
- **Build Tool:** Apache Maven
- **Database:** SQLite (via Xerial JDBC driver)
- **Native OS Integration:** JNA (Java Native Access) hooking directly into the Windows `Kernel32.dll` power APIs

---

## 🔍 How It Works (In Plain English)

1. **Reading Windows Power Status:** Through JNA, PowerTrack listens to Windows `GetSystemPowerStatus` to know the millisecond you plug in or unplug your charger.
2. **Inspecting Battery Capacity:** On startup, PowerTrack quietly generates a native Windows battery report (`powercfg /batteryreport /xml`) to read your exact battery design & full-charge capacity in milliwatt-hours (mWh).
3. **Calculating Watts on the Fly:** As your battery charges, PowerTrack tracks the delta in percentage over time:
   $$\text{Charging Power (Watts)} = \frac{\Delta\% \times \text{Full Capacity (Wh)}}{\text{Time Elapsed (Hours)}}$$
4. **Local Logging:** Each time you disconnect, your session (start time, end time, duration, energy used, and calculated cost) is committed to an embedded SQLite database.

> **Honest Note on Methodology:**
> PowerTrack is a software-based monitor. It measures the energy flowing into the battery based on Windows power telemetry. It is not a physical wall meter (like a Kill-A-Watt), meaning it doesn't measure charger brick heat loss or the idle power drawn when the battery sits at 100% full charge. When your battery hits 100%, the app marks active charging wattage as `N/A` rather than showing fake numbers.

---

## 🚀 Getting Started

### Prerequisites

- **OS:** Windows 10 or 11
- **Java:** JDK 22 or newer ([Adoptium Temurin](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/))
- **Maven:** Version 3.8+ ([Download Maven](https://maven.apache.org/download.cgi))

Check your setup in PowerShell or Command Prompt:
```bash
java -version
mvn -version
```

### Installation & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/AdwaitDendge/PowerTrack.git
   cd PowerTrack
   ```

2. **Run directly using JavaFX Maven Plugin:**
   ```bash
   mvn clean javafx:run
   ```

3. **Or build a standalone Fat JAR:**
   ```bash
   mvn clean package
   java -jar target/PowerTrack-1.0.0-SNAPSHOT.jar
   ```

---

## 📂 Project Structure

```text
PowerTrack/
├── src/
│   └── main/
│       └── java/com/powertrack/
│           ├── database/      # SQLite connection & session queries
│           ├── estimation/    # Wattage and energy estimation formulas
│           ├── monitor/       # Windows Kernel32 power status & powercfg parser
│           ├── session/       # AC plug-in session lifecycle tracker
│           ├── settings/      # User settings & tariff rate storage
│           ├── ui/            # JavaFX Dashboard & live status updates
│           ├── Launcher.java  # Non-modular Fat-JAR entrypoint
│           └── Main.java      # JavaFX Application entrypoint
├── ARCHITECTURE.md            # In-depth architectural documentation
├── DECISIONS.md               # Technical choices & tradeoffs
├── PROJECT_SPEC.md            # Original project specifications
├── pom.xml                    # Maven dependencies & build plugins
└── README.md                  # You are here!
```

---

## 🤝 Contributing

Contributions are welcome! If you have ideas for:
- Supporting standby / sleep tracking
- System tray minimization
- Multi-tier tariff slabs or currency selectors
- Dark mode theme

Feel free to open an issue or submit a pull request!

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — free to use, modify, and distribute.
