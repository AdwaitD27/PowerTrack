# POWERTRACK - FINAL ANTIGRAVITY PROJECT PROMPT

You are the coding agent working inside an empty project folder.

Build the complete PowerTrack Windows desktop application directly in this workspace.

Do not merely provide code snippets or explanations. Create the actual project structure, source files, configuration, database layer, UI, documentation, and runnable application.

This specification contains decisions that have already been finalized. Follow them exactly and do not expand the scope unless explicitly instructed later.

---

# 1. DEVELOPMENT ENVIRONMENT

The development machine already has:

- Windows
- JDK 22
- Git 2.55.0 for Windows

Use **JDK 22** as the project's Java version.

Do NOT downgrade the project to JDK 17.

Configure Maven/compiler settings appropriately for Java 22.

Use modern, stable dependencies compatible with JDK 22.

Git is already installed and available.

---

# 2. PROJECT IDEA / MOTIVATION

PowerTrack is a lightweight Windows desktop utility for laptop users.

Laptop users often keep their laptops connected to AC power for long periods while studying, working, gaming, or developing software.

They can see battery percentage and whether the charger is connected, but they generally do not have a simple way to know:

- how long the laptop has remained connected to AC power
- approximately how much energy the laptop has consumed
- approximately how much that usage contributes to electricity cost
- how much this has accumulated during the current month

PowerTrack solves this with a small local Windows application.

The application automatically observes available Windows/system and battery information and uses it to estimate laptop power consumption.

The application must clearly distinguish between **estimated** and **measured** values.

PowerTrack is NOT a physical electricity meter.

---

# 3. PROBLEM STATEMENT

Laptop users lack a lightweight software utility that tracks their laptop's AC-powered sessions and estimates the corresponding energy consumption and electricity cost.

Existing system information provides battery and power-state information, but that information is not presented as a simple monthly laptop electricity tracker.

PowerTrack combines:

- AC connection status
- AC session duration
- automatic power estimation
- estimated energy
- estimated cost
- monthly tracking

into one focused desktop utility.

The entire solution is software-only.

No smart plug, external power meter, Arduino, IoT device, or other hardware is required.

---

# 4. FINAL V1 FEATURES

These are the ONLY core features.

## 1. AC connected/disconnected status

Automatically detect whether the laptop is currently connected to AC power.

Display clearly:

POWER CONNECTED

or:

POWER DISCONNECTED

The user must not manually enter this state.

---

## 2. Current AC session duration

When AC power becomes connected:

- start a session
- record the session start time
- track its duration

When AC power becomes disconnected:

- end the session
- calculate its duration
- persist it

Example:

Current Session  
02h 17m

If the application starts while AC is already connected, correctly recognize the existing state.

---

## 3. Automatic estimated power consumption

This is a CORE feature.

Do NOT ask the user to enter laptop wattage.

Do NOT use a fixed user-entered value such as 70 W.

Do NOT assume the charger's rating is the laptop's actual consumption.

For example:

A 200 W charger does NOT mean the laptop continuously consumes 200 W.

Instead, investigate available Windows/system/battery information such as:

- AC state
- battery percentage
- battery capacity
- full charge capacity where available
- charging state
- charge/discharge rate where available
- battery energy changes over time
- other reliable Windows power information

Use automatic estimation.

The displayed value must be labelled:

Estimated Power

not:

Measured Power

Round the displayed value sensibly.

Do not show fake precision such as:

73.428917 W

Prefer:

73 W

---

# 5. ESTIMATED ENERGY + COST

PowerTrack must calculate estimated energy.

Basic relationship:

Energy (Wh) = Power (W) × Time (hours)

Energy (kWh) = Energy (Wh) / 1000

Cost:

Estimated Cost = Estimated Energy (kWh) × Electricity Rate

The electricity rate is configurable.

The result represents:

Estimated laptop electricity cost

It does NOT represent the user's complete household electricity bill.

---

# 6. MONTHLY TRACKING / RESET

