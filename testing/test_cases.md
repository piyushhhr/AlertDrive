
# AlertDrive – Product Test Cases

## 1. Introduction

This document defines the functional, hardware, software, communication and integration test cases for the AlertDrive driver drowsiness detection and alert system.

The test cases are based on the integrated AlertDrive firmware, Android application, hardware component selection and supplied circuit diagram.

## 2. Testing Objectives

- Verify individual hardware component functionality.
- Verify GC2145 camera operation.
- Verify LED operation through the 200 Ω current-limiting resistor.
- Verify buzzer operation through the C2N7000 MOSFET.
- Verify vibration motor operation through the XY-GMOS module.
- Verify the LM2596 motor voltage regulation arrangement.
- Verify Android application functionality.
- Verify local Wi-Fi and HTTP communication.
- Verify drowsiness detection and alert generation.
- Verify communication-loss detection.
- Verify integrated product operation.

## 3. Testing Environment

| Parameter | Specification |
|---|---|
| Microcontroller | ESP32-S3 N16R8 |
| Flash memory | 16 MB |
| PSRAM | 8 MB |
| Camera | GC2145 |
| Android application | AlertDrive |
| Firmware | Arduino C/C++ |
| Connectivity | Local Wi-Fi |
| Network name | ALERT-DRIVE |
| Communication protocol | HTTP |
| ESP32 IP address | 192.168.4.1 |
| Visual alert | Red LED |
| LED control GPIO | GPIO38 |
| LED resistor | 200 Ω |
| Audible alert | 5V buzzer |
| Buzzer control GPIO | GPIO14 |
| Buzzer switching | C2N7000 MOSFET |
| MOSFET gate resistor | 200 Ω |
| MOSFET gate pull-down resistor | 10 kΩ |
| Tactile alert | Two 3V coin vibration motors |
| Motor control GPIO | GPIO47 |
| Motor switching | XY-GMOS MOSFET module |
| Motor voltage regulation | LM2596 buck converter |
| Target motor supply voltage | 3V DC |
| Protection diodes | Two 1N5817 Schottky diodes |
| Test environment | Stationary prototype testing |

### Pre-Test Requirements

Before performing hardware and integrated product tests:

- Verify all electrical connections.
- Verify the common ground connections.
- Verify LED polarity and resistor placement.
- Verify C2N7000 terminal assignments.
- Verify the MOSFET gate and pull-down resistor connections.
- Verify the polarity of both 1N5817 protection diodes.
- Measure the LM2596 output voltage before connecting the vibration motors.
- Verify that the XY-GMOS module supports the separate motor supply arrangement.
- Verify sufficient power supply current capacity.
- Ensure the integrated firmware and Android application are available.

These requirements represent proposed electrical verification procedures and do not constitute recorded measurements.

## 4. Hardware Test Cases

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| HW-01 | ESP32 startup | Connect power to the embedded hardware | ESP32 initializes successfully |
| HW-02 | Camera initialization | Start the integrated firmware | GC2145 initializes without errors |
| HW-03 | LED operation | Verify the 200 Ω resistor and activate GPIO38 | Red LED illuminates |
| HW-04 | Buzzer operation | Verify the C2N7000 circuit and activate GPIO14 | The 5V buzzer produces an audible alert |
| HW-05 | Vibration motor operation | Verify the regulated 3V motor supply and activate GPIO47 through the XY-GMOS module | Both vibration motors operate simultaneously |
| HW-06 | Output deactivation | Set the system to STANDBY | LED, buzzer and both vibration motors deactivate |
| HW-07 | Hardware integration | Activate the DROWSY state | LED, buzzer and both vibration motors operate according to the configured pattern |

### Hardware Verification Notes

The hardware tests require the intended component configuration described in the hardware documentation.

The LM2596 output voltage, MOSFET operating characteristics and XY-GMOS compatibility must be verified separately before reproducing the circuit.

