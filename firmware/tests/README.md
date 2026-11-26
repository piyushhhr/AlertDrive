# AlertDrive – Firmware Component Testing

## 1. Overview

This directory contains independent Arduino test sketches for the hardware and communication subsystems of AlertDrive.

Each sketch isolates a specific component or subsystem to simplify troubleshooting, hardware verification and firmware development.

The sketches are designed for Arduino IDE and the ESP32-S3 N16R8 development board.

## 2. Testing Directory Structure

```text
tests/
├── led_test/
│   └── led_test.ino
├── buzzer_test/
│   └── buzzer_test.ino
├── motor_test/
│   └── motor_test.ino
├── camera_test/
│   └── camera_test.ino
├── communication_test/
│   └── communication_test.ino
└── README.md
```

## 3. Arduino IDE Configuration

| Parameter | Configuration |
|---|---|
| Board | ESP32S3 Dev Module |
| Flash Size | 16 MB |
| PSRAM | OPI PSRAM |
| Serial Monitor | 115200 baud |
| Programming Language | Arduino C/C++ |

## 4. LED Test

**File:** [led_test.ino](led_test/led_test.ino)

### Objective

Verify the operation of the LED connected to GPIO38.

### Hardware Configuration

| Parameter | Value |
|---|---|
| Control GPIO | GPIO38 |
| Control logic | Active-HIGH |
| Protection | Current-limiting resistor |

### Expected Behaviour

- LED turns ON for one second.
- LED turns OFF for one second.
- The cycle repeats continuously.

### Pass Criteria

The LED blinks consistently without unexpected interruptions.

## 5. Buzzer Test

**File:** [buzzer_test.ino](buzzer_test/buzzer_test.ino)

### Objective

Verify the operation of the 5V buzzer through the C2N7000 MOSFET switching circuit.

### Hardware Configuration

| Parameter | Value |
|---|---|
| Control GPIO | GPIO14 |
| Buzzer voltage | 5V |
| Switching device | C2N7000 MOSFET |
| Control logic | Active-HIGH |

### Expected Behaviour

- Buzzer activates for one second.
- Buzzer deactivates for one second.
- The cycle repeats continuously.

### Pass Criteria

The active buzzer produces consistent audible alerts according to the programmed timing.

## 6. Vibration Motor Test

**File:** [motor_test.ino](motor_test/motor_test.ino)

### Objective

Verify the operation of the vibration motors through the XY-GMOS MOSFET switching module.

### Hardware Configuration

| Parameter | Value |
|---|---|
| Control GPIO | GPIO47 |
| Motor rated voltage | 3V |
| Switching device | XY-GMOS MOSFET module |
| Control logic | Active-HIGH |

### Expected Behaviour

- Vibration motors activate for one second.
- Vibration motors deactivate for two seconds.
- The cycle repeats continuously.

### Pass Criteria

The motors operate according to the programmed timing without unexpected interruptions.

## 7. GC2145 Camera Test

**File:** [camera_test.ino](camera_test/camera_test.ino)

### Objective

Verify camera initialization, image capture and software JPEG conversion.

### Camera Configuration

| Parameter | Value |
|---|---|
| Camera sensor | GC2145 |
| Resolution | 640 × 480 |
| Pixel format | RGB565 |
| External clock | 20 MHz |
| Frame buffer count | 1 |
| Frame buffer location | PSRAM |
| Output conversion | JPEG |

### Expected Behaviour

- PSRAM is detected.
- Camera initializes successfully.
- Camera sensor information is displayed.
- Camera frames are captured.
- RGB565 frames are converted into JPEG images.
- Frame size and processing duration are displayed in the serial monitor.

### Pass Criteria

Camera initialization, repeated frame capture and JPEG conversion complete successfully without errors.

Image quality must be evaluated separately.

## 8. Communication Test

**File:** [communication_test.ino](communication_test/communication_test.ino)

### Objective

