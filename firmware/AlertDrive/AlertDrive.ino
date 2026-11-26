
#include <WiFi.h>
#include <WebServer.h>
#include "esp_camera.h"
#include "img_converters.h"

// ==================================================
// ALERTDRIVE WIFI CONFIGURATION
// ==================================================

const char* AP_SSID = "ALERT-DRIVE";
const char* AP_PASSWORD = "alertdrive8";

IPAddress localIP(192, 168, 4, 1);
IPAddress gateway(192, 168, 4, 1);
IPAddress subnet(255, 255, 255, 0);

WebServer server(80);

// ==================================================
// ALERTDRIVE HARDWARE PINS
// ==================================================

#define LED_PIN     38
#define BUZZER_PIN  14
#define MOTOR_PIN   47

// ==================================================
// GC2145 CAMERA PIN CONFIGURATION
// ==================================================

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

// ==================================================
// SYSTEM VARIABLES
// ==================================================

String currentState = "STANDBY";

bool cameraReady = false;
bool wifiReady = false;
bool monitoringActive = false;

unsigned long lastAlertReceived = 0;

const unsigned long WATCHDOG_TIMEOUT = 5000;

// ==================================================
// HARDWARE CONTROL
// ==================================================

void allOff() {

  digitalWrite(LED_PIN, LOW);
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(MOTOR_PIN, LOW);
}

void setOutputs(
  bool led,
  bool buzzer,
  bool motor
) {

  digitalWrite(
    LED_PIN,
    led ? HIGH : LOW
  );

  digitalWrite(
    BUZZER_PIN,
    buzzer ? HIGH : LOW
  );

  digitalWrite(
    MOTOR_PIN,
    motor ? HIGH : LOW
  );
}

// ==================================================
// CAMERA INITIALIZATION
// ==================================================

bool initializeCamera() {

  camera_config_t config = {};

  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;

  config.pin_d0 = CAM_D0;
  config.pin_d1 = CAM_D1;
  config.pin_d2 = CAM_D2;
  config.pin_d3 = CAM_D3;
  config.pin_d4 = CAM_D4;
  config.pin_d5 = CAM_D5;
  config.pin_d6 = CAM_D6;
  config.pin_d7 = CAM_D7;

  config.pin_xclk = CAM_XCLK;
  config.pin_pclk = CAM_PCLK;
  config.pin_vsync = CAM_VSYNC;
  config.pin_href = CAM_HREF;

  config.pin_sccb_sda = CAM_SIOD;
  config.pin_sccb_scl = CAM_SIOC;

  config.pin_pwdn = CAM_PWDN;
  config.pin_reset = CAM_RESET;

  config.xclk_freq_hz = 20000000;

  // GC2145 raw image format

  config.pixel_format = PIXFORMAT_RGB565;

  config.frame_size = FRAMESIZE_VGA;

  config.jpeg_quality = 12;

  config.fb_count = 1;

  config.fb_location = CAMERA_FB_IN_PSRAM;

  config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;

  esp_err_t result = esp_camera_init(&config);

  if (result != ESP_OK) {

    Serial0.printf(
      "Camera initialization failed: 0x%x\n",
      result
    );

    return false;
  }

  sensor_t* sensor = esp_camera_sensor_get();

  if (sensor != nullptr) {

    Serial0.printf(
      "Camera PID: 0x%04X\n",
      sensor->id.PID
    );
  }

  Serial0.println(
    "GC2145 camera initialized successfully"
  );

  return true;
}

// ==================================================
// WIFI ACCESS POINT INITIALIZATION
// ==================================================

bool initializeWiFi() {

  WiFi.mode(WIFI_AP);

  if (!WiFi.softAPConfig(
        localIP,
        gateway,
        subnet
      )) {

    Serial0.println(
      "WiFi configuration failed"
    );

    return false;
  }

  if (!WiFi.softAP(
        AP_SSID,
        AP_PASSWORD
      )) {

    Serial0.println(
      "WiFi access point failed"
    );

    return false;
  }

  Serial0.println("========================");

  Serial0.println("AlertDrive WiFi started");

  Serial0.print("SSID: ");
  Serial0.println(AP_SSID);

  Serial0.print("IP Address: ");
  Serial0.println(WiFi.softAPIP());

  Serial0.println("========================");

  return true;
}

