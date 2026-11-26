# AlertDrive – Power Supply Configuration

## 1. Overview

The AlertDrive system requires a stable power supply to operate the ESP32-S3 microcontroller, GC2145 camera, LED, buzzer and vibration motors.

The Android smartphone performs drowsiness detection using its own processing capabilities and power supply.

The system is designed to operate without a laptop, internet connection or external Wi-Fi router during normal Android-based operation.

## 2. Component Power Requirements

| Component | Operating Voltage | Power Source |
|---|---|---|
| ESP32-S3 N16R8 | Board-compatible USB 5V input | USB or regulated power supply |
| GC2145 camera | Board-specific | ESP32 camera interface |
| LED | 3.3V GPIO logic | GPIO38 through a resistor |
| Buzzer | 5V DC | Regulated 5V supply |
| C2N7000 MOSFET | 3.3V gate control | GPIO14 |
| Vibration motors | 3V DC | Suitable regulated 3V supply |
| XY-GMOS MOSFET module | According to module specifications | Compatible regulated supply |
| Android smartphone | Device-specific | Internal battery or compatible charger |

## 3. Development Power Configuration

During development, the ESP32-S3 can receive power through its USB connection to a laptop.

The USB connection is used for:

- Firmware uploading.
- Serial monitoring.
- Hardware debugging.
- Prototype power supply.

The available USB current must be sufficient for the connected load.

Additional regulated power supplies may be required for the buzzer and vibration motors.

## 4. Portable Power Configuration

The intended final product should operate independently of a laptop.

Possible power supply options include:

- USB power bank.
- Protected rechargeable battery with suitable voltage regulation.
- Regulated external USB power adapter.

The final power source must provide sufficient current for the ESP32-S3, camera, buzzer and vibration motors.

The Android smartphone can operate using its internal battery.

## 5. Buzzer Power Circuit

The buzzer requires a regulated 5V supply and is controlled using the C2N7000 N-channel MOSFET.

| Parameter | Configuration |
|---|---|
| Buzzer rated voltage | 5V |
| Control GPIO | GPIO14 |
| Switching device | C2N7000 |
| Gate control voltage | 3.3V |
| Switching method | Low-side switching |

The ESP32 GPIO provides the control signal and does not directly supply the buzzer current.

The MOSFET's conduction characteristics and current handling capability must be verified at the available gate voltage.

## 6. Vibration Motor Power Circuit

The coin vibration motors are rated for 3V DC operation.

| Parameter | Configuration |
|---|---|
| Motor rated voltage | 3V |
| Control GPIO | GPIO47 |
| Switching device | XY-GMOS MOSFET module |
| Control mode | Active-HIGH |

The XY-GMOS module controls the electrical supply to the vibration motors.

The module does not provide voltage regulation. Its minimum operating voltage must be verified before selecting the motor supply arrangement.

The motors must not be connected directly to an unregulated 5V supply.

If multiple motors are connected in parallel, the power supply must support their combined startup and operating currents.

## 7. Power Distribution Architecture

The intended power distribution consists of:

- A suitable main power source.
- An ON/OFF switch.
- A compatible 5V supply for the ESP32 and buzzer.
- A suitable 3V supply arrangement for the vibration motors.
- Appropriate MOSFET switching circuits.
- Common ground for non-isolated control circuits.

The exact power distribution circuit remains subject to hardware verification.

## 8. ON/OFF Switch

The main ON/OFF switch is intended to control power delivery to the embedded hardware.

The switch must be rated for the system voltage and maximum expected current.

Switching off the embedded hardware does not automatically switch off the Android smartphone.

## 9. Protection Components

### 1N5817 Schottky Diode

Used where appropriate to protect switching circuits from inductive voltage transients.

### SMBJ5.0A TVS Diode

Available for transient voltage protection in a suitable power supply circuit.

The diode placement and suitability must be verified against the final circuit design.

### Gate Pull-Down Resistors

Used where required to prevent unintended MOSFET activation during startup or when a control signal is disconnected.

### Voltage Regulation

Suitable voltage regulation must be provided to ensure that each component receives its rated supply voltage.

## 10. Power Budget

The final power supply capacity must account for:

- ESP32-S3 operating current.
- Wi-Fi transmission current peaks.
- Camera operating current.
- Buzzer operating current.
- Combined vibration motor startup current.
- Voltage regulator losses.
- Appropriate design margin.

The total power consumption and estimated battery runtime must be determined through actual measurements.

## 11. Electrical Safety Precautions

- Never apply 5V directly to an ESP32 GPIO pin.
- Never power the vibration motors directly from GPIO pins.
- Do not exceed component voltage ratings.
- Verify supply polarity before connecting components.
- Use suitable battery protection and voltage regulation.
- Avoid connecting multiple power sources without verifying the board's power distribution circuit.
- Provide suitable protection against inductive voltage transients.
- Disconnect power before modifying the hardware.
- Prevent short circuits and exposed electrical connections.

## 12. Development Status

| Item | Status |
|---|---|
| USB-powered development | Available |
| ESP32 local Wi-Fi operation | Implemented in recovered firmware |
| Android-based processing | Application available |
| Final portable power source | Pending verification |
| Motor supply regulation | Pending verification |
| Total current consumption | Pending measurement |
| Battery runtime | Pending measurement |
| Complete power circuit validation | Pending |

## 13. Disclaimer

AlertDrive is an academic prototype and is not a certified automotive safety system.