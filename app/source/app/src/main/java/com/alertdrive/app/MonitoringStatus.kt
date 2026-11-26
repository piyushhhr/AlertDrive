
package com.alertdrive.app

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log

object MonitoringStatus {

    private const val TAG = "AlertDrive"

    data class Snapshot(
        val running: Boolean = false,
        val connected: Boolean = false,
        val state: String = "STANDBY",
        val ear: Double? = null,
        val updatedAt: Long = 0L
    )

    data class PreviewFrame(
        val bitmap: Bitmap,
        val capturedAt: Long
    )

    @Volatile
    var current = Snapshot()
        private set

    @Volatile
    var previewFrame: PreviewFrame? = null
        private set

    @Volatile
    var previewEnabled = false
        private set

    private var generation = 0L

    @Synchronized
    fun begin(): Long {

        generation++

        previewFrame = null

        current = Snapshot(
            running = true,
            connected = false,
            state = "CONNECTING",
            updatedAt = SystemClock.elapsedRealtime()
        )

        return generation
    }

    @Synchronized
    fun setPreviewEnabled(enabled: Boolean) {

        if (previewEnabled != enabled) {
            Log.d(TAG, "PREVIEW ENABLED=$enabled")
        }

        previewEnabled = enabled

        if (!enabled) {
            previewFrame = null
        }
    }

    @Synchronized
    fun updatePreview(
        session: Long,
        bitmap: Bitmap,
        capturedAt: Long
    ) {

        if (
            session != generation ||
            !current.running ||
            !previewEnabled ||
            bitmap.isRecycled
        ) {

            Log.w(
                TAG,
                "PREVIEW REJECTED: " +
                        "sessionValid=${session == generation}, " +
                        "running=${current.running}, " +
                        "enabled=$previewEnabled, " +
                        "recycled=${bitmap.isRecycled}"
            )

            return
        }

        previewFrame = PreviewFrame(
            bitmap = bitmap,
            capturedAt = capturedAt
        )

        Log.d(
            TAG,
            "PREVIEW FRAME STORED: " +
                    "${bitmap.width}x${bitmap.height}"
        )
    }

    @Synchronized
    fun clearPreview(session: Long) {

        if (session != generation) return

        previewFrame = null
    }

    @Synchronized
    fun update(
        session: Long,
        connected: Boolean,
        state: String,
        ear: Double? = null
    ) {

        if (
            session != generation ||
            !current.running
        ) return

        current = Snapshot(
            running = true,
            connected = connected,
            state = state,
            ear = ear,
            updatedAt = SystemClock.elapsedRealtime()
        )

        if (
            !connected ||
            state == "CAMERA_ERROR" ||
            state == "CONNECTION_LOST" ||
            state == "MONITORING_INTERRUPTED" ||
            state == "ERROR"
        ) {

            previewFrame = null
        }
    }

    @Synchronized
    fun finish(
        session: Long,
        state: String = "STANDBY"
    ) {

        if (session != generation) return

        previewFrame = null

        current = Snapshot(
            running = false,
            connected = false,
            state = state,
            updatedAt = SystemClock.elapsedRealtime()
        )
    }
}
