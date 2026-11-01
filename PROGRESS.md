# Progress

## Completed
- Project structure initialization.
- Maven POM setup for Java 22, JavaFX, SQLite, JNA.
- Initial architecture and documentation creation.

## Current
- Addressed all V1 acceptance criteria and telemetry feedback.
- Completed full integration of precise Battery Charging Power estimation without hardcoded assumptions.

## Next
- V1 is functionally complete. Wait for further project roadmap from user.

## Known Issues
- System AC power consumption (wall power) cannot be reliably isolated from Battery Charging Power via standard Windows APIs; the application therefore accurately labels and provides only Battery Charging estimations.
