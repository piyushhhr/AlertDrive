# AlertDrive – Product Test Results

## 1. Introduction

This document records the hardware, software, communication and integrated product validation status of the AlertDrive driver drowsiness detection and alert system.

AlertDrive was developed by Engineering Product Design (EPD) Group 8.

## 2. Product Information

| Parameter | Description |
|---|---|
| Product name | AlertDrive |
| Product version | v1.0.0 |
| Microcontroller | ESP32-S3 N16R8 |
| Camera | GC2145 |
| Application platform | Android |
| Firmware language | Arduino C/C++ |
| Application language | Kotlin |
| Communication | Local Wi-Fi and HTTP |
| Buzzer switching | C2N7000 MOSFET |
| Motor switching | XY-GMOS MOSFET module |
| Product status | Working prototype |

## 3. Hardware Validation Results

| Component | Expected Operation | Validation Status |
|---|---|---|
| ESP32-S3 | Successful initialization and embedded control | Operational |
| GC2145 camera | Camera initialization and image acquisition | Operational |
| LED | Visual warning generation | Operational |
| 5V buzzer | Audible warning generation | Operational |
| C2N7000 MOSFET | Buzzer switching | Operational |
| Vibration motors | Tactile warning generation | Operational |
| XY-GMOS module | Vibration motor switching | Operational |
| Integrated hardware | Combined operation of hardware components | Completed |

The hardware subsystem has been validated as part of the working integrated prototype, as reported by the development team.

## 4. Camera Validation

| Function | Expected Result |
|---|---|
| Camera initialization | GC2145 initializes successfully |
| Frame acquisition | Camera captures facial images |
| Image conversion | RGB565 frames are converted into JPEG |
| Image transmission | Frames are transmitted to the Android application |
| Continuous monitoring | Repeated camera frame acquisition |

Overall camera integration status: Operational.

Individual frame processing times, measured frame rates and image transmission latency have not been recorded in this document.

## 5. Android Application Validation

| Function | Expected Result |
|---|---|
| APK installation | Application installs successfully |
| Application startup | Application launches |
| ESP32 connectivity | Application communicates with the embedded hardware |
| Camera frame reception | Application receives camera images |
| Facial feature processing | Application processes facial features |
| Drowsiness detection | Application identifies configured monitoring states |
| Alert communication | Application sends alert commands to ESP32 |
| Monitoring control | Application supports starting and stopping monitoring |

Overall Android application status: Operational.

Detailed device compatibility and individual application performance measurements are not recorded here.

## 6. Network and Communication Validation

| Function | Expected Result |
|---|---|
| Local Wi-Fi access point | ALERT-DRIVE network becomes available |
| Android connection | Smartphone connects to ESP32 |
| HTTP communication | Device accepts HTTP requests |
| Camera transmission | JPEG frames are transferred |
| Alert command reception | Valid alert commands are processed |
| Communication watchdog | Communication interruptions are detected |
| Monitoring stop command | System returns to standby |

Overall communication subsystem status: Operational.

The firmware watchdog timeout is configured to 5000 ms. This is a programmed value, not an independently measured response time.

## 7. Alert State Reference

The following table describes the expected behaviour implemented in the integrated firmware.

| State | LED | Buzzer | Vibration Motors |
|---|---|---|---|
| STANDBY | OFF | OFF | OFF |
| NORMAL | OFF | OFF | OFF |
| CLOSING | Blinking | OFF | OFF |
| WARNING | ON | Pulsing | OFF |
| DROWSY | ON | Pulsing | Pulsing |
| NO_FACE | Slow flashing | OFF | OFF |
| CONNECTION_LOST | Rapid blinking | Periodic beep | OFF |

The integrated hardware alert subsystem is reported to be working.

Individual state-by-state observations should be retained with the original product testing records where available.

## 8. Configured Alert Timing

| State | Component | Configured Timing |
|---|---|---|
| CLOSING | LED | 500 ms ON, 500 ms OFF |
| WARNING | Buzzer | 500 ms ON, 500 ms OFF |
| DROWSY | Buzzer and motors | 300 ms ON, 300 ms OFF |
| NO_FACE | LED | 250 ms ON, 1750 ms OFF |
| CONNECTION_LOST | LED | 100 ms ON, 100 ms OFF |
| CONNECTION_LOST | Buzzer | 200 ms ON, 2800 ms OFF |
| Communication watchdog | Timeout | 5000 ms |

These values are taken from the recovered integrated firmware and represent configured behaviour rather than measured timing results.

## 9. Individual Component Test Records

| Test | GPIO | Recorded Status |
|---|---|---|
| LED operation | GPIO38 | Previously tested successfully |
| Alternative LED operation | GPIO21 | Previously tested successfully |
| Buzzer operation | GPIO14 | Operational in integrated prototype |
| Vibration motor operation | GPIO47 | Operational in integrated prototype |
| Camera integration | Dedicated GPIOs | Operational in integrated prototype |
| Communication integration | Local Wi-Fi | Operational in integrated prototype |

The original individual testing sketches were not retained.

Reconstructed test sketches are provided in the firmware/tests directory for future troubleshooting and reproducibility.

Their independent compilation and execution results have not been recorded.

## 10. Integrated Product Validation

| Subsystem | Validation Status |
|---|---|
| Embedded hardware integration | Completed |
| Camera integration | Completed |
| Android application integration | Completed |
| Local communication integration | Completed |
| Hardware alert integration | Completed |
| Final hardware validation | Completed |
| Product testing | Completed |
| Integrated prototype operation | Working |

These overall validation statuses are based on the development team's confirmation of successful prototype operation.

## 11. Test Evidence and Traceability

The complete set of proposed product test cases is available in:

[Product Test Cases](test_cases.md)

Individual firmware test programs are available in:

[Firmware Testing](../firmware/tests/)

The integrated firmware is available in:

[AlertDrive Firmware](../firmware/AlertDrive/AlertDrive.ino)

The Android application is available through:

[AlertDrive Releases](https://github.com/piyushhhr/AlertDrive/releases)

## 12. Performance Measurements

| Parameter | Recorded Result |
|---|---|
| Drowsiness detection accuracy | Not recorded |
| False-positive rate | Not recorded |
| False-negative rate | Not recorded |
| Camera frame rate | Not recorded |
| Image processing latency | Not recorded |
| End-to-end alert response time | Not recorded |
| Continuous operation duration | Not recorded |

No numerical performance results have been estimated or fabricated.

## 13. Test Summary

| Category | Overall Status |
|---|---|
| Hardware validation | Completed |
| Software integration | Completed |
| Android and ESP32 communication | Completed |
| Hardware alert integration | Completed |
| Integrated product testing | Completed |
| Individual test-case evidence | Not fully recorded in this repository |
| Reconstructed test sketch validation | Not recorded |

**Overall Product Status: Working and Validated Academic Prototype**

## 14. Conclusion

The AlertDrive prototype integrates an ESP32-S3 microcontroller, GC2145 camera, Android-based drowsiness detection and visual, audible and tactile alert mechanisms.

The development team reports successful completion of final hardware validation and integrated product testing.

The system demonstrates the intended functionality of a camera-based driver drowsiness detection and alert system using local communication between embedded hardware and an Android application.

Further quantitative evaluation is required to establish detection accuracy, reliability and performance under different operating conditions.

## 15. Disclaimer

AlertDrive is an academic engineering prototype developed for educational and demonstration purposes.

Successful prototype validation does not constitute automotive safety certification.