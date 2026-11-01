# Design Choices & Tradeoffs

These are the key design choices made while building PowerTrack and the reasoning behind them:

### 1. Using SQLite Instead of a Big Database Server
Since PowerTrack is a personal desktop utility running on a single laptop, there is no need for a complex database server like MySQL or PostgreSQL. SQLite stores all data in a single local file (`powertrack.db`), requires zero configuration, and works offline out of the box.

### 2. Using JNA for Native Windows Calls
Instead of relying on external scripts (like Python or PowerShell wrappers) that might fail if dependencies are missing, the app uses JNA (Java Native Access). This lets Java call the Windows `Kernel32` system power API directly in memory, making it fast and self-contained.

### 3. Estimating Battery Charging vs Wall Socket Power
Standard laptops do not have built-in hardware sensors to measure wall-plug electricity draw. They only have sensors that report battery charge level and capacity. Rather than inventing fake numbers for total wall draw, PowerTrack explicitly measures and displays **estimated battery charging power**. When the battery reaches 100%, charging stops, so the app shows "N/A" instead of guessing.

### 4. 10-Second Polling Frequency
Checking system power status every 10 seconds is frequent enough to keep session tracking accurate, while using virtually 0% CPU.

### 5. Flat Electricity Rate
Different regions have complex slab systems for electricity billing. To keep the app simple and easy to understand, PowerTrack uses a single flat rate per unit (e.g. ₹/kWh) that the user can change anytime from the UI.
