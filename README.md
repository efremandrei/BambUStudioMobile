# Bambu Studio Mobile

Android control console for Bambu Lab printers. The app is structured to provide Bambu Studio style control from a phone while keeping the heavy and firmware-sensitive work in a local backend.

## Current state

This first commit creates the Android app shell and the full control-console UI:

- Printer fleet overview
- Live status and camera placeholder
- Print job actions
- Temperature, fan, speed, light, chamber, airduct, and bed controls
- AMS spool overview
- Queue and sliced-file workflow
- Slicer/profile panels
- Backend connection settings

The command surface is represented by typed actions in the Android code. The next implementation step is wiring those actions to either Bambuddy's API or a custom local backend that talks to printers over LAN/developer-mode MQTT, FTPS, and camera streams.

## Architecture

Android should not directly reproduce all Bambu Studio internals. Full desktop parity requires slicing engines, printer profiles, credentials, file transfer, queueing, camera proxying, and firmware-specific command handling. Those belong in a trusted LAN backend.

Recommended runtime:

- Android app: UI, file picker, live dashboard, remote controls, notifications.
- Backend service: printer discovery, MQTT/FTPS command execution, camera proxy, queue management, slicing orchestration.
- Optional Bambuddy bridge: reuse Bambuddy where it already provides printer control, queueing, virtual printers, archiving, and server-side slicing.

## Build

Open `BambuStudioMobile` in Android Studio or run:

```powershell
.\gradlew.bat assembleDebug
```

Note: the source upload excludes `gradle-wrapper.jar` because this connector session cannot reliably transmit that binary. Android Studio can open and build the project, and the wrapper jar can be regenerated with Gradle if needed.
