
package com.alertdrive.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log

class DetectionService : Service() {

    companion object {

        private const val TAG = "AlertDrive"

        // ======================================
        // INDIVIDUAL EYE EAR THRESHOLDS
        // ======================================

        private const val LEFT_EAR_CLOSE = 0.22

        private const val RIGHT_EAR_CLOSE = 0.22

        private const val MEAN_EAR_CLOSE = 0.22

        // Confirmed eye reopening threshold
        private const val EAR_OPEN = 0.25

        // ======================================
        // DROWSINESS DETECTION DURATIONS
        // ======================================

        private const val CLOSING_TIME = 1000L

        private const val WARNING_TIME = 2000L

        private const val DROWSY_TIME = 3000L

        // Maximum acceptable gap between frames

        private const val MAX_FRAME_GAP = 1000L
    }

    @Volatile
    private var running = false

    @Volatile
    private var destroyed = false

    private var worker: Thread? = null

    private var wakeLock:
            PowerManager.WakeLock? = null

    private lateinit var notificationManager:
            NotificationManager

    private var lastNotification = ""

    private var session = 0L

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    // ==========================================
    // START MONITORING SERVICE
    // ==========================================

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (running) return START_NOT_STICKY

        destroyed = false

        lastNotification = ""

        notificationManager =
            getSystemService(
                NotificationManager::class.java
            )

