
/*
 * AlertDrive - Buzzer Component Test
 *
 * Board: ESP32-S3 N16R8
 * Buzzer: 5V
 * Switching device: C2N7000 N-channel MOSFET
 * Control GPIO: 14
 *
 * Purpose:
 * Verify the buzzer switching operation.
 *
 * Expected behaviour:
 * Buzzer ON for 1 second.
 * Buzzer OFF for 1 second.
 */

#define BUZZER_PIN 14

void setup() {
  Serial0.begin(115200);

  digitalWrite(BUZZER_PIN, LOW);
  pinMode(BUZZER_PIN, OUTPUT);

  Serial0.println("AlertDrive - Buzzer Test");
  Serial0.println("GPIO: 14");
  Serial0.println("MOSFET: C2N7000");
}

void loop() {
  digitalWrite(BUZZER_PIN, HIGH);
  Serial0.println("Buzzer ON");
  delay(1000);

  digitalWrite(BUZZER_PIN, LOW);
  Serial0.println("Buzzer OFF");
  delay(1000);
}
