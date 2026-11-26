# AlertDrive – Product Test Cases

## 1. Introduction

This document defines the functional, hardware, software, communication and integration test cases for the AlertDrive driver drowsiness detection and alert system.

## 2. Testing Objectives

- Verify individual hardware component functionality.
- Verify GC2145 camera operation.
- Verify Android application functionality.
- Verify local Wi-Fi and HTTP communication.
- Verify drowsiness detection and alert generation.
- Verify communication-loss detection.
- Verify integrated product operation.

## 3. Testing Environment

| Parameter | Specification |
|---|---|
| Microcontroller | ESP32-S3 N16R8 |
| Camera | GC2145 |
| Android application | AlertDrive |
| Firmware | Arduino C/C++ |
| Connectivity | Local Wi-Fi |
| Communication protocol | HTTP |
| Buzzer switching | C2N7000 MOSFET |
| Motor switching | XY-GMOS MOSFET module |
| Test environment | Stationary prototype testing |

## 4. Hardware Test Cases

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| HW-01 | ESP32 startup | Switch on the embedded hardware | ESP32 initializes successfully |
| HW-02 | Camera initialization | Start the integrated firmware | GC2145 initializes without errors |
| HW-03 | LED operation | Activate GPIO38 | LED illuminates |
| HW-04 | Buzzer operation | Activate GPIO14 through C2N7000 | Buzzer produces an audible alert |
| HW-05 | Vibration motor operation | Activate GPIO47 through XY-GMOS | Vibration motors operate |
| HW-06 | Output deactivation | Set the system to STANDBY | All alert components switch off |
| HW-07 | Hardware integration | Activate the DROWSY state | LED, buzzer and motors operate according to the configured pattern |

## 5. Camera Test Cases

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| CAM-01 | Camera detection | Initialize the camera driver | GC2145 is detected |
| CAM-02 | PSRAM availability | Check PSRAM during initialization | PSRAM is available |
| CAM-03 | Frame capture | Request a camera frame | Image is captured successfully |
| CAM-04 | Image resolution | Inspect captured frame dimensions | Resolution is 640 × 480 |
| CAM-05 | JPEG conversion | Convert RGB565 frame into JPEG | Valid JPEG image is generated |
| CAM-06 | Frame transmission | Access the /frame endpoint | JPEG image is received |
| CAM-07 | Continuous capture | Request successive camera frames | Repeated image capture succeeds |

## 6. Android Application Test Cases

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| APP-01 | APK installation | Install AlertDrive.apk | Application installs successfully |
| APP-02 | Application startup | Launch AlertDrive | Application opens without crashing |
| APP-03 | Device connection | Connect to the ALERT-DRIVE network | Application communicates with ESP32 |
| APP-04 | Camera reception | Start camera monitoring | Application receives camera frames |
| APP-05 | Face detection | Position a face in view of the camera | Facial features are detected |
| APP-06 | Start monitoring | Activate monitoring | Driver monitoring begins |
| APP-07 | Stop monitoring | Select the stop control | Monitoring stops and alerts deactivate |
| APP-08 | Status display | Change the detected monitoring state | Application displays the corresponding state |
| APP-09 | Offline operation | Disable internet access while retaining local Wi-Fi | Application continues local operation |

## 7. Network and Communication Test Cases

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| NET-01 | Wi-Fi access point | Switch on ESP32 | ALERT-DRIVE network becomes available |
| NET-02 | Wi-Fi connection | Connect the Android smartphone | Local Wi-Fi connection succeeds |
| NET-03 | Device information | Send GET / | Device information is returned |
| NET-04 | Health endpoint | Send GET /health with camera initialized | System status is returned |
| NET-05 | Frame endpoint | Send GET /frame | JPEG image is returned |
| NET-06 | Valid alert command | Send a valid monitoring state | HTTP 200 response is returned |
| NET-07 | Invalid alert command | Send an unsupported monitoring state | HTTP 400 response is returned |
| NET-08 | Missing state parameter | Send GET /alert without a state | HTTP 400 response is returned |
| NET-09 | Unknown endpoint | Request an unregistered endpoint | HTTP 404 response is returned |
| NET-10 | Stop command | Send GET /alert?state=STOP | System enters STANDBY |
| NET-11 | Communication watchdog | Interrupt alert commands during active monitoring | CONNECTION_LOST state activates after the configured five-second timeout |
| NET-12 | Communication recovery | Resume valid alert commands | System accepts the new monitoring state |