        notificationManager.createNotificationChannel(

            NotificationChannel(
                "alertdrive",
                "AlertDrive Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        session = MonitoringStatus.begin()

        startForeground(
            1,
            createNotification(
                "Initializing monitoring"
            )
        )

        // ======================================
        // KEEP CPU ACTIVE DURING MONITORING
        // ======================================

        val power =
            getSystemService(
                POWER_SERVICE
            ) as PowerManager

        wakeLock = power.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AlertDrive:Monitoring"
        )

        wakeLock?.acquire()

        running = true

        val activeSession = session

        // ======================================
        // AI PROCESSING THREAD
        // ======================================

        worker = Thread {

            var detector: FaceDetector? = null

            var closedDuration = 0L

            var previousFrame = -1L

            var previousBothClosed = false

            var finalState = "STANDBY"

            // ==================================
            // RESET EYE CLOSURE TIMER
            // ==================================

            fun resetDetection() {

                closedDuration = 0L

                previousFrame = -1L

                previousBothClosed = false
            }

            try {

                ESP32Client.init(this)

                Log.d(
                    TAG,
                    "Loading AI model"
                )

                detector = FaceDetector(this)

                Log.d(
                    TAG,
                    "AI MODEL LOADED"
                )

                while (running) {

                    try {

                        // ==========================
                        // RECEIVE CAMERA FRAME
                        // ==========================

                        val bitmap =
                            ESP32Client.getFrame()

                        if (!running) {

                            if (
                                bitmap != null &&
                                !bitmap.isRecycled
                            ) {

                                bitmap.recycle()
                            }

                            break
                        }

                        // ==========================
                        // CAMERA FRAME UNAVAILABLE
                        // ==========================

                        if (bitmap == null) {

                            MonitoringStatus.clearPreview(
                                activeSession
                            )

                            resetDetection()

                            ESP32Client.sendAlert(
                                "NO_FACE"
                            )

                            publishStatus(
                                activeSession,
                                true,
                                "CAMERA_ERROR"
                            )

                            Thread.sleep(100)

                            continue
                        }

                        // Current frame timestamp

                        val now =
                            SystemClock.elapsedRealtime()

                        // ==========================
                        // FRAME INTERVAL
                        // ==========================

                        val frameGap =
                            if (previousFrame >= 0L) {

                                now - previousFrame

                            } else {

                                0L
                            }

                        // Do not count missing
                        // camera observations.

                        if (
                            frameGap > MAX_FRAME_GAP
                        ) {

                            Log.w(
                                TAG,
                                "Long frame gap: ${frameGap}ms"
                            )

                            resetDetection()
                        }

                        val validGap =
                            if (previousFrame >= 0L) {

                                now - previousFrame

                            } else {

                                0L
                            }

                        previousFrame = now

                        // ==========================
                        // LIVE CAMERA PREVIEW
                        // AND DUAL-EYE EAR
                        // ==========================

                        val eyeReadings = try {

                            // Copy preview bitmap
                            // BEFORE AI processing.

                            if (
                                running &&
                                MonitoringStatus.previewEnabled
                            ) {

                                try {

                                    val previewBitmap =
                                        bitmap.copy(
                                            Bitmap.Config.ARGB_8888,
                                            false
                                        )

                                    if (
                                        previewBitmap != null
                                    ) {

                                        MonitoringStatus.updatePreview(
                                            activeSession,
                                            previewBitmap,
                                            now
                                        )

                                        Log.d(
                                            TAG,
                                            "PREVIEW FRAME PUBLISHED: " +
                                                    "${previewBitmap.width}x" +
                                                    "${previewBitmap.height}"
                                        )

                                    } else {

                                        Log.w(
                                            TAG,
                                            "PREVIEW COPY RETURNED NULL"
                                        )
                                    }

                                } catch (e: Exception) {

                                    Log.e(
                                        TAG,
                                        "PREVIEW COPY FAILED",
                                        e
                                    )
                                }
                            }

                            // ======================
                            // DETECT BOTH EYES
                            // ======================

                            detector.getEyeEAR(
                                bitmap,
                                now
                            )

                        } finally {

                            if (!bitmap.isRecycled) {

                                bitmap.recycle()
                            }
                        }

                        // ==========================
                        // DUAL-EYE DROWSINESS LOGIC
                        // ==========================

                        val state: String

                        var meanEAR: Double? = null

                        if (
                            eyeReadings == null ||
                            !eyeReadings.isValid
                        ) {

                            // Face not detected
                            // or invalid eye readings.

                            resetDetection()

                            state = "NO_FACE"

                            Log.d(
                                TAG,
                                "NO VALID EYE LANDMARKS"
                            )

                        } else {

                            // ======================
                            // INDIVIDUAL EAR VALUES
                            // ======================

                            val leftEAR =
                                eyeReadings.left

                            val rightEAR =
                                eyeReadings.right

                            meanEAR =
                                eyeReadings.mean

                            // ======================
                            // INDIVIDUAL EYE STATUS
                            // ======================

                            val leftClosed =
                                leftEAR <
                                        LEFT_EAR_CLOSE

                            val rightClosed =
                                rightEAR <
                                        RIGHT_EAR_CLOSE

                            val meanClosed =
                                meanEAR <
                                        MEAN_EAR_CLOSE

                            // ======================
                            // BOTH EYES MUST BE CLOSED
                            // ======================

                            val bothEyesClosed =
                                leftClosed &&
                                        rightClosed &&
                                        meanClosed

                            // Confirmed open eye
                            // for diagnostic logging.

                            val eitherEyeOpen =
                                leftEAR >= EAR_OPEN ||
                                        rightEAR >= EAR_OPEN

                            // Exactly one eye meets
                            // the closure threshold.

                            val oneEyeClosed =
                                leftClosed !=
                                        rightClosed

                            // ======================
                            // ACCUMULATE CLOSURE TIME
                            // ======================

                            if (bothEyesClosed) {

                                // Count only intervals
                                // where BOTH consecutive
                                // observations show
                                // both eyes closed.

                                if (previousBothClosed) {

                                    closedDuration +=
                                        validGap

                                } else {

                                    closedDuration = 0L
                                }

                                previousBothClosed = true

                            } else {

                                // One or both eyes open,
                                // or uncertain closure:
                                // reset the entire timer.

                                closedDuration = 0L

                                previousBothClosed = false
                            }

                            // ======================
                            // DETERMINE ALERT LEVEL
                            // ======================

                            state = when {

                                !bothEyesClosed ->
                                    "NORMAL"

                                closedDuration >=
                                        DROWSY_TIME ->
                                    "DROWSY"

                                closedDuration >=
                                        WARNING_TIME ->
                                    "WARNING"

                                closedDuration >=
                                        CLOSING_TIME ->
                                    "CLOSING"

                                else ->
                                    "NORMAL"
                            }

                            // ======================
                            // INDIVIDUAL EAR DEBUG LOG
                            // ======================

                            Log.d(
                                TAG,

                                "LEFT_EAR=$leftEAR " +

                                        "RIGHT_EAR=$rightEAR " +

                                        "MEAN_EAR=$meanEAR " +

                                        "LEFT_CLOSED=$leftClosed " +

                                        "RIGHT_CLOSED=$rightClosed " +

                                        "BOTH_CLOSED=$bothEyesClosed " +

                                        "ONE_EYE_CLOSED=$oneEyeClosed " +

                                        "EITHER_EYE_OPEN=$eitherEyeOpen " +

                                        "DURATION=${closedDuration}ms " +

                                        "FRAME_GAP=${frameGap}ms " +

                                        "STATE=$state"
                            )
                        }

                        // ==========================
                        // SEND ALERT TO ESP32
                        // ==========================

                        if (running) {

                            ESP32Client.sendAlert(
                                state
                            )

                            publishStatus(
                                activeSession,
                                true,
                                state,
                                meanEAR
                            )
                        }

                        Thread.sleep(30)

                    } catch (
                        e: InterruptedException
                    ) {

                        break

                    } catch (e: Exception) {

                        resetDetection()

                        MonitoringStatus.clearPreview(
                            activeSession
                        )

                        Log.e(
                            TAG,
                            "Monitoring error",
                            e
                        )

                        if (running) {

                            publishStatus(
                                activeSession,
                                false,
                                "MONITORING_INTERRUPTED"
                            )

                            runCatching {

                                ESP32Client.sendAlert(
                                    "NO_FACE"
                                )
                            }

                            Thread.sleep(300)
                        }
                    }
                }

            } catch (
                e: InterruptedException
            ) {

                // Normal service interruption.

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Detection stopped",
                    e
                )

                finalState = "ERROR"

            } finally {

                // ==============================
                // RELEASE FACE DETECTOR
                // ==============================

                runCatching {

                    detector?.close()
                }

                running = false

                // Clear camera preview.

                MonitoringStatus.clearPreview(
                    activeSession
                )

                // ==============================
                // STOP HARDWARE ALERTS
                // ==============================

                if (
                    destroyed &&
                    finalState == "STANDBY"
                ) {

                    Thread.interrupted()

                    runCatching {

                        ESP32Client.sendAlert(
                            "STANDBY"
                        )
                    }
                }

                // ==============================
                // FINISH MONITORING SESSION
                // ==============================

                MonitoringStatus.finish(
                    activeSession,
                    finalState
                )

                Handler(
                    Looper.getMainLooper()
                ).post {

                    if (!destroyed) {

                        stopSelf()
                    }
                }
            }
        }

