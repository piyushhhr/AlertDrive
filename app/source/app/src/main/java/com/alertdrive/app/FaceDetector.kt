
package com.alertdrive.app

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import kotlin.math.hypot

class FaceDetector(context: Context) {

    // ==========================================
    // INDIVIDUAL AND MEAN EAR VALUES
    // ==========================================

    data class EyeEAR(
        val left: Double,
        val right: Double
    ) {

        val mean: Double
            get() = (left + right) / 2.0

        val isValid: Boolean
            get() =
                left.isFinite() &&
                        right.isFinite() &&
                        left >= 0.0 &&
                        right >= 0.0
    }

    // ==========================================
    // MEDIAPIPE FACE LANDMARKER
    // ==========================================

    private val detector = FaceLandmarker.createFromOptions(
        context,

        FaceLandmarker.FaceLandmarkerOptions.builder()

            .setBaseOptions(
                BaseOptions.builder()
                    .setModelAssetPath(
                        "face_landmarker.task"
                    )
                    .build()
            )

            .setRunningMode(RunningMode.VIDEO)

            .setNumFaces(1)

            .build()
    )

    // ==========================================
    // EYE LANDMARKS
    // ==========================================

    private val rightEye = intArrayOf(
        33, 160, 158, 133, 153, 144
    )

    private val leftEye = intArrayOf(
        362, 385, 387, 263, 373, 380
    )

    // ==========================================
    // CALCULATE BOTH EYES' EAR
    // ==========================================

    fun getEyeEAR(
        bitmap: Bitmap,
        time: Long
    ): EyeEAR? {

        if (bitmap.isRecycled) return null

        val imageWidth = bitmap.width

        val imageHeight = bitmap.height

        val image =
            BitmapImageBuilder(bitmap).build()

        try {

            val result =
                detector.detectForVideo(
                    image,
                    time
                )

            if (result.faceLandmarks().isEmpty()) {

                return null
            }

            val points =
                result.faceLandmarks()[0]

            if (points.size <= 387) {

                return null
            }

            // Calculate pixel distance between
            // two facial landmarks.

            fun distance(
                a: Int,
                b: Int
            ): Double {

                val dx =
                    (points[a].x() -
                            points[b].x()) *
                            imageWidth

                val dy =
                    (points[a].y() -
                            points[b].y()) *
                            imageHeight

                return hypot(
                    dx.toDouble(),
                    dy.toDouble()
                )
            }

            // Calculate EAR for an individual eye.

            fun ear(
                eye: IntArray
            ): Double {

                val vertical1 =
                    distance(eye[1], eye[5])

                val vertical2 =
                    distance(eye[2], eye[4])

                val horizontal =
                    distance(eye[0], eye[3])

                if (
                    !horizontal.isFinite() ||
                    horizontal <= 0.001
                ) {

                    return Double.NaN
                }

                return (
                        vertical1 + vertical2
                        ) / (2.0 * horizontal)
            }

            // Calculate each eye independently.

            val leftEAR =
                ear(leftEye)

            val rightEAR =
                ear(rightEye)

            val readings = EyeEAR(
                left = leftEAR,
                right = rightEAR
            )

            return if (readings.isValid) {

                readings

            } else {

                null
            }

        } finally {

            image.close()
        }
    }

    // ==========================================
    // BACKWARD-COMPATIBLE MEAN EAR FUNCTION
    // ==========================================

    fun getEAR(
        bitmap: Bitmap,
        time: Long
    ): Double? {

        return getEyeEAR(
            bitmap,
            time
        )?.mean
    }

    // ==========================================
    // RELEASE AI MODEL
    // ==========================================

    fun close() {

        detector.close()
    }
}
