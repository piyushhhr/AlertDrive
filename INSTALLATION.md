# AlertDrive – Installation and Setup Guide

## 1. Introduction

This guide provides instructions for installing, configuring and operating the AlertDrive driver drowsiness detection and alert system.

AlertDrive consists of an ESP32-S3 embedded hardware unit and an Android application communicating through a dedicated local Wi-Fi network.

Internet connectivity, an external Wi-Fi router and a laptop are not required during normal operation.

## 2. System Requirements

### Hardware Requirements

- ESP32-S3 N16R8 microcontroller
- GC2145 camera module
- LED with current-limiting resistor
- 5V buzzer
- 3V coin vibration motors
- C2N7000 N-channel MOSFET
- XY-GMOS MOSFET module
- Protection components
- Connecting wires
- Android smartphone
- USB cable for firmware programming

### Software Requirements

| Software | Purpose |
|---|---|
| Arduino IDE | ESP32 firmware programming |
| ESP32 Arduino board package | ESP32-S3 development support |
| Android Studio | Android application development |
| Git | Repository management |
| VS Code | Optional source code and documentation editing |

Android Studio is not required if the prebuilt APK is used.

## 3. Download the Project

Clone the repository using Git:

```bash
git clone <repository-url>
cd AlertDrive
```

Alternatively, open the GitHub repository, select Code and download the project as a ZIP file.

Extract the ZIP file before proceeding.

## 4. Hardware Assembly

Connect the hardware components according to the AlertDrive GPIO configuration.

| Component | ESP32 GPIO | Switching Circuit |
|---|---|---|
| LED | GPIO38 | Current-limiting resistor |
| Buzzer | GPIO14 | C2N7000 MOSFET |
| Vibration motors | GPIO47 | XY-GMOS MOSFET module |
| GC2145 camera | Dedicated camera GPIOs | Camera interface |

Refer to the following documents before assembling the hardware:

- [Hardware Components](hardware/components.md)
- [GPIO Configuration](hardware/pin_configuration.md)
- [Wiring Guide](hardware/wiring_guide.md)
- [Circuit Diagrams](hardware/circuit_diagrams/)

Disconnect the hardware from power before modifying any electrical connections.

## 5. Arduino IDE Installation

Download Arduino IDE from:

https://www.arduino.cc/en/software

Install Arduino IDE and launch the application.

## 6. ESP32 Board Package Installation

1. Open Arduino IDE.
2. Navigate to File > Preferences.
3. Add the following URL to Additional Boards Manager URLs:

```
https://espressif.github.io/arduino-esp32/package_esp32_index.json
```

4. Open Tools > Board > Boards Manager.
5. Search for esp32.
6. Install the ESP32 board package by Espressif Systems.

## 7. ESP32 Firmware Configuration

Open the following Arduino sketch:

```
firmware/AlertDrive/AlertDrive.ino
```

Configure the Arduino IDE board settings.

| Setting | Configuration |
|---|---|
| Board | ESP32S3 Dev Module |
| Flash Size | 16 MB (128 Mb) |
| PSRAM | OPI PSRAM |
| Serial baud rate | 115200 |
| Port | Connected ESP32 serial port |

Other board settings may depend on the specific ESP32-S3 development board and installed Arduino core version.

### Firmware Libraries

The firmware uses the following libraries:

```cpp
#include <WiFi.h>
#include <WebServer.h>
#include "esp_camera.h"
#include "img_converters.h"
```

These libraries are provided through the Arduino ESP32 board support package.

## 8. Firmware Upload

1. Connect the ESP32-S3 to the computer using a USB data cable.
2. Select the appropriate serial port in Arduino IDE.
3. Verify the board configuration.
4. Click Verify to compile the firmware.
5. Resolve any compilation errors.
6. Click Upload.
7. Wait for the upload to finish.
8. Restart the ESP32 if required.

If the ESP32 already contains working AlertDrive firmware, uploading again is unnecessary.

## 9. Verify ESP32 Startup

Open the Arduino IDE Serial Monitor and configure the baud rate to 115200.

The firmware should initialize the camera, start the local Wi-Fi access point and launch the HTTP server.

Example successful startup messages:

```
AlertDrive Starting
Camera PID: 0x2145
GC2145 camera initialized successfully
AlertDrive WiFi started
SSID: ALERT-DRIVE
IP Address: 192.168.4.1
HTTP server started
Waiting for mobile application
AlertDrive is ready
```

The exact output may vary depending on the firmware version and serial connection configuration.

## 10. Android APK Installation

### Download the Application

