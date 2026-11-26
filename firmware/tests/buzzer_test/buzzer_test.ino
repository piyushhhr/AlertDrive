
/*
 * AlertDrive - Buzzer Component Test
 * Board: ESP32-S3 N16R8
 * Buzzer: 5V
 * Driver: XY-GMOS MOSFET Module
 * Control GPIO: 14
 */

#define BUZZER_PIN 14

void setup() {
  Serial0.begin(115200);

  digitalWrite(BUZZER_PIN, LOW);
  pinMode(BUZZER_PIN, OUTPUT);

  Serial0.println("AlertDrive Buzzer Test Started");
}

void loop() {
  digitalWrite(BUZZER_PIN, HIGH);
  Serial0.println("Buzzer ON");
  delay(1000);

  digitalWrite(BUZZER_PIN, LOW);
  Serial0.println("Buzzer OFF");
  delay(1000);
}