## 5. Camera Test Cases

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| CAM-01 | Camera detection | Initialize the camera driver | GC2145 is detected |
| CAM-02 | PSRAM availability | Check PSRAM during initialization | PSRAM is available |
| CAM-03 | Frame capture | Request a camera frame | Image is captured successfully |
| CAM-04 | Image resolution | Inspect captured frame dimensions | Resolution is 640 × 480 |
| CAM-05 | JPEG conversion | Convert RGB565 frame into JPEG | Valid JPEG image is generated |
| CAM-06 | Frame transmission | Access the `/frame` endpoint | JPEG image is received |
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
| NET-01 | Wi-Fi access point | Connect power to the ESP32-S3 | ALERT-DRIVE network becomes available |
| NET-02 | Wi-Fi connection | Connect the Android smartphone to ALERT-DRIVE | Local Wi-Fi connection succeeds |
| NET-03 | Device information | Send `GET /` | Device information is returned |
| NET-04 | Health endpoint | Send `GET /health` with camera initialized | System status is returned |
| NET-05 | Frame endpoint | Send `GET /frame` | JPEG image is returned |
| NET-06 | Valid alert command | Send a valid monitoring state | HTTP 200 response is returned |
| NET-07 | Invalid alert command | Send an unsupported monitoring state | HTTP 400 response is returned |
| NET-08 | Missing state parameter | Send `GET /alert` without a state | HTTP 400 response is returned |
| NET-09 | Unknown endpoint | Request an unregistered endpoint | HTTP 404 response is returned |
| NET-10 | Stop command | Send `GET /alert?state=STOP` | System enters STANDBY |
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

| Test ID | Monitoring State | Expected LED | Expected Buzzer | Expected Vibration Motors |
|---|---|---|---|---|
| ALT-01 | STANDBY | OFF | OFF | OFF |
| ALT-02 | NORMAL | OFF | OFF | OFF |
| ALT-03 | CLOSING | Blinking | OFF | OFF |
| ALT-04 | WARNING | ON | Pulsing | OFF |
| ALT-05 | DROWSY | ON | Pulsing | Both pulsing |
| ALT-06 | NO_FACE | Slow flashing | OFF | OFF |
| ALT-07 | CONNECTION_LOST | Rapid blinking | Periodic beep | OFF |

### Alert Hardware Configuration

| Alert Component | GPIO | Control Circuit |
|---|---|---|
| Red LED | GPIO38 | 200 Ω series resistor |
| 5V buzzer | GPIO14 | C2N7000 MOSFET |
| Two 3V vibration motors | GPIO47 | XY-GMOS MOSFET module |

All three alert outputs use active-HIGH control logic.

Both vibration motors are intended to activate simultaneously through the same motor control signal.

## 10. Alert Timing Test Cases

| Test ID | State | Component | Expected Timing |
|---|---|---|---|
| TIM-01 | CLOSING | LED | 500 ms ON, 500 ms OFF |
| TIM-02 | WARNING | Buzzer | 500 ms ON, 500 ms OFF |
| TIM-03 | DROWSY | Buzzer and both vibration motors | 300 ms ON, 300 ms OFF |
| TIM-04 | NO_FACE | LED | 250 ms ON, 1750 ms OFF |
| TIM-05 | CONNECTION_LOST | LED | 100 ms ON, 100 ms OFF |
| TIM-06 | CONNECTION_LOST | Buzzer | 200 ms ON, 2800 ms OFF |
| TIM-07 | Communication watchdog | State transition | 5000 ms configured timeout |

Actual timing measurements may differ slightly from configured values because of firmware execution and communication latency.

These values represent programmed firmware timings rather than independently measured response times.

## 11. Integrated Product Test Cases

| Test ID | Test Description | Test Procedure | Expected Result |
|---|---|---|---|
| INT-01 | Complete system startup | Connect power to the ESP32-S3 and launch the Android application | System becomes operational |
| INT-02 | End-to-end image processing | Capture and transmit driver images | Application processes received frames |
| INT-03 | End-to-end alert generation | Simulate a drowsiness condition | LED, buzzer and both vibration motors activate |
| INT-04 | Normal state recovery | Return to a normal monitoring condition | Hardware alerts deactivate |
| INT-05 | Monitoring interruption | Disconnect application communication during active monitoring | Communication-loss warning activates |
| INT-06 | Monitoring restart | Reconnect and restart monitoring | System resumes operation |
| INT-07 | Laptop-independent operation | Operate using a compatible ESP32 power source and Android smartphone without a laptop | Driver monitoring operates independently |
| INT-08 | Internet-independent operation | Operate using only the ESP32 local Wi-Fi network | Monitoring and alerts function without internet |
| INT-09 | Continuous operation | Run monitoring for a recorded test duration | No unexpected interruption during the test period |
| INT-10 | Product shutdown | Stop monitoring, verify alert deactivation and disconnect power from the embedded hardware | Monitoring stops and embedded hardware shuts down correctly |