// ==================================================
// ROOT ENDPOINT
// ==================================================

void handleRoot() {

  String response = "{";

  response += "\"device\":\"AlertDrive\",";
  response += "\"camera\":\"GC2145\",";
  response += "\"ip\":\"192.168.4.1\",";
  response += "\"status\":\"ONLINE\"";

  response += "}";

  server.send(
    200,
    "application/json",
    response
  );
}

// ==================================================
// HEALTH ENDPOINT
// ==================================================

void handleHealth() {

  String response = "{";

  response += "\"device\":\"AlertDrive\",";

  response += "\"camera_ready\":";

  response += cameraReady ? "true" : "false";

  response += ",\"state\":\"";
  response += currentState;
  response += "\"";

  response += ",\"monitoring\":";

  response += monitoringActive ? "true" : "false";

  response += "}";

  server.send(
    cameraReady ? 200 : 503,
    "application/json",
    response
  );
}

// ==================================================
// CAMERA FRAME ENDPOINT
// ==================================================

void handleFrame() {

  if (!cameraReady) {

    server.send(
      503,
      "text/plain",
      "Camera unavailable"
    );

    return;
  }

  camera_fb_t* fb = esp_camera_fb_get();

  if (fb == nullptr) {

    server.send(
      500,
      "text/plain",
      "Frame capture failed"
    );

    return;
  }

  uint8_t* jpegBuffer = nullptr;

  size_t jpegLength = 0;

  // Convert GC2145 RGB565 image into JPEG

  bool converted = frame2jpg(
    fb,
    80,
    &jpegBuffer,
    &jpegLength
  );

  esp_camera_fb_return(fb);

  if (
    !converted ||
    jpegBuffer == nullptr ||
    jpegLength == 0
  ) {

    if (jpegBuffer != nullptr) {

      free(jpegBuffer);
    }

    server.send(
      500,
      "text/plain",
      "JPEG conversion failed"
    );

    return;
  }

  server.sendHeader(
    "Cache-Control",
    "no-store, no-cache"
  );

  server.setContentLength(jpegLength);

  server.send(
    200,
    "image/jpeg",
    ""
  );

  server.sendContent(
    reinterpret_cast<const char*>(jpegBuffer),
    jpegLength
  );

  free(jpegBuffer);
}

// ==================================================
// ALERT COMMAND ENDPOINT
// ==================================================

void handleAlert() {

  if (!server.hasArg("state")) {

    server.send(
      400,
      "text/plain",
      "Missing state parameter"
    );

    return;
  }

  String requestedState = server.arg("state");

  requestedState.trim();

  requestedState.toUpperCase();

  bool validState =

    requestedState == "NORMAL" ||
    requestedState == "CLOSING" ||
    requestedState == "WARNING" ||
    requestedState == "DROWSY" ||
    requestedState == "NO_FACE" ||
    requestedState == "STANDBY" ||
    requestedState == "STOP";

  if (!validState) {

    server.send(
      400,
      "text/plain",
      "Invalid alert state"
    );

    return;
  }

  if (requestedState != currentState) {

    Serial0.print("AlertDrive State: ");

    Serial0.println(requestedState);
  }

  currentState = requestedState;

  lastAlertReceived = millis();

  monitoringActive =

    requestedState != "STOP" &&
    requestedState != "STANDBY";

  if (!monitoringActive) {

    currentState = "STANDBY";

    allOff();
  }

  server.send(
    200,
    "text/plain",
    "OK"
  );
}

// ==================================================
// AUTOMATIC ALERT PATTERNS
// ==================================================

