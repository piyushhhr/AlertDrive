
# AlertDrive – GPIO Pin Configuration

## 1. Microcontroller Specifications

| Parameter | Specification |
|---|---|
| Microcontroller | ESP32-S3 N16R8 |
| Flash memory | 16 MB |
| PSRAM | 8 MB |
| GPIO logic voltage | 3.3V |
| Camera sensor | GC2145 |
| Programming framework | Arduino ESP32 |

## 2. Alert System GPIO Configuration

| Component | GPIO | Direction | Control Circuit |
|---|---|---|---|
| Red LED | GPIO38 | Output | 200 Ω current-limiting resistor |
| 5V buzzer | GPIO14 | Output | C2N7000 MOSFET |
| Two 3V vibration motors | GPIO47 | Output | XY-GMOS MOSFET module |

All three alert outputs use active-HIGH control logic in the integrated firmware.

### LED Control – GPIO38

| Parameter | Configuration |
|---|---|
| Control GPIO | GPIO38 |
| LED colour | Red |
| Series resistor | 200 Ω |
| GPIO voltage | 3.3V |
| Control logic | Active-HIGH |

- HIGH: LED ON.
- LOW: LED OFF.

The LED is connected to GPIO38 through a 200 Ω current-limiting resistor.

### Buzzer Control – GPIO14

| Parameter | Configuration |
|---|---|
| Control GPIO | GPIO14 |
| Buzzer rated voltage | 5V DC |
| Switching device | C2N7000 N-channel MOSFET |
| Gate control voltage | 3.3V |
| Gate resistor | 200 Ω |
| Gate pull-down resistor | 10 kΩ |
| Protection diode | 1N5817 |
| Switching method | Low-side switching |
| Control logic | Active-HIGH |

- HIGH: Buzzer ON.
- LOW: Buzzer OFF.

GPIO14 controls the C2N7000 MOSFET gate through the 200 Ω resistor.

The 10 kΩ pull-down resistor maintains the MOSFET in the OFF state when the control signal is floating.

The buzzer receives power from the 5V supply rail rather than directly from the GPIO pin.

The MOSFET's actual terminal configuration and sufficient conduction at a 3.3V gate voltage must be verified.

### Vibration Motor Control – GPIO47

| Parameter | Configuration |
|---|---|
| Control GPIO | GPIO47 |
| Motor type | Coin vibration motor |
| Motor quantity | 2 |
| Motor rated voltage | 3V DC |
| Motor connection | Parallel |
| Switching device | XY-GMOS MOSFET module |
| Voltage regulation | LM2596 buck converter |
| Protection diode | 1N5817 |
| Control logic | Active-HIGH |

- HIGH: Both vibration motors ON.
- LOW: Both vibration motors OFF.

GPIO47 provides the control signal to the XY-GMOS module.

The LM2596 buck converter provides the intended 3V motor supply.

The GPIO pin does not directly supply the vibration motor current.

The separate motor supply arrangement shown in the circuit diagram requires verification against the XY-GMOS module's internal switching topology.

## 3. GC2145 Camera GPIO Configuration

The GC2145 camera connects to the ESP32-S3 through the compatible camera ribbon connector.

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

The camera is not illustrated in the supplied alert hardware circuit diagram. Its GPIO assignments are retained from the integrated firmware.

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

There are no overlapping GPIO assignments between the camera interface and the alert system in the integrated firmware.

## 6. External Circuit Components

The following components are associated with the alert hardware but do not require additional dedicated GPIO pins.

| Component | Quantity | Function |
|---|---:|---|
| 200 Ω resistor | 1 | LED current limiting |
| 200 Ω resistor | 1 | C2N7000 gate control |
| 10 kΩ resistor | 1 | C2N7000 gate pull-down |
| C2N7000 MOSFET | 1 | Buzzer switching |
| XY-GMOS MOSFET module | 1 | Vibration motor switching |
| LM2596 buck converter | 1 | Intended motor voltage regulation |
| 1N5817 Schottky diode | 2 | Inductive load protection |
| 3V coin vibration motor | 2 | Tactile alerts |

## 7. Hardware Alert Patterns

| State | LED | Buzzer | Vibration Motors |
|---|---|---|---|
| STANDBY | OFF | OFF | OFF |
| NORMAL | OFF | OFF | OFF |
| CLOSING | Blinking | OFF | OFF |
| WARNING | ON | Pulsing | OFF |
| DROWSY | ON | Pulsing | Pulsing |
| NO_FACE | Slow flashing | OFF | OFF |
| CONNECTION_LOST | Rapid blinking | Periodic beep | OFF |

## 8. Alert Timing Configuration