## 12. Test Coverage Summary

| Testing Category | Test ID Range | Number of Test Cases |
|---|---|---:|
| Hardware | HW-01 to HW-07 | 7 |
| Camera | CAM-01 to CAM-07 | 7 |
| Android application | APP-01 to APP-09 | 9 |
| Network and communication | NET-01 to NET-12 | 12 |
| Drowsiness detection | DET-01 to DET-06 | 6 |
| Alert states | ALT-01 to ALT-07 | 7 |
| Alert timing | TIM-01 to TIM-07 | 7 |
| Integrated product | INT-01 to INT-10 | 10 |
| **Total** | | **65** |

## 13. Test Result Classification

| Result | Description |
|---|---|
| PASS | Actual behaviour satisfies the expected result |
| FAIL | Actual behaviour does not satisfy the expected result |
| NOT TESTED | Test has not been performed |
| NOT RECORDED | Individual test evidence or measurements are unavailable |
| NOT APPLICABLE | Test does not apply to the tested configuration |

## 14. Component Testing Programs

Individual Arduino test sketches are available for component verification and troubleshooting.

| Component | Test Program |
|---|---|
| LED | [led_test.ino](../firmware/tests/led_test/led_test.ino) |
| Buzzer | [buzzer_test.ino](../firmware/tests/buzzer_test/buzzer_test.ino) |
| Vibration motors | [motor_test.ino](../firmware/tests/motor_test/motor_test.ino) |
| GC2145 camera | [camera_test.ino](../firmware/tests/camera_test/camera_test.ino) |
| Network and communication | [communication_test.ino](../firmware/tests/communication_test/communication_test.ino) |

For individual test procedures, refer to the [Component Testing Guide](../firmware/tests/README.md).

The reconstructed component test sketches are provided for reproducibility and troubleshooting. Independent compilation and execution of these reconstructed sketches have not been recorded.

## 15. Test Documentation and Validation

The development team has reported successful completion of integrated hardware validation and product testing.

Actual test observations and validation results are documented where available in:

[Product Test Results](test_results.md)

This document defines 65 proposed test cases. The existence of a test case does not establish that it has been individually executed or passed.

### Electrical Measurement Limitations

The following electrical measurements are not recorded in the available test documentation:

- LM2596 output voltage under motor load.
- Combined vibration motor startup current.
- Total system operating current.
- Total system power consumption.
- C2N7000 conduction characteristics at a 3.3V gate voltage.

The compatibility of the separate motor supply arrangement with the XY-GMOS module also requires independent electrical verification.

No unrecorded measurements are claimed as verified test results.

## 16. Safety Precautions

- Perform all driver-state simulations in a stationary environment.
- Verify electrical connections before testing.
- Disconnect power before modifying hardware.
- Never apply 5V directly to an ESP32 GPIO pin.
- Never power the buzzer or vibration motors directly from GPIO pins.
- Verify the C2N7000 MOSFET terminal assignments.
- Verify the 200 Ω resistor connections.
- Verify the 10 kΩ gate pull-down resistor.
- Verify the polarity of both 1N5817 protection diodes.
- Measure the LM2596 output voltage before connecting the motors.
- Verify the XY-GMOS module's electrical compatibility.
- Never apply 5V directly to the 3V vibration motors.
- Do not exceed component voltage or current ratings.
- Maintain secure common ground connections.
- Do not overwrite working integrated firmware without preserving a verified backup.
- Stop testing if abnormal heating or electrical behaviour occurs.

## 17. Related Documentation

- [Main Project README](../README.md)
- [Installation Guide](../INSTALLATION.md)
- [Hardware Components](../hardware/components.md)
- [Hardware Wiring Guide](../hardware/wiring_guide.md)
- [GPIO Configuration](../hardware/pin_configuration.md)
- [Power Supply Configuration](../hardware/power_supply.md)
- [Circuit Diagrams](../hardware/circuit_diagrams/)
- [Integrated Firmware](../firmware/AlertDrive/AlertDrive.ino)
- [Component Testing Guide](../firmware/tests/README.md)
- [Product Test Results](test_results.md)

## 18. Disclaimer

AlertDrive is an academic engineering prototype.

It is not a certified automotive safety system and must not be relied upon as a substitute for attentive driving, adequate rest or certified driver monitoring equipment.
