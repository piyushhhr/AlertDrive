
/*
 * AlertDrive - LED Component Test
 * Board: ESP32-S3 N16R8
 * LED GPIO: 38
 * Purpose: Verify LED operation
 */

#define LED_PIN 38

void setup() {
  Serial0.begin(115200);

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

  Serial0.println("AlertDrive LED Test Started");
}

void loop() {
  digitalWrite(LED_PIN, HIGH);
  Serial0.println("LED ON");
  delay(1000);

  digitalWrite(LED_PIN, LOW);
  Serial0.println("LED OFF");
  delay(1000);
}
