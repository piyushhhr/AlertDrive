\# AlertDrive ESP32-S3 Firmware



\## Overview



The ESP32-S3 firmware handles camera initialization, local wireless communication with the Android application, and hardware alert control.



The Android application performs drowsiness detection and sends alert commands to the ESP32.



\## Hardware Requirements



\- ESP32-S3 N16R8

\- GC2145 camera module

\- LED

\- 5V buzzer

\- Vibration motors

\- MOSFET switching circuitry

\- Suitable power supply



\## Arduino IDE Configuration



| Setting | Value |

|---|---|

| Board | ESP32S3 Dev Module |

| Flash Size | 16 MB |

| PSRAM | OPI PSRAM |

| Serial Baud Rate | 115200 |



\## GPIO Configuration



| Component | GPIO |

|---|---|

| LED | 38 |

| Buzzer | 14 |

| Vibration motor control | 47 |



\## Camera Configuration



\- Sensor: GC2145

\- Resolution: VGA (640 x 480)

\- Pixel format: RGB565

\- Software JPEG conversion

\- Frame buffer location: PSRAM



\## Network Configuration



\- Connection mode: Local Wi-Fi access point

\- SSID: ALERT-DRIVE

\- ESP32 IP address: 192.168.4.1

\- HTTP port: 80

\- Internet connection: Not required



\## HTTP Endpoints



| Endpoint | Description |

|---|---|

| GET / | Device information |

| GET /health | Camera and monitoring status |

| GET /frame | Capture and return a JPEG image |

| GET /alert?state=STATE | Update the hardware alert state |



\## Alert States



\- STANDBY

\- NORMAL

\- CLOSING

\- WARNING

\- DROWSY

\- NO\_FACE

\- CONNECTION\_LOST



The communication watchdog detects interruptions after five seconds during active monitoring.



\## Installation



1\. Install Arduino IDE.

2\. Install the ESP32 board package.

3\. Open AlertDrive/AlertDrive.ino.

4\. Configure the board settings.

5\. Verify hardware connections and power requirements.

6\. Compile the firmware.

7\. Upload the firmware to the ESP32-S3.

8\. Connect the Android application to the ESP32 access point.



\## Firmware Status



Recovered Android-compatible prototype firmware. Exact equivalence to the currently flashed firmware and complete hardware validation remain pending.



\## Safety Notice



AlertDrive is an academic prototype and is not a certified automotive safety device.

