
/*
 * AlertDrive - Communication Test
 *
 * Board: ESP32-S3 N16R8
 * Communication: Local Wi-Fi and HTTP
 *
 * Purpose:
 * - Verify Wi-Fi access point initialization
 * - Verify HTTP server operation
 * - Verify alert command reception
 * - Verify communication watchdog
 *
 * No physical alert components are activated.
 */

#include <WiFi.h>
#include <WebServer.h>

// Wi-Fi configuration
const char* AP_SSID = "ALERT-DRIVE";
const char* AP_PASSWORD = "alertdrive8";

IPAddress localIP(192, 168, 4, 1);
IPAddress gateway(192, 168, 4, 1);
IPAddress subnet(255, 255, 255, 0);

WebServer server(80);

// System variables
String currentState = "STANDBY";

bool monitoringActive = false;
bool wifiReady = false;

unsigned long lastAlertReceived = 0;
const unsigned long WATCHDOG_TIMEOUT = 5000;

// Root endpoint
void handleRoot() {
  server.send(
    200,
    "text/plain",
    "AlertDrive Communication Test Online"
  );
}

// Health endpoint
void handleHealth() {

  String response = "{";

  response += "\"device\":\"AlertDrive\",";
  response += "\"camera_ready\":false,";
  response += "\"state\":\"";
  response += currentState;
  response += "\",";
  response += "\"monitoring\":";
  response += monitoringActive ? "true" : "false";
  response += "}";

  server.send(200, "application/json", response);
}

// Alert endpoint
void handleAlert() {

  if (!server.hasArg("state")) {
    server.send(400, "text/plain", "Missing state");
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
    server.send(400, "text/plain", "Invalid state");
    return;
  }

  currentState = requestedState;
  lastAlertReceived = millis();

  monitoringActive =
    requestedState != "STOP" &&
    requestedState != "STANDBY";

  if (!monitoringActive) {
    currentState = "STANDBY";
  }

  Serial0.print("Received state: ");
  Serial0.println(currentState);

  server.send(200, "text/plain", "OK");
}

// Communication watchdog
void checkWatchdog() {

  if (!monitoringActive) {
    return;
  }

  if (millis() - lastAlertReceived >= WATCHDOG_TIMEOUT) {

    if (currentState != "CONNECTION_LOST") {

      currentState = "CONNECTION_LOST";

      Serial0.println(
        "WARNING: Communication lost"
      );
    }
  }
}

void setup() {

  Serial0.begin(115200);
  delay(1000);

  Serial0.println("AlertDrive Communication Test");

  // Keep alert output pins LOW
  digitalWrite(38, LOW);
  digitalWrite(14, LOW);
  digitalWrite(47, LOW);

  pinMode(38, OUTPUT);
  pinMode(14, OUTPUT);
  pinMode(47, OUTPUT);

  // Initialize local Wi-Fi
  WiFi.mode(WIFI_AP);

  if (!WiFi.softAPConfig(localIP, gateway, subnet)) {
    Serial0.println("Wi-Fi configuration failed");
    return;
  }

  if (!WiFi.softAP(AP_SSID, AP_PASSWORD)) {
    Serial0.println("Wi-Fi initialization failed");
    return;
  }

  wifiReady = true;

  Serial0.println("Wi-Fi initialized successfully");

  Serial0.print("IP Address: ");
  Serial0.println(WiFi.softAPIP());

  // Register HTTP endpoints
  server.on("/", HTTP_GET, handleRoot);
  server.on("/health", HTTP_GET, handleHealth);
  server.on("/alert", HTTP_GET, handleAlert);

  server.onNotFound([]() {
    server.send(404, "text/plain", "Endpoint not found");
  });

  server.begin();

  Serial0.println("HTTP server started");
  Serial0.println("Waiting for communication");
}

void loop() {

  if (!wifiReady) {
    delay(100);
    return;
  }

  server.handleClient();
  checkWatchdog();

  delay(1);
}