void updateAlerts() {

  unsigned long now = millis();

  // ----------------------------------------------
  // STANDBY
  // All components OFF
  // ----------------------------------------------

  if (currentState == "STANDBY") {

    allOff();
  }

  // ----------------------------------------------
  // NORMAL
  // Driver awake, all components OFF
  // ----------------------------------------------

  else if (currentState == "NORMAL") {

    allOff();
  }

  // ----------------------------------------------
  // CLOSING
  // LED blinks every 1 second
  // ----------------------------------------------

  else if (currentState == "CLOSING") {

    bool ledPulse =

      (now % 1000) < 500;

    setOutputs(
      ledPulse,
      false,
      false
    );
  }

  // ----------------------------------------------
  // WARNING
  // LED continuously ON
  // Buzzer pulses
  // Motors OFF
  // ----------------------------------------------

  else if (currentState == "WARNING") {

    bool buzzerPulse =

      (now % 1000) < 500;

    setOutputs(
      true,
      buzzerPulse,
      false
    );
  }

  // ----------------------------------------------
  // DROWSY
  // LED continuously ON
  // Buzzer and both motors pulse together
  // ----------------------------------------------

  else if (currentState == "DROWSY") {

    bool alertPulse =

      (now % 600) < 300;

    setOutputs(
      true,
      alertPulse,
      alertPulse
    );
  }

  // ----------------------------------------------
  // NO FACE
  // LED flashes once every 2 seconds
  // LED ON for 250 ms
  // Buzzer and motors OFF
  // ----------------------------------------------

  else if (currentState == "NO_FACE") {

    bool ledPulse =

      (now % 2000) < 250;

    setOutputs(
      ledPulse,
      false,
      false
    );
  }

  // ----------------------------------------------
  // CONNECTION LOST
  // LED rapidly blinks
  // 100 ms ON, 100 ms OFF
  // Buzzer beeps every 3 seconds
  // Motors OFF
  // ----------------------------------------------

  else if (currentState == "CONNECTION_LOST") {

    bool ledPulse =

      (now % 200) < 100;

    bool buzzerPulse =

      (now % 3000) < 200;

    setOutputs(
      ledPulse,
      buzzerPulse,
      false
    );
  }

  // Unknown state protection

  else {

    allOff();
  }
}

// ==================================================
// COMMUNICATION WATCHDOG
// ==================================================

void checkWatchdog() {

  if (!monitoringActive) {

    return;
  }

  unsigned long elapsed =

    millis() - lastAlertReceived;

  if (elapsed >= WATCHDOG_TIMEOUT) {

    if (currentState != "CONNECTION_LOST") {

      currentState = "CONNECTION_LOST";

      Serial0.println(
        "WARNING: Mobile communication lost"
      );
    }
  }
}

// ==================================================
// HTTP SERVER INITIALIZATION
// ==================================================

void initializeServer() {

  server.on(
    "/",
    HTTP_GET,
    handleRoot
  );

  server.on(
    "/health",
    HTTP_GET,
    handleHealth
  );

  server.on(
    "/frame",
    HTTP_GET,
    handleFrame
  );

  server.on(
    "/alert",
    HTTP_GET,
    handleAlert
  );

  server.onNotFound([]() {

    server.send(
      404,
      "text/plain",
      "Endpoint not found"
    );
  });

  server.begin();

  Serial0.println(
    "HTTP server started"
  );

  Serial0.println(
    "Waiting for mobile application"
  );
}

// ==================================================
// SETUP
// ==================================================

void setup() {

  Serial0.begin(115200);

  // Set output latches LOW before enabling outputs

  digitalWrite(LED_PIN, LOW);
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(MOTOR_PIN, LOW);

  pinMode(LED_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(MOTOR_PIN, OUTPUT);

  allOff();

  delay(1000);

  Serial0.println();

  Serial0.println("========================");

  Serial0.println("AlertDrive Starting");

  Serial0.println("========================");

  // Initialize camera

  cameraReady = initializeCamera();

  // Initialize local WiFi

  wifiReady = initializeWiFi();

  if (!wifiReady) {

    Serial0.println(
      "System WiFi initialization error"
    );

    allOff();

    return;
  }

  // Start HTTP server

  initializeServer();

  // Startup confirmation LED

  if (cameraReady) {

    digitalWrite(LED_PIN, HIGH);

    delay(300);

    digitalWrite(LED_PIN, LOW);

    Serial0.println(
      "AlertDrive is ready"
    );

  } else {

    Serial0.println(
      "AlertDrive camera error"
    );
  }
}

// ==================================================
// MAIN LOOP
// ==================================================

void loop() {

  if (!wifiReady) {

    allOff();

    delay(100);

    return;
  }

  // Handle communication with Android application

  server.handleClient();

  // Detect interrupted communication

  checkWatchdog();

  // Update LED, buzzer and vibration motors

  updateAlerts();

  delay(1);
}