[Download AlertDrive APK](https://github.com/piyushhhr/AlertDrive/releases/latest/download/AlertDrive.apk)

### Install the Application

1. Download the APK onto the Android smartphone.
2. Open the downloaded APK file.
3. If prompted, allow installation from the selected application source.
4. Select Install.
5. Open AlertDrive after installation.
6. Disable the temporary installation permission when no longer required.

The included APK is a prototype development build.

## 11. Connect the Android Smartphone

1. Switch on the AlertDrive hardware.
2. Open the smartphone Wi-Fi settings.
3. Select the ALERT-DRIVE network.
4. Enter the network password configured in the ESP32 firmware.
5. Remain connected even if Android indicates that the network has no internet access.
6. Open the AlertDrive application.

### Network Configuration

| Parameter | Value |
|---|---|
| Wi-Fi SSID | ALERT-DRIVE |
| ESP32 IP address | 192.168.4.1 |
| HTTP port | 80 |
| Internet connection | Not required |

The network credentials used by the application and firmware must remain consistent.

## 12. Verify Device Communication

While connected to the ALERT-DRIVE Wi-Fi network, open a web browser on the smartphone.

### Device Information

```
http://192.168.4.1/
```

Displays basic device information.

### Health Status

```
http://192.168.4.1/health
```

Displays camera availability, monitoring status and the current alert state.

### Camera Frame

```
http://192.168.4.1/frame
```

Returns a JPEG image captured by the GC2145 camera.

Successful responses indicate that the corresponding network and camera functions are available.

## 13. Start Driver Monitoring

1. Open the AlertDrive Android application.
2. Verify that the application is connected to the ESP32.
3. Position the camera so that the driver's face is clearly visible.
4. Ensure suitable lighting conditions.
5. Start monitoring using the application controls.
6. Observe the monitoring status and hardware alert behaviour.

The application retrieves camera frames, processes facial features and sends monitoring states to the ESP32.

## 14. Alert State Reference

| State | LED | Buzzer | Vibration Motors |
|---|---|---|---|
| STANDBY | OFF | OFF | OFF |
| NORMAL | OFF | OFF | OFF |
| CLOSING | Blinking | OFF | OFF |
| WARNING | ON | Pulsing | OFF |
| DROWSY | ON | Pulsing | Pulsing |
| NO_FACE | Slow flashing | OFF | OFF |
| CONNECTION_LOST | Rapid blinking | Periodic beep | OFF |

The ESP32 firmware uses a five-second communication watchdog during active monitoring.

## 15. Stop Driver Monitoring

1. Select the stop monitoring control in the Android application.
2. Verify that the application enters standby mode.
3. Confirm that the LED, buzzer and vibration motors are switched off.
4. Disconnect from the AlertDrive Wi-Fi network if required.
5. Switch off the hardware when the system is no longer needed.

## 16. Build the Android Application from Source

Download Android Studio from:

https://developer.android.com/studio

### Open the Project

1. Launch Android Studio.
2. Select Open.
3. Navigate to the following directory:

```
AlertDrive/app/source/
```

4. Open the directory as an Android Studio project.
5. Allow Gradle synchronization to complete.
6. Install any required Android SDK components.

### Build Using Android Studio

Use the Android Studio build menu to generate an APK.

The exact menu labels may vary depending on the Android Studio version.

### Build Using Git Bash

From the repository root, execute:

```bash
cd app/source
./gradlew assembleDebug
```

The Android SDK and compatible Java environment must be configured before building from the terminal.

The generated APK is normally available at:

```
app/source/app/build/outputs/apk/debug/app-debug.apk
```

The APK can be copied into the distribution folder and renamed to AlertDrive.apk.

## 17. Troubleshooting

| Problem | Possible Cause | Recommended Action |
|---|---|---|
| ESP32 is not detected | USB cable or driver issue | Verify cable and serial port |
| Firmware compilation fails | Incorrect board package or configuration | Verify ESP32 Arduino support |
| Camera initialization fails | Camera connection or configuration issue | Check ribbon cable, pin mapping and PSRAM |
| Wi-Fi network is unavailable | ESP32 startup failure | Restart ESP32 and check serial output |
| Smartphone cannot connect | Incorrect network credentials | Verify the configured password |
| Wi-Fi reports no internet | Expected local network behaviour | Remain connected to ALERT-DRIVE |
| Application cannot retrieve frames | Camera or network failure | Check the health and frame endpoints |
| LED does not operate | Loose wiring or incorrect polarity | Verify GPIO38 and LED connections |
| Buzzer does not operate | Switching circuit issue | Verify GPIO14 and C2N7000 connections |
| Motors do not operate | Motor supply or switching issue | Verify GPIO47 and XY-GMOS connections |
| Connection-lost warning activates | Interrupted communication | Check Wi-Fi and application monitoring |
| APK installation fails | Android compatibility or signature conflict | Verify the APK and installed application version |

## 18. Validation and Testing

The AlertDrive development team has completed integrated hardware validation and product testing of the working prototype.

After installing the system on another device, verify the following before use:

- ESP32 startup.
- Camera initialization.
- Local Wi-Fi connectivity.
- Android application communication.
- Camera frame reception.
- Drowsiness detection functionality.
- LED operation.
- Buzzer operation.
- Vibration motor operation.
- Communication-loss warning.
- Start and stop monitoring.

Perform installation verification and testing in a stationary environment.

## 19. Related Documentation

- [Project README](README.md)
- [Firmware Documentation](firmware/README.md)
- [Hardware Components](hardware/components.md)
- [GPIO Configuration](hardware/pin_configuration.md)
- [Wiring Guide](hardware/wiring_guide.md)
- [Circuit Diagrams](hardware/circuit_diagrams/)
- [EPD Project Documents](docs/)

## 20. Disclaimer

AlertDrive is an academic engineering prototype developed for educational and demonstration purposes.

It is not a certified automotive safety system and must not be used as a substitute for attentive driving, adequate rest or certified driver monitoring equipment.

---

Developed by Engineering Product Design – Group 8.