## 8. Drowsiness Detection Test Cases

All driver-state simulations must be performed in a stationary environment.

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| DET-01 | Normal monitoring | Keep eyes open with face visible | NORMAL state |
| DET-02 | Eye closure | Simulate eye closure meeting the configured threshold | CLOSING state |
| DET-03 | Warning detection | Maintain eye closure until the warning threshold is reached | WARNING state |
| DET-04 | Drowsiness detection | Maintain eye closure until the drowsiness threshold is reached | DROWSY state |
| DET-05 | Face absence | Move face outside the camera view | NO_FACE state |
| DET-06 | Recovery detection | Open eyes and resume normal facial positioning | System returns to NORMAL according to the detection logic |

Detection thresholds are determined by the Android application implementation.

## 9. Alert State Test Cases

| Test ID | Monitoring State | Expected LED | Expected Buzzer | Expected Motors |
|---|---|---|---|---|
| ALT-01 | STANDBY | OFF | OFF | OFF |
| ALT-02 | NORMAL | OFF | OFF | OFF |
| ALT-03 | CLOSING | Blinking | OFF | OFF |
| ALT-04 | WARNING | ON | Pulsing | OFF |
| ALT-05 | DROWSY | ON | Pulsing | Pulsing |
| ALT-06 | NO_FACE | Slow flashing | OFF | OFF |
| ALT-07 | CONNECTION_LOST | Rapid blinking | Periodic beep | OFF |

## 10. Alert Timing Test Cases

| Test ID | State | Component | Expected Timing |
|---|---|---|---|
| TIM-01 | CLOSING | LED | 500 ms ON, 500 ms OFF |
| TIM-02 | WARNING | Buzzer | 500 ms ON, 500 ms OFF |
| TIM-03 | DROWSY | Buzzer and motors | 300 ms ON, 300 ms OFF |
| TIM-04 | NO_FACE | LED | 250 ms ON, 1750 ms OFF |
| TIM-05 | CONNECTION_LOST | LED | 100 ms ON, 100 ms OFF |
| TIM-06 | CONNECTION_LOST | Buzzer | 200 ms ON, 2800 ms OFF |
| TIM-07 | Communication watchdog | State transition | 5000 ms configured timeout |

Actual timing measurements may differ slightly from configured values because of firmware execution and communication latency.

## 11. Integrated Product Test Cases

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| INT-01 | Complete system startup | Start ESP32 and Android application | System becomes operational |
| INT-02 | End-to-end image processing | Capture and transmit driver images | Application processes received frames |
| INT-03 | End-to-end alert generation | Simulate a drowsiness condition | LED, buzzer and motors activate |
| INT-04 | Normal state recovery | Return to a normal monitoring condition | Hardware alerts deactivate |
| INT-05 | Monitoring interruption | Disconnect application communication | Communication-loss warning activates |
| INT-06 | Monitoring restart | Reconnect and restart monitoring | System resumes operation |
| INT-07 | Laptop-independent operation | Operate using ESP32 and Android smartphone without a laptop | Driver monitoring operates independently |
| INT-08 | Internet-independent operation | Operate using only the ESP32 local Wi-Fi network | Monitoring and alerts function without internet |
| INT-09 | Continuous operation | Run monitoring for a recorded test duration | No unexpected interruption during the test period |
| INT-10 | Product shutdown | Stop monitoring and switch off the hardware | System shuts down correctly |

## 12. Test Result Classification

| Result | Description |
|---|---|
| PASS | Actual behaviour satisfies the expected result |
| FAIL | Actual behaviour does not satisfy the expected result |
| NOT TESTED | Test has not been performed |
| NOT RECORDED | Individual test evidence or measurements are unavailable |
| NOT APPLICABLE | Test does not apply to the tested configuration |

## 13. Test Documentation

Individual test sketches are available in:

[firmware/tests](../firmware/tests/)

Actual test observations and validation results should be documented in:

[test_results.md](test_results.md)

This document defines 65 proposed test cases. The existence of a test case does not establish that it has been individually executed or passed.

## 14. Safety Precautions

- Perform all driver-state simulations in a stationary environment.
- Verify electrical connections before testing.
- Disconnect power before modifying hardware.
- Do not exceed component voltage or current ratings.
- Do not overwrite working integrated firmware without preserving a verified backup.
- Stop testing if abnormal heating or electrical behaviour occurs.

## 15. Disclaimer

AlertDrive is an academic engineering prototype and is not a certified automotive safety system.