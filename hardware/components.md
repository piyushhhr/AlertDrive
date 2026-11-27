# AlertDrive – Hardware Components

## 1. Bill of Materials (BOM)

| S.No. | Component | Specification | Quantity |
|---|---|---|---|
| 1 | Microcontroller | ESP32-S3 N16R8, 16 MB Flash, 8 MB PSRAM | 1 |
| 2 | Camera module | GC2145 | 1 |
| 3 | Android smartphone | Drowsiness detection and processing | 1 |
| 4 | LED | Visual alert indicator | 1 |
| 5 | Buzzer | 5V buzzer | 1 |
| 6 | Coin vibration motor | 3V DC | As installed |
| 7 | N-channel MOSFET | C2N7000 | 1 |
| 8 | MOSFET switching module | XY-GMOS | 1 |
| 9 | Schottky diode | 1N5817 | 2 |
| 10 | Resistors | Current limiting and MOSFET gate control | As required |
| 11 | Connecting wires | Hardware interconnections | As required |
| 12 | Power supply | Suitable regulated supply | 1 |
| 13 | USB cable | Programming and prototype power | 1 |
| 14 | Enclosure | Product housing | 1 |

## 2. Component Functions

### ESP32-S3 N16R8

The ESP32-S3 acts as the main embedded controller. It manages camera initialization, image transmission, local Wi-Fi communication and hardware alert mechanisms.

### GC2145 Camera

Captures facial images of the driver. The ESP32 converts the captured RGB565 frames into JPEG images and transmits them to the Android application.

### Android Smartphone

Processes the camera frames, detects signs of driver drowsiness and sends appropriate alert commands to the ESP32 through a local Wi-Fi connection.

### LED

### LED

Provides visual indications of the driver monitoring state.

- Control GPIO: GPIO38
- Current-limiting resistor: 200 Ω
- Control logic: Active-HIGH
- LED colour: Red

### 5V Buzzer

Provides audible warnings when signs of driver fatigue or communication failure are detected. It is controlled using GPIO14 through the C2N7000 MOSFET.


### C2N7000 N-Channel MOSFET

Used as a low-side switching device for the 5V buzzer.

- Control GPIO: GPIO14
- Gate control voltage: 3.3V
- Gate resistor: 200 Ω
- Gate pull-down resistor: 10 kΩ
- Load: 5V buzzer
- Protection diode: 1N5817

The gate pull-down resistor maintains the MOSFET in the OFF state when the control signal is floating.

The MOSFET terminal configuration and suitability for the buzzer current at 3.3V gate voltage must be verified.

### 3V Coin Vibration Motors

Provide tactile feedback during drowsiness alerts. The motors operate from a suitable 3V supply and are controlled through the XY-GMOS MOSFET module.

### XY-GMOS MOSFET Module

Controls the vibration motors using the GPIO47 output of the ESP32-S3.

- Control signal: GPIO47
- Load: 3V coin vibration motors
- Control mode: Active-HIGH, as configured in the firmware

The module does not regulate voltage. Its minimum operating voltage and compatibility with the motor supply arrangement must be verified.

### LM2596 DC-DC Buck Converter

Used to provide a reduced supply voltage for the two 3V coin vibration motors.

- Converter type: Step-down DC-DC converter
- Intended input voltage: 5V DC
- Required output voltage: 3V DC
- Connected load: Two parallel vibration motors
- Output voltage adjustment: Onboard potentiometer

The output voltage must be measured and adjusted before connecting the vibration motors.

Adequate input voltage headroom and output current capacity must be verified under load.

### 1N5817 Schottky Diode

Two 1N5817 Schottky diodes are included in the circuit diagram for inductive load protection.

- Quantity: 2
- Maximum average forward current: 1A
- Maximum repetitive reverse voltage: 20V
- Application: Flyback protection for the buzzer and vibration motor circuits

The cathode must face the positive supply terminal, and the anode must face the negative switched terminal.

### SMBJ5.0A TVS Diode

Available for transient voltage protection in a suitable power supply circuit. Its final placement depends on the validated circuit design.

### Resistors

Used for LED current limiting, MOSFET gate control and pull-down protection where required.

### Power Supply

Provides electrical power to the ESP32-S3 and external alert components.


### Enclosure

Provides physical mounting and protection for the electronic components.

## 3. Hardware Control Summary

| Component | GPIO | Control Circuit | Load Voltage |
|---|---|---|---|
| LED | GPIO38 | Current-limiting resistor | 3.3V GPIO output |
| Buzzer | GPIO14 | C2N7000 MOSFET | 5V |
| Vibration motors | GPIO47 | XY-GMOS MOSFET module | 3V |
| GC2145 camera | Dedicated camera GPIOs | Camera interface | Board-specific |

## 4. Operating Architecture

The ESP32-S3 captures camera frames and transmits them to the Android application over a local Wi-Fi access point.

The Android application performs drowsiness detection and communicates the detected state to the ESP32.

The ESP32 activates the LED, buzzer and vibration motors according to the received alert state.

An internet connection, external Wi-Fi router or laptop is not required for the intended Android-based operation.

## 5. Electrical Precautions

- ESP32-S3 GPIO pins operate at 3.3V logic levels.
- Do not connect 5V directly to any GPIO pin.
- Do not power the buzzer or vibration motors directly from GPIO pins.
- Use a suitable current-limiting resistor for the LED.
- Supply the vibration motors with their rated operating voltage.
- Verify MOSFET pin assignments before making connections.
- Use appropriate protection for inductive loads.
- Disconnect the power supply before modifying connections.

## 6. Development Status

The component selection and GPIO assignments are based on the current AlertDrive prototype.

Final component quantities, MOSFET operating conditions, power supply ratings and protection circuitry must be verified against the completed physical prototype.

## 7. Disclaimer

AlertDrive is an academic prototype and is not a certified automotive safety system.