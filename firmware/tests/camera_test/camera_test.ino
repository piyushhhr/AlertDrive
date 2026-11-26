
/*
 * AlertDrive - GC2145 Camera Component Test
 *
 * Board: ESP32-S3 N16R8
 * Camera: GC2145
 * Resolution: VGA (640 x 480)
 * Pixel format: RGB565
 *
 * Purpose:
 * - Verify camera initialization
 * - Verify frame capture
 * - Verify software JPEG conversion
 * - Monitor camera performance
 */

#include "esp_camera.h"
#include "img_converters.h"

// Camera pin configuration
#define PWDN_GPIO_NUM  -1
#define RESET_GPIO_NUM -1
#define XCLK_GPIO_NUM  15
#define SIOD_GPIO_NUM  4
#define SIOC_GPIO_NUM  5

#define Y2_GPIO_NUM    11
#define Y3_GPIO_NUM    9
#define Y4_GPIO_NUM    8
#define Y5_GPIO_NUM    10
#define Y6_GPIO_NUM    12
#define Y7_GPIO_NUM    18
#define Y8_GPIO_NUM    17
#define Y9_GPIO_NUM    16

#define VSYNC_GPIO_NUM 6
#define HREF_GPIO_NUM  7
#define PCLK_GPIO_NUM  13

bool cameraReady = false;
unsigned long frameCount = 0;

void setup() {
  Serial0.begin(115200);
  delay(1000);

  Serial0.println("AlertDrive - Camera Test");

  if (!psramFound()) {
    Serial0.println("ERROR: PSRAM not detected");
    return;
  }

  camera_config_t config = {};

  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;

  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;

  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;

  config.pin_sccb_sda = SIOD_GPIO_NUM;
  config.pin_sccb_scl = SIOC_GPIO_NUM;

  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;

  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_RGB565;
  config.frame_size = FRAMESIZE_VGA;

  config.jpeg_quality = 12;
  config.fb_count = 1;
  config.fb_location = CAMERA_FB_IN_PSRAM;
  config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;

  esp_err_t err = esp_camera_init(&config);

  if (err != ESP_OK) {
    Serial0.printf(
      "Camera initialization failed: 0x%x\n",
      err
    );
    return;
  }

  cameraReady = true;

  Serial0.println("Camera initialized successfully");

  sensor_t *sensor = esp_camera_sensor_get();

  if (sensor != nullptr) {
    Serial0.printf(
      "Camera PID: 0x%04X\n",
      sensor->id.PID
    );
  }
}

void loop() {

  if (!cameraReady) {
    delay(1000);
    return;
  }

  unsigned long startTime = millis();

  camera_fb_t *fb = esp_camera_fb_get();

  if (!fb) {
    Serial0.println("ERROR: Frame capture failed");
    delay(1000);
    return;
  }

  frameCount++;

  Serial0.printf(
    "Frame %lu | Resolution: %u x %u | Raw size: %u bytes\n",
    frameCount,
    (unsigned int)fb->width,
    (unsigned int)fb->height,
    (unsigned int)fb->len
  );

  uint8_t *jpegBuffer = nullptr;
  size_t jpegLength = 0;

  bool converted = frame2jpg(
    fb,
    80,
    &jpegBuffer,
    &jpegLength
  );

  if (converted) {
    Serial0.printf(
      "JPEG conversion successful: %u bytes\n",
      (unsigned int)jpegLength
    );
  } else {
    Serial0.println("ERROR: JPEG conversion failed");
  }

  free(jpegBuffer);
  esp_camera_fb_return(fb);

  Serial0.printf(
    "Processing time: %lu ms\n",
    millis() - startTime
  );

  Serial0.println("--------------------------");

  delay(1000);
}