Verify local Wi-Fi connectivity, HTTP communication, alert command handling and communication-loss detection.

### Network Configuration

| Parameter | Value |
|---|---|
| Wi-Fi SSID | ALERT-DRIVE |
| ESP32 IP address | 192.168.4.1 |
| HTTP port | 80 |
| Watchdog timeout | 5000 ms |
| Internet required | No |

### HTTP Test Cases

| Endpoint | Expected Response |
|---|---|
| GET / | Communication test online |
| GET /health | JSON system status |
| GET /alert?state=NORMAL | HTTP 200 |
| GET /alert?state=CLOSING | HTTP 200 |
| GET /alert?state=WARNING | HTTP 200 |
| GET /alert?state=DROWSY | HTTP 200 |
| GET /alert?state=NO_FACE | HTTP 200 |
| GET /alert?state=STANDBY | HTTP 200 |
| GET /alert?state=STOP | HTTP 200 |
| GET /alert?state=INVALID | HTTP 400 |
| GET /alert | HTTP 400 |
| Unregistered endpoint | HTTP 404 |

### Communication Watchdog Test

1. Connect a smartphone or computer to the ESP32 access point.
2. Send a valid active monitoring state.
3. Stop sending alert commands.
4. Allow five seconds to elapse.
5. Retrieve the health status.
6. Verify that the reported state is CONNECTION_LOST.

### Pass Criteria

- The ESP32 access point initializes successfully.
- Valid HTTP requests receive appropriate responses.
- Invalid commands are rejected.
- STOP returns the system to standby.
- Communication interruption is detected.

This sketch does not initialize the camera or activate physical alert components. It verifies the communication protocol independently of the complete Android monitoring workflow.

## 9. Running Individual Tests

1. Open the required test sketch in Arduino IDE.
2. Select the ESP32S3 Dev Module board.
3. Configure the required board settings.
4. Verify the hardware connections.
5. Select the correct serial port.
6. Compile the sketch using Verify.
7. Upload the sketch to a dedicated test board or an ESP32 intended for testing.
8. Open the serial monitor at 115200 baud.
9. Observe the component behaviour.
10. Compare the results against the expected pass criteria.

## 10. Restoring the Integrated Firmware

Uploading a component test sketch replaces the currently installed application firmware.

After testing, restore the verified integrated AlertDrive firmware before attempting normal product operation.

The recovered integrated source code is located at:

[AlertDrive.ino](../AlertDrive/AlertDrive.ino)

Confirm compatibility with the working hardware and Android application before flashing the recovered source.

## 11. Testing Status

| Subsystem | Test Sketch |
|---|---|
| LED | Available |
| Buzzer | Available |
| Vibration motors | Available |
| GC2145 camera | Available |
| Wi-Fi and HTTP communication | Available |

The development team has reported successful integrated hardware validation and product testing.

The individual sketches in this directory are reconstructed test programs provided for reproducibility and troubleshooting. They are not necessarily identical to the original testing programs and require independent compilation and execution before their results can be recorded as verified.

## 12. Safety Precautions

- Disconnect power before changing hardware connections.
- Never connect 5V directly to an ESP32 GPIO.
- Use a current-limiting resistor for the LED.
- Use appropriate MOSFET switching circuits for the buzzer and vibration motors.
- Verify voltage and current requirements before testing.
- Do not power motors directly from ESP32 GPIO pins.
- Perform testing in a stationary environment.
- Preserve the working integrated firmware before uploading component test sketches.

## 13. Related Documentation

- [Main Project README](../../README.md)
- [Firmware Documentation](../README.md)
- [Hardware Components](../../hardware/components.md)
- [GPIO Configuration](../../hardware/pin_configuration.md)
- [Wiring Guide](../../hardware/wiring_guide.md)
- [Installation Guide](../../INSTALLATION.md)

## 14. Disclaimer

AlertDrive is an academic engineering prototype and is not a certified automotive safety system.