# AlertDrive – GPIO Pin Configuration

## 1. Microcontroller Specifications

| Parameter | Specification |
|---|---|
| Microcontroller | ESP32-S3 N16R8 |
| Flash memory | 16 MB |
| PSRAM | 8 MB |
| GPIO logic voltage | 3.3V |
| Camera sensor | GC2145 |

## 2. Alert System GPIO Configuration

| Component | GPIO | Direction | Switching Circuit |
|---|---|---|---|
| LED | GPIO38 | Output | Current-limiting resistor |
| 5V buzzer | GPIO14 | Output | C2N7000 MOSFET |
| 3V vibration motors | GPIO47 | Output | XY-GMOS MOSFET module |

All three alert outputs use active-HIGH control logic in the recovered firmware.

### LED Control – GPIO38

- HIGH: LED ON
- LOW: LED OFF
- Current-limiting resistor required.

### Buzzer Control – GPIO14

- Switching device: C2N7000 N-channel MOSFET
- Buzzer supply voltage: 5V
- HIGH: Buzzer ON
- LOW: Buzzer OFF

The GPIO controls the MOSFET gate rather than supplying power directly to the buzzer.

Correct switching operation at a 3.3V gate voltage must be verified.

### Vibration Motor Control – GPIO47

- Switching device: XY-GMOS MOSFET module
- Motor rated voltage: 3V
- HIGH: Vibration motors ON
- LOW: Vibration motors OFF

The GPIO provides a control signal to the MOSFET module. The motors require a suitable external power supply.

The XY-GMOS module's operating voltage requirements must be verified separately.

## 3. GC2145 Camera GPIO Configuration

| Camera Signal | ESP32-S3 GPIO | Function |
|---|---|---|
| XCLK | GPIO15 | External camera clock |
| SIOD | GPIO4 | SCCB data |
| SIOC | GPIO5 | SCCB clock |
| D0 | GPIO11 | Data bit 0 |
| D1 | GPIO9 | Data bit 1 |
| D2 | GPIO8 | Data bit 2 |
| D3 | GPIO10 | Data bit 3 |
| D4 | GPIO12 | Data bit 4 |
| D5 | GPIO18 | Data bit 5 |
| D6 | GPIO17 | Data bit 6 |
| D7 | GPIO16 | Data bit 7 |
| VSYNC | GPIO6 | Vertical synchronization |
| HREF | GPIO7 | Horizontal reference |
| PCLK | GPIO13 | Pixel clock |
| PWDN | -1 | Not controlled through GPIO |
| RESET | -1 | Not controlled through GPIO |

## 4. Camera Configuration

| Parameter | Value |
|---|---|
| Camera sensor | GC2145 |
| Frame resolution | 640 × 480 (VGA) |
| Pixel format | RGB565 |
| External clock frequency | 20 MHz |
| Frame buffer count | 1 |
| Frame buffer location | PSRAM |
| Image transmission format | JPEG |
| JPEG conversion | Software-based |
| Grab mode | CAMERA_GRAB_WHEN_EMPTY |

## 5. Complete GPIO Usage Summary

| GPIO | Assigned Function |
|---|---|
| GPIO4 | Camera SIOD |
| GPIO5 | Camera SIOC |
| GPIO6 | Camera VSYNC |
| GPIO7 | Camera HREF |
| GPIO8 | Camera D2 |
| GPIO9 | Camera D1 |
| GPIO10 | Camera D3 |
| GPIO11 | Camera D0 |
| GPIO12 | Camera D4 |
| GPIO13 | Camera PCLK |
| GPIO14 | C2N7000 buzzer control |
| GPIO15 | Camera XCLK |
| GPIO16 | Camera D7 |
| GPIO17 | Camera D6 |
| GPIO18 | Camera D5 |
| GPIO38 | LED control |
| GPIO47 | XY-GMOS vibration motor control |

There are no overlapping GPIO assignments between the camera and alert system in the recovered firmware.

## 6. Hardware Alert Patterns

| State | LED | Buzzer | Vibration Motors |
|---|---|---|---|
| STANDBY | OFF | OFF | OFF |
| NORMAL | OFF | OFF | OFF |
| CLOSING | Blinking | OFF | OFF |
| WARNING | ON | Pulsing | OFF |
| DROWSY | ON | Pulsing | Pulsing |
| NO_FACE | Slow flashing | OFF | OFF |
| CONNECTION_LOST | Rapid blinking | Periodic beep | OFF |

## 7. Alert Timing Configuration

| State | Output | Timing |
|---|---|---|
| CLOSING | LED | 500 ms ON, 500 ms OFF |
| WARNING | Buzzer | 500 ms ON, 500 ms OFF |
| DROWSY | Buzzer and motors | 300 ms ON, 300 ms OFF |
| NO_FACE | LED | 250 ms ON, 1750 ms OFF |
| CONNECTION_LOST | LED | 100 ms ON, 100 ms OFF |
| CONNECTION_LOST | Buzzer | 200 ms ON, 2800 ms OFF |

The communication watchdog timeout is 5000 ms during active monitoring.

## 8. Electrical Precautions

- ESP32-S3 GPIO pins operate at 3.3V logic levels.
- Never apply 5V directly to a GPIO pin.
- Connect the LED through a suitable current-limiting resistor.
- Use the C2N7000 MOSFET for buzzer switching.
- Use the XY-GMOS module for vibration motor switching.
- Verify the MOSFET gate voltage and load current requirements.
- Do not power the buzzer or motors directly from GPIO pins.
- Do not supply 5V directly to the 3V vibration motors.
- Maintain a common ground between non-isolated switching circuits and the ESP32.
- Provide suitable inductive load protection.
- Do not reuse dedicated camera GPIOs for external alert components.
- Disconnect power before modifying hardware connections.

## 9. Validation Status

| Configuration | Status |
|---|---|
| LED GPIO38 | Previously tested successfully |
| Alternative LED GPIO21 | Previously tested successfully; not used in integrated firmware |
| Buzzer GPIO14 | Assigned in recovered firmware |
| C2N7000 buzzer switching | User-confirmed component selection |
| Vibration motor GPIO47 | Assigned in recovered firmware |
| XY-GMOS motor switching | User-confirmed component selection |
| GC2145 camera mapping | Defined in recovered firmware |
| Complete integrated configuration | Requires final verification |

## 10. Reference

The GPIO assignments, camera parameters and alert timing configurations are based on:

firmware/AlertDrive/AlertDrive.ino

Any changes to the firmware GPIO configuration must also be reflected in this document.

## 11. Disclaimer

AlertDrive is an academic prototype and is not a certified automotive safety system.