Track usage for the current month.

Display:

Estimated Energy  
XX.XX kWh

Estimated Cost  
₹XX.XX

When a new month begins, the current month's aggregation naturally starts fresh.

IMPORTANT:

Do NOT delete previous session records.

Historical sessions remain stored in SQLite.

Use timestamps to determine which sessions belong to the current month.

Do not create a separate monthly table unless there is a strong technical reason.

Prefer SQL aggregation over persisted session data.

---

# 7. MINIMAL BACKGROUND MONITORING

PowerTrack should be lightweight.

It must monitor the power state while running without creating unnecessary CPU usage.

Prefer:

- Windows power events/notifications where practical
- otherwise sensible low-frequency polling

Do NOT create an aggressive high-frequency loop.

Monitoring must not freeze the GUI.

Use appropriate Java background/concurrency mechanisms.

---

# 8. MINIMAL NECESSARY SETTING

The only necessary setting is:

Electricity Rate (₹/kWh)

Do NOT add:

- themes
- accounts
- cloud settings
- notification preferences
- advanced analytics settings
- gaming settings
- AI settings
- unnecessary customization

Keep settings minimal.

---

# 9. EXPLICITLY EXCLUDED FEATURE: NOTIFICATIONS

There must be NO AC connection notification.

Do NOT implement Windows toast notifications such as:

Power Connected!

Do NOT create notification settings.

AC state is displayed only inside the application.

---

# 10. TECHNOLOGY STACK

Use:

- Java 22
- Maven
- SQLite
- JDBC
- JavaFX for the desktop GUI, if it integrates cleanly with Java 22

Java is the primary and main application language.

---

# 11. WHY JAVA

Java is intentionally being used because this project should demonstrate practical understanding of:

- Java
- OOP
- JDBC
- SQL
- DBMS
- Windows/system API integration
- background processing
- desktop application architecture
- persistence
- software engineering

Do not replace Java with Python.

---

# 12. PYTHON POLICY

Python is NOT part of the default architecture.

Only introduce Python if a specific required Windows/system capability is genuinely impractical to implement reliably in Java.

Before introducing Python:

1. Investigate Java/JNA/WMI/Windows APIs.
2. Prefer Java if reasonably practical.
3. Only use Python if genuinely necessary.
4. Isolate it to the smallest possible component.
5. Document why it was required.

Do not create a Java + Python hybrid merely for convenience.

Preferred architecture:

Java Application  
↓  
Windows/System APIs  
↓  
SQLite via JDBC  
↓  
JavaFX GUI

---

# 13. WINDOWS POWER INTEGRATION

PowerTrack is a Windows application.

Investigate reliable Windows mechanisms for obtaining:

- AC connection state
- battery percentage
- charging state
- battery capacity
- full charge capacity
- charge/discharge rate where supported
- other useful battery/power information

Potential approaches include:

- Windows APIs
- JNA
- WMI
- other appropriate Java-accessible Windows interfaces

Do NOT invent APIs.

Do NOT assume every laptop exposes every metric.

Handle unavailable information safely.

Document what information is actually available.

---

# 14. POWER ESTIMATION

Power estimation is the most technically important part of this project.

Do NOT calculate:

200 W charger × plugged-in duration

as the default model.

Charger rating is not equivalent to actual laptop consumption.

Instead, use observable system/battery information.

Where charge/discharge power is available, use it appropriately.

Where only battery capacity and percentage changes are available, use those values as inputs to a reasonable estimation model.

If a metric is unavailable:

- use a sensible fallback
- do not crash
- do not fabricate precision
- clearly document the limitation

Keep all estimation logic inside:

PowerEstimator

This allows the estimation algorithm to be improved independently later.

---

# 15. IMPORTANT ESTIMATION LIMITATION

Battery-side power is not automatically identical to wall-socket input power.

There can be:

- charging losses
- adapter losses
- battery conversion losses
- system power consumption
- hardware-specific differences

