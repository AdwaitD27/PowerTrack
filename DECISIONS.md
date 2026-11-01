# Important Decisions & Tradeoffs

- **Java 22**: Chosen for modern features and as the primary implementation language as per spec.
- **SQLite via JDBC**: Ideal for local, single-user desktop apps. No complex client-server database (e.g., MySQL) was used.
- **Pure Java over Python**: System data is gathered via JNA (Java Native Access) mapping to Windows APIs and standard Java `ProcessBuilder` (for querying WMI if needed), avoiding adding Python as an external dependency.
- **No Tariff Slabs**: A flat electricity rate is used for simplicity, strictly adhering to the requirements.
- **Estimated Battery Charging Power vs Wall Power**: Exact total system wattage (wall power) isn't provided via standard WMI without driver-specific hacks. PowerTrack strictly estimates the *Battery Charging Power* by monitoring `BatteryLifePercent` changes from `GetSystemPowerStatus` and multiplying by `FullChargeCapacity` (obtained by generating and parsing a Windows `powercfg /batteryreport /xml` on startup) over elapsed time. If at 100% capacity (where charging stops) or if capacity is unavailable, the telemetry is explicitly marked as unavailable (-1/N/A) rather than outputting false precision (like 0W or a 50Wh fallback) for total system consumption.
- **Minimal Monitoring**: A background polling thread runs every 10 seconds. This is low-frequency enough to not drain CPU while keeping tracking reasonably accurate.
- **No Notifications**: Windows Toast notifications were explicitly excluded from the design to maintain minimal intrusion.
- **Aggregation**: Monthly totals are calculated dynamically via SQL `SUM` aggregations over session timestamps rather than maintaining separate monthly tracking tables, keeping the schema simple and ensuring past data remains intact.