        worker?.start()

        return START_NOT_STICKY
    }

    // ==========================================
    // UPDATE APPLICATION STATUS
    // ==========================================

    private fun publishStatus(
        activeSession: Long,
        connected: Boolean,
        state: String,
        ear: Double? = null
    ) {

        if (!running) return

        MonitoringStatus.update(
            activeSession,
            connected,
            state,
            ear
        )

        val message = when (state) {

            "NORMAL" ->
                "Monitoring active"

            "CLOSING" ->
                "Eyes closing"

            "WARNING" ->
                "Drowsiness warning"

            "DROWSY" ->
                "Drowsiness detected"

            "NO_FACE" ->
                "Driver face not detected"

            "CAMERA_ERROR" ->
                "Camera frame unavailable"

            "CONNECTION_LOST" ->
                "Device connection lost"

            "MONITORING_INTERRUPTED" ->
                "Monitoring interrupted - retrying"

            "ERROR" ->
                "Monitoring failed"

            else ->
                "Connecting to AlertDrive"
        }

        if (
            message != lastNotification
        ) {

            lastNotification = message

            if (running && !destroyed) {

                notificationManager.notify(
                    1,
                    createNotification(
                        message
                    )
                )
            }
        }
    }

    // ==========================================
    // FOREGROUND SERVICE NOTIFICATION
    // ==========================================

    private fun createNotification(
        message: String
    ): Notification {

        val openApp = PendingIntent.getActivity(

            this,

            0,

            Intent(
                this,
                MainActivity::class.java
            ),

            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(
            this,
            "alertdrive"
        )
            .setContentTitle("AlertDrive")
            .setContentText(message)
            .setSmallIcon(
                android.R.drawable.ic_dialog_info
            )
            .setContentIntent(openApp)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    // ==========================================
    // SERVICE SHUTDOWN
    // ==========================================

    override fun onDestroy() {

        destroyed = true

        running = false

        worker?.interrupt()

        MonitoringStatus.clearPreview(
            session
        )

        if (
            MonitoringStatus.current.running
        ) {

            MonitoringStatus.finish(
                session
            )
        }

        wakeLock?.let {

            if (it.isHeld) {

                it.release()
            }
        }

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        super.onDestroy()
    }
}