Therefore PowerTrack must clearly state that the result is an estimate.

Never claim that software universally measures exact wall-socket power.

Use:

Estimated Power  
Estimated Energy  
Estimated Cost

Do not claim:

Exact Power  
Exact Energy

unless actual measurement hardware exists.

---

# 16. OOP ARCHITECTURE

Do not put the entire application into Main.java.

Use meaningful separation of responsibilities.

Recommended classes:

Main  
PowerMonitor  
SessionManager  
PowerEstimator  
DatabaseManager  
SettingsManager  
Dashboard

Small supporting classes/interfaces are allowed when genuinely useful.

Do not create dozens of unnecessary classes.

---

# 17. CLASS RESPONSIBILITIES

## Main

Application entry point.

Starts the application and wires major components.

No major business logic here.

## PowerMonitor

Responsible for obtaining Windows power information:

- AC state
- battery percentage
- charging state
- available battery metrics
- supported power information

No database logic.

## SessionManager

Responsible for:

- starting sessions
- ending sessions
- tracking current session
- calculating duration
- coordinating persistence

## PowerEstimator

Responsible for:

- estimated watts
- estimated energy
- estimation methodology
- fallbacks
- handling unavailable data

## DatabaseManager

Responsible for SQLite/JDBC operations:

- database initialization
- table creation
- inserts
- updates where needed
- retrieval
- monthly aggregation

## SettingsManager

Responsible for:

- electricity rate
- loading settings
- saving settings
- default value

## Dashboard

Responsible for displaying application state.

It should not contain:

- Windows API logic
- SQL queries
- complex estimation logic

---

# 18. DATABASE

Use SQLite.

Suggested table:

power_sessions

Fields:

- id
- start_time
- end_time
- duration_minutes
- estimated_watts
- energy_wh
- estimated_cost

Settings table:

settings

Fields:

- key
- value

Do NOT create:

tariff_slabs

There is no tariff-slab functionality.

Do not create unnecessary tables.

---

# 19. WHY SQLITE

SQLite is appropriate because PowerTrack is:

- local
- single-user
- desktop-based
- offline
- lightweight

There is no need for:

- MySQL server
- database credentials
- networking
- external database installation

Store the database in an appropriate Windows application-data location.

The application must create the database/tables automatically on first run.

---

# 20. JDBC REQUIREMENTS

Use JDBC for SQLite.

Demonstrate:

- Connection
- PreparedStatement
- ResultSet
- SELECT
- INSERT
- UPDATE where required
- SQL aggregation
- proper resource handling
- try-with-resources

Use parameterized SQL.

---

# 21. SESSION MODEL

An AC session means:

AC connected  
↓  
Session starts  
↓  
AC remains connected  
↓  
Session continues  
↓  
AC disconnected  
↓  
Session ends

Persist:

- start time
- end time
- duration
- estimated power/energy
- estimated cost

If PowerTrack starts while AC is already connected, handle that correctly.

---

# 22. APPLICATION STARTUP

Startup flow:

Application starts  
↓  
Initialize SQLite  
↓  
Load electricity rate  
↓  
Initialize PowerMonitor  
↓  
Read current AC state  
↓  
Determine current session state  
↓  
Initialize PowerEstimator  
↓  
Start background monitoring  
↓  
Launch Dashboard

The dashboard must correctly reflect the current AC state immediately.

---

# 23. APPLICATION SHUTDOWN

When the user closes PowerTrack:

- stop background monitoring
- handle the current session safely
- close database resources
- avoid database corruption

The application should preserve enough information to recover sensibly after restart.

Do not over-engineer crash recovery.

---

# 24. RESTART / UNFINISHED SESSION

Consider:

PowerTrack running  
↓  
AC connected  
↓  
Application closes unexpectedly  
↓  
Laptop remains AC connected  
↓  
PowerTrack starts again

Use persisted information and the current AC state to determine a sensible session state.

Keep this logic understandable.

---

# 25. MONTHLY CALCULATION