| State | Output | Configured Timing |
|---|---|---|
| CLOSING | LED | 500 ms ON, 500 ms OFF |
| WARNING | Buzzer | 500 ms ON, 500 ms OFF |
| DROWSY | Buzzer and motors | 300 ms ON, 300 ms OFF |
| NO_FACE | LED | 250 ms ON, 1750 ms OFF |
| CONNECTION_LOST | LED | 100 ms ON, 100 ms OFF |
| CONNECTION_LOST | Buzzer | 200 ms ON, 2800 ms OFF |

The communication watchdog timeout is configured to 5000 ms during active monitoring.

These timings are programmed firmware values rather than independently measured response times.

## 9. Firmware GPIO Definitions

The integrated AlertDrive Arduino firmware defines the alert GPIO pins as follows:

```cpp
#define LED_PIN     38
#define BUZZER_PIN  14
#define MOTOR_PIN   47
```

The camera GPIO pins are defined as follows:

```cpp
#define CAM_PWDN    -1
#define CAM_RESET   -1

#define CAM_XCLK    15
#define CAM_SIOD     4
#define CAM_SIOC     5

#define CAM_D0      11
#define CAM_D1       9
#define CAM_D2       8
#define CAM_D3      10
#define CAM_D4      12
#define CAM_D5      18
#define CAM_D6      17
#define CAM_D7      16

#define CAM_VSYNC    6
#define CAM_HREF     7
#define CAM_PCLK    13
```

These definitions correspond to the integrated AlertDrive firmware.

## 10. Electrical Precautions

- ESP32-S3 GPIO pins operate at 3.3V logic levels.
- Never apply 5V directly to an ESP32 GPIO pin.
- Use a 200 Ω current-limiting resistor for the LED.
- Use the C2N7000 MOSFET for buzzer control.
- Use a 200 Ω MOSFET gate resistor.
- Use a 10 kΩ gate pull-down resistor.
- Verify the MOSFET terminal configuration.
- Verify sufficient MOSFET conduction at 3.3V.
- Use the XY-GMOS module for vibration motor control.
- Verify the LM2596 output voltage before connecting the motors.
- Never supply 5V directly to the 3V vibration motors.
- Do not power the buzzer or motors directly from GPIO pins.
- Verify the polarity of both 1N5817 protection diodes.
- Maintain a secure common ground.
- Do not reuse dedicated camera GPIOs for other components.
- Disconnect power before modifying connections.

## 11. Circuit Diagram Compatibility

| Configuration | Verification Status |
|---|---|
| LED GPIO38 | Matches firmware and circuit diagram |
| Buzzer GPIO14 | Matches firmware and circuit diagram |
| Vibration motor GPIO47 | Matches firmware and circuit diagram |
| LED series resistor | 200 Ω, as illustrated |
| MOSFET gate resistor | 200 Ω, as illustrated |
| MOSFET gate pull-down | 10 kΩ, as illustrated |
| C2N7000 MOSFET | User-confirmed component |
| XY-GMOS module | User-confirmed component |
| LM2596 buck converter | Present in circuit diagram |
| 1N5817 protection diodes | Present in circuit diagram |
| GC2145 camera GPIOs | Defined in integrated firmware |

### Diagram Representation Notes

- The C2N7000 MOSFET is represented using an IRLZ44N component graphic in the supplied diagram.
- The two intended vibration motors are represented using components labelled buzzer.
- The GC2145 camera is not illustrated.
- The ESP32 common ground connection is not explicitly illustrated.

These representation differences should be corrected in the final circuit diagram.

## 12. Hardware Validation Status

The development team has reported successful integrated hardware validation and product testing.

| Configuration | Reported Status |
|---|---|
| LED GPIO38 | Operational |
| Alternative LED GPIO21 | Previously tested; not used in integrated firmware |
| Buzzer GPIO14 | Operational |
| Vibration motor GPIO47 | Operational |
| GC2145 camera integration | Operational |
| Android communication | Operational |
| Integrated alert hardware | Validated |
| Final hardware validation | Completed |
| Product testing | Completed |

Detailed electrical measurements, MOSFET operating characteristics and the LM2596 output voltage under load have not been independently recorded.

Successful prototype operation does not establish that every individual electrical parameter has been verified.

## 13. Related Documentation

- [Main Project README](../README.md)
- [Hardware Components](components.md)
- [Hardware Wiring Guide](wiring_guide.md)
- [Power Supply Configuration](power_supply.md)
- [Circuit Diagrams](circuit_diagrams/)
- [Integrated Firmware](../firmware/AlertDrive/AlertDrive.ino)
- [Firmware Component Testing](../firmware/tests/)
- [Product Test Cases](../testing/test_cases.md)
- [Product Test Results](../testing/test_results.md)

Any changes to GPIO assignments in the integrated firmware must also be reflected in this document.

## 14. Disclaimer

AlertDrive is an academic engineering prototype.

It is not a certified automotive safety system and must not be relied upon as a substitute for attentive driving, adequate rest or certified driver monitoring equipment.
