# AlertDrive – Hardware Wiring Guide

## 1. Overview

This document describes the hardware interconnection architecture of the AlertDrive driver drowsiness detection system.

The ESP32-S3 controls the GC2145 camera, LED, buzzer and vibration motors. The Android application performs drowsiness detection and communicates with the ESP32 over a local Wi-Fi connection.

## 2. Hardware Connection Summary

| Component | ESP32 Connection | Control Circuit |
|---|---|---|
| GC2145 camera | Dedicated camera connector | Camera interface |
| LED | GPIO38 | Current-limiting resistor |
| 5V buzzer | GPIO14 | C2N7000 MOSFET |
| 3V vibration motors | GPIO47 | XY-GMOS MOSFET module |
| ON/OFF switch | Main power supply | Power control |

## 3. LED Connections

The LED provides visual feedback according to the current driver monitoring state.

| From | To |
|---|---|
| ESP32 GPIO38 | Current-limiting resistor |
| Current-limiting resistor | LED anode |
| LED cathode | ESP32 GND |

A 330-ohm resistor may be used for a typical indicator LED, subject to its electrical specifications.

## 4. Buzzer Connections

The 5V buzzer is controlled using the C2N7000 N-channel MOSFET as a low-side switch.

### Intended Connection Configuration

| From | To |
|---|---|
| ESP32 GPIO14 | MOSFET gate through a suitable gate resistor |
| MOSFET gate | Ground through a pull-down resistor |
| MOSFET source | Common ground |
| MOSFET drain | Buzzer negative terminal |
| Buzzer positive terminal | Regulated 5V supply |
| Supply ground | ESP32 GND |

### Protection Diode

For an inductive buzzer requiring flyback protection, connect a suitable 1N5817 Schottky diode in parallel with the buzzer.

- Cathode (striped terminal): Buzzer positive terminal.
- Anode (non-striped terminal): Buzzer negative terminal.

### Operating Principle

When GPIO14 goes HIGH, the MOSFET is intended to conduct and activate the buzzer.

When GPIO14 goes LOW, the MOSFET switches off and deactivates the buzzer.

The actual MOSFET terminal order, buzzer current and sufficient MOSFET conduction at 3.3V must be verified before energizing the circuit.

## 5. Vibration Motor Connections

The 3V coin vibration motors are controlled using the XY-GMOS MOSFET switching module.

### Control Connections

| From | To |
|---|---|
| ESP32 GPIO47 | XY-GMOS HIGH/PWM |
| ESP32 GND | XY-GMOS control GND |
| XY-GMOS LOW/SWITCH | Unused |

### Intended Load Connections

| Module Terminal | Connection |
|---|---|
| OUT+ | Motor positive terminal |
| OUT- | Motor negative terminal |
| VIN+ | Compatible regulated supply |
| VIN- | Supply ground |

If multiple 3V motors are installed in parallel, the power supply and switching module must support their combined operating and startup currents.

### Motor Supply Precautions

The vibration motors are rated for 3V operation.

The XY-GMOS module does not provide voltage regulation. A 5V module input must not be assumed to produce a safe 3V motor output.

The module's minimum operating voltage and motor voltage regulation arrangement must be verified before completing the power connections.

Appropriate flyback protection must be provided for the motors.

## 6. GC2145 Camera Connections

The GC2145 camera module connects to the ESP32-S3 camera interface using the compatible ribbon cable.

| Camera Signal | ESP32 GPIO |
|---|---|
| XCLK | GPIO15 |
| SIOD | GPIO4 |
| SIOC | GPIO5 |
| D0 | GPIO11 |
| D1 | GPIO9 |
| D2 | GPIO8 |
| D3 | GPIO10 |
| D4 | GPIO12 |
| D5 | GPIO18 |
| D6 | GPIO17 |
| D7 | GPIO16 |
| VSYNC | GPIO6 |
| HREF | GPIO7 |
| PCLK | GPIO13 |

### Camera Installation Precautions

- Disconnect power before inserting or removing the ribbon cable.
- Verify ribbon cable orientation.
- Ensure the connector locking mechanism is secured.
- Do not reuse dedicated camera GPIOs for other components.

## 7. Power Supply Architecture

The AlertDrive system requires appropriate power supplies for the following subsystems:

| Subsystem | Supply Requirement |
|---|---|
| ESP32-S3 | Compatible board power input |
| GPIO control signals | 3.3V logic |
| Buzzer | Regulated 5V |
| Vibration motors | Regulated 3V |
| Camera | Compatible onboard camera supply |
| Android smartphone | Internal battery or suitable charger |

During development, the ESP32 can be powered through USB. External loads must remain within the available power supply capacity.

For portable operation, a suitable battery-based power supply and voltage regulation circuit are required.

The main ON/OFF switch must be rated for the total system current.

## 8. Common Ground

The ESP32-S3, buzzer switching circuit and motor switching circuit must share a common reference ground when using non-isolated control connections.

Ground connections must be secure to prevent unreliable switching and unstable operation.

## 9. System Operation

1. The main power supply is switched on.
2. The ESP32 initializes the GC2145 camera.
3. The ESP32 creates the ALERT-DRIVE Wi-Fi access point.
4. The Android application connects to the ESP32.
5. Camera frames are transmitted to the Android application.
6. The application processes the facial images and determines the driver monitoring state.
7. Alert commands are transmitted to the ESP32.
8. The ESP32 activates the appropriate visual, audible and tactile warnings.

## 10. Safety Precautions

- Disconnect power before modifying connections.
- Never apply 5V directly to an ESP32 GPIO pin.
- Never power motors directly from a GPIO pin.
- Verify MOSFET terminal assignments against the component datasheet.
- Use suitable current-limiting and gate pull-down resistors.
- Provide appropriate inductive load protection.
- Do not exceed the rated voltage of the vibration motors.
- Verify power supply current capacity.
- Secure loose wires and exposed electrical connections.
- Perform prototype testing in a stationary environment.

## 11. Validation Status

This wiring guide is based on the recovered AlertDrive firmware and the current hardware component selection.

The following items require final verification:

- C2N7000 MOSFET terminal assignments.
- MOSFET conduction at 3.3V gate voltage.
- XY-GMOS module minimum operating voltage.
- Vibration motor voltage regulation.
- Protection diode placement.
- Final battery and power supply configuration.
- Complete integrated hardware connections.

This document describes the intended wiring architecture and must not be treated as a fully validated circuit schematic.

## 12. Disclaimer

AlertDrive is an academic prototype and is not a certified automotive safety system.