Monthly values should be based on stored session timestamps.

For the current month, calculate:

SUM(energy)

and:

SUM(cost)

using appropriate SQL filtering.

Previous session data must remain available.

No unnecessary monthly table.

---

# 26. USER INTERFACE

Keep the UI minimal and clean.

Conceptually:

POWER  
CONNECTED

Battery  
82%

Current Session  
02h 17m

Estimated Power  
73 W

THIS MONTH

Estimated Energy  
8.42 kWh

Estimated Cost  
₹84.20

Settings should provide the electricity-rate setting.

Do not create:

- charts
- graphs
- analytics pages
- AI assistant
- cloud
- login
- social features
- gaming mode
- CPU/GPU dashboard
- battery-health dashboard
- benchmarking
- unrelated metrics

---

# 27. MINIMALISM

PowerTrack is intentionally a focused utility.

The goal is:

Small Windows utility  
+  
Automatic power tracking  
+  
Reasonable estimation  
+  
Monthly energy/cost tracking

It is NOT a complete laptop monitoring suite.

Do not add features simply because they sound impressive.

---

# 28. BACKGROUND MONITORING

The monitoring system must not freeze the JavaFX UI.

Use an appropriate Java background execution mechanism.

Prefer event-driven Windows power changes where practical.

Otherwise use sensible low-frequency polling.

Document the chosen approach.

---

# 29. PROJECT STRUCTURE

Create a clean Maven project.

Suggested:

PowerTrack/
│
├── pom.xml
├── README.md
├── PROJECT_SPEC.md
├── ARCHITECTURE.md
├── PROGRESS.md
├── DECISIONS.md
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── powertrack/
        │           ├── Main.java
        │           ├── monitor/
        │           │   └── PowerMonitor.java
        │           ├── session/
        │           │   └── SessionManager.java
        │           ├── estimation/
        │           │   └── PowerEstimator.java
        │           ├── database/
        │           │   └── DatabaseManager.java
        │           ├── settings/
        │           │   └── SettingsManager.java
        │           └── ui/
        │               └── Dashboard.java
        │
        └── resources/

You may modify the exact structure if technically justified, but keep it simple.

---

# 30. DOCUMENTATION

Create:

## README.md

Include:

- project overview
- motivation
- problem statement
- features
- technology stack
- setup
- build instructions
- run instructions
- database location
- estimation methodology
- limitations

## PROJECT_SPEC.md

Store this specification.

## ARCHITECTURE.md

Explain:

- architecture
- classes
- responsibilities
- data flow
- monitoring flow
- session flow
- estimation flow
- database flow
- startup
- shutdown

## PROGRESS.md

Maintain:

Completed  
Current  
Next  
Known Issues

This is important for continuing development in future Antigravity sessions.

## DECISIONS.md

Document important decisions and tradeoffs:

- Java 22
- SQLite
- JDBC
- no MySQL
- no Python by default
- no tariff slabs
- no fixed wattage
- no charger-rating calculation
- no external hardware
- estimated rather than measured power
- minimal monitoring
- no notifications
- monthly aggregation

---

# 31. INTERVIEW / LEARNING OBJECTIVE

This project should provide meaningful SDE interview discussion.

The implementation should demonstrate understanding of:

## OOP

- encapsulation
- abstraction
- composition
- separation of responsibilities
- interfaces where genuinely useful

Do not force inheritance just for the sake of showing inheritance.

## DBMS

- relational structure
- primary key
- timestamps
- aggregation
- persistence

## JDBC

- Connection
- PreparedStatement
- ResultSet
- SQL queries
- resource management

## OS / System Interaction

- Java communicating with Windows
- AC state detection
- battery information
- background monitoring

## Software Engineering

- modular architecture
- separation of concerns
- error handling
- persistence
- maintainability
- documentation

---

# 32. CODE QUALITY

Code must be:

- readable
- modular
- understandable
- reasonably concise
- properly named

Avoid:

- giant classes
- giant methods
- unnecessary design patterns
- excessive abstraction
- enterprise architecture
- unnecessary interfaces
- unnecessary dependencies
- meaningless boilerplate

The student must be able to understand and explain the entire project.

---

# 33. DEPENDENCIES

Only use dependencies that are genuinely required.

Likely:

- SQLite JDBC driver
- JNA if required for Windows API access
- JavaFX dependencies

All dependencies must be compatible with Java 22.

Do not add random libraries.

---

# 34. TESTING

Before declaring the project complete:

1. Build the Maven project.
2. Verify Java 22 compilation.
3. Verify SQLite initialization.
4. Verify application launch.
5. Verify dashboard.
6. Verify AC state detection.
7. Verify session tracking.
8. Verify database persistence.
9. Verify monthly aggregation.
10. Verify electricity-rate setting.
11. Verify background monitoring.
12. Verify graceful shutdown.

If Windows-specific functionality cannot be tested in the current environment, clearly state that.

Never claim something was tested if it was not.

---

# 35. TECHNICAL UNCERTAINTY RULE

Automatic power estimation is the most technically uncertain part.

If a Windows API or laptop hardware does not expose a required metric:

- identify the limitation
- use the best available observable data
- implement a reasonable fallback
- clearly label the result as estimated
- document the limitation

Do not fabricate measurements.

Do not invent unsupported APIs.

Do not create fake precision.

Keep PowerEstimator isolated so it can be improved later.

---

# 36. ABSOLUTELY OUT OF SCOPE

Do NOT implement:

- AC connection notifications
- Windows toast notifications
- notification settings
- household electricity bill prediction
- electricity tariff slabs
- tariff slab tables
- cloud
- login
- accounts
- online synchronization
- mobile application
- web application
- external hardware
- smart plug
- IoT hardware
- AI assistant
- unnecessary charts
- unnecessary analytics
- gaming mode
- CPU monitoring dashboard
- GPU monitoring dashboard
- battery health dashboard
- benchmarking
- user-entered wattage as the primary estimation method
- large settings system
- unrelated features

---

# 37. FINAL V1 ACCEPTANCE CRITERIA

PowerTrack V1 is complete when:

- Maven project builds successfully.
- Java 22 application launches.
- Desktop dashboard opens.
- AC connected/disconnected status works.
- Current AC session duration works.
- Automatic power estimation works.
- Estimated energy calculation works.
- Estimated cost calculation works.
- Monthly tracking works.
- Previous session data remains persisted.
- SQLite database works.
- Electricity rate can be configured.
- Background monitoring is lightweight.
- Application shuts down cleanly.
- No AC notification exists.
- No tariff slab logic exists.
- No household bill prediction exists.
- No external hardware is required.
- Estimation limitations are documented.
- Architecture documentation exists.
- Project structure is clean.
- Code is understandable.

---

# 38. ANTIGRAVITY EXECUTION INSTRUCTION

Create the entire project directly inside the currently opened workspace.

Do NOT ask me to manually create Java files.

Do NOT dump the project as a giant code block into chat.

Create the actual files.

Use the existing:

- JDK 22
- Git 2.55.0

Do not downgrade Java.

After implementation:

1. Build the project.
2. Fix compilation/build issues.
3. Run whatever tests are possible.
4. Verify the application launch where possible.
5. Report actual results.

If something cannot be tested, explicitly say so.

Do not fake successful testing.

---

# 39. AFTER IMPLEMENTATION

Once the project has been created, DO NOT immediately add more features.

Give me a concise implementation report containing:

1. Files created
2. Architecture
3. Dependencies
4. How to run
5. How Windows power information is obtained
6. How AC session tracking works
7. How power estimation works
8. How energy and cost are calculated
9. How SQLite/JDBC works
10. Known limitations
11. What was actually tested
12. Anything that could not be tested

Then STOP.
Do not modify the scope.
The next stage will be understanding the generated implementation before making further changes.
