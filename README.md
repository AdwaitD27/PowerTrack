# PowerTrack

A simple Windows desktop tool to track laptop charging sessions, estimate power consumption, and see how much electricity your laptop uses each month.

---

## Why I Built This

I spend long hours on my laptop working and studying with the charger plugged in. One day I wondered: *how much electricity is my laptop actually consuming, and what does it add to my electricity bill at the end of the month?*

Laptops show you battery percentage, but they don't give you any idea of your power usage or cost. PowerTrack solves this. It runs quietly on your machine, senses when you plug in, and calculates your charging sessions and monthly costs.

Everything stays on your computer in a local database. No accounts, no internet required, and no extra hardware.

---

## What It Does

- **Automatic session tracking:** Detects the moment you plug into power and starts counting. When you unplug, it automatically saves the session.
- **Power estimation:** Calculates roughly how much power (in Watts) your battery is drawing while charging, based on your battery's actual capacity and percentage change over time.
- **Monthly cost summary:** Adds up your total charging energy (in kWh) for the current month and calculates the bill based on your local electricity rate.
- **Custom electricity rate:** You can set your own rate (e.g. ₹7/kWh) right on the screen, and it remembers it for future sessions.
- **Lightweight & local:** Saves all data into a local SQLite file (`powertrack.db`) and checks your power status every 10 seconds so it won't slow down your computer.

---

## How It Works (The Basic Idea)

The logic behind the app is straightforward:

1. **Checking connection:** The app uses Windows system APIs to check if AC power is connected.
2. **Reading battery capacity:** On startup, it reads the laptop's battery full-charge capacity (in Watt-hours) using the built-in Windows battery report.
3. **Calculating power:** While charging, it looks at how much the battery percentage went up over time:
   - For example: if a 50 Wh battery charges 10% in 15 minutes:
     - Energy gained = $10\% \times 50\text{ Wh} = 5\text{ Wh}$
     - Charging rate = $5\text{ Wh} \div 0.25\text{ hours} = 20\text{ Watts}$
4. **Calculating cost:** Converts Watt-hours to kilowatt-hours (kWh) and multiplies by your saved rate per unit.
5. **Saving history:** When you unplug the charger, the session duration, energy, and cost are saved to SQLite.

*Note: This app calculates battery charging power based on software metrics. It is an estimate and not a physical wall-plug meter (hardware like a Kill-A-Watt). When the battery is completely full at 100%, active charging power stops, so the app shows "N/A" instead of guessing fake numbers.*

---

## Tech Stack

- **Java 22** – Core programming language
- **JavaFX** – Desktop user interface
- **SQLite (JDBC)** – Lightweight local database
- **JNA (Java Native Access)** – Calls Windows power APIs directly from Java
- **Maven** – Build and dependency management

---

## How to Run It

### Requirements
- Windows 10 or 11
- JDK 22 or newer
- Maven

### Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/AdwaitD27/PowerTrack.git
   cd PowerTrack
   ```

2. **Run with Maven:**
   ```bash
   mvn clean javafx:run
   ```

3. **Or package into an executable JAR:**
   ```bash
   mvn clean package
   java -jar target/PowerTrack-1.0.0-SNAPSHOT.jar
   ```

---

## Project Structure

```text
PowerTrack/
├── src/main/java/com/powertrack/
│   ├── database/      # SQLite helper to save and read sessions
│   ├── estimation/    # Logic that estimates Watts and energy
│   ├── monitor/       # Checks if charger is plugged in via Windows API
│   ├── session/       # Starts and stops charging sessions
│   ├── settings/      # Saves user preferences (electricity rate)
│   ├── ui/            # JavaFX dashboard window
│   ├── Launcher.java  # Main entry point for the packaged JAR
│   └── Main.java      # JavaFX app starter
├── ARCHITECTURE.md    # High-level architecture overview
├── DECISIONS.md       # Design choices and tradeoffs
└── pom.xml            # Maven build configuration
```
