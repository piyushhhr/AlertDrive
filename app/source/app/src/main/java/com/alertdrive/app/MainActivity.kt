
package com.alertdrive.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executors

class MainActivity : Activity() {

    // ==========================================
    // GRAPHITE BLACK THEME
    // ==========================================

    private val bg = Color.parseColor("#151719")
    private val beige = Color.parseColor("#BFC6CD")
    private val surface = Color.parseColor("#25292D")
    private val ivory = Color.parseColor("#F5F5F5")
    private val muted = Color.parseColor("#A1A9B0")

    private val cardBorder = Color.parseColor("#444A50")
    private val iconBackground = Color.parseColor("#343A40")

    // ==========================================
    // ALERT STATE COLOURS
    // ==========================================

    private val green = Color.parseColor("#69D08C")
    private val yellow = Color.parseColor("#F2C94C")
    private val orange = Color.parseColor("#FF9F43")
    private val red = Color.parseColor("#FF6B6B")
    private val blue = Color.parseColor("#6EC6FF")
    private val purple = Color.parseColor("#B388FF")

    // ==========================================
    // UI ELEMENTS
    // ==========================================

    private lateinit var statusTitle: TextView
    private lateinit var statusDescription: TextView

    private lateinit var networkStatus: TextView
    private lateinit var networkDot: View

    private lateinit var statusRing: StatusRingView

    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    // ==========================================
    // LIVE CAMERA PREVIEW ELEMENTS
    // ==========================================

    private lateinit var cameraCard: LinearLayout
    private lateinit var cameraImage: ImageView
    private lateinit var cameraMessage: TextView
    private lateinit var cameraToggle: Button

    private var cameraExpanded = false

    private var lastPreviewFrame:
            MonitoringStatus.PreviewFrame? = null

    private val previewTimeout = 2000L

    // ==========================================
    // NETWORK AND STATUS MANAGEMENT
    // ==========================================

    private val handler = Handler(
        Looper.getMainLooper()
    )

    private val executor =
        Executors.newSingleThreadExecutor()

    private var checkingHealth = false
    private var idleConnected: Boolean? = null

    private var lastHealthCheck = 0L
    private var activityVisible = false
    private var startRequestedAt = 0L

    private val refreshTask = object : Runnable {

        override fun run() {

            if (!activityVisible) return

            refreshStatus()

            val snapshot = MonitoringStatus.current
            val now = SystemClock.elapsedRealtime()

            if (
                !snapshot.running &&
                !checkingHealth &&
                hasLocalNetworkPermission() &&
                now - lastHealthCheck >= 2500
            ) {

                checkDeviceConnection()
            }

            handler.postDelayed(this, 200)
        }
    }

    // ==========================================
    // CREATE ACTIVITY
    // ==========================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        ESP32Client.init(this)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        window.decorView.systemUiVisibility = 0

        MonitoringStatus.setPreviewEnabled(false)

        // ======================================
        // ROOT LAYOUT
        // ======================================

        val scroll = ScrollView(this).apply {

            setBackgroundColor(bg)

            isFillViewport = true

            isVerticalScrollBarEnabled = false
        }

        val root = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(24),
                dp(24),
                dp(24),
                dp(24)
            )
        }

        scroll.addView(root)

        setContentView(scroll)

        // ======================================
        // SYSTEM BAR SPACING
        // ======================================

        if (Build.VERSION.SDK_INT >= 30) {

            scroll.setOnApplyWindowInsetsListener {
                    view, insets ->

                val bars = insets.getInsets(
                    WindowInsets.Type.systemBars()
                )

                view.setPadding(
                    bars.left,
                    bars.top,
                    bars.right,
                    bars.bottom
                )

                insets
            }

            scroll.requestApplyInsets()
        }

        space(root, 12)

        // ======================================
        // APPLICATION HEADER
        // ======================================

        root.addView(

            label(
                "AlertDrive",
                34f,
                ivory,
                true
            ).apply {

                gravity = Gravity.CENTER

                letterSpacing = -0.03f
            }
        )

        space(root, 5)

        root.addView(

            label(
                "DRIVER MONITORING SYSTEM",
                10f,
                beige,
                true
            ).apply {

                gravity = Gravity.CENTER

                letterSpacing = 0.13f
            }
        )

        space(root, 28)

        // ======================================
        // STEERING SHIELD STATUS RING
        // ======================================

        statusRing = StatusRingView(this)

        root.addView(

            statusRing,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(235)
            )
        )

        space(root, 18)

        // ======================================
        // MONITORING STATUS
        // ======================================

        statusTitle = label(
            "System standby",
            26f,
            ivory,
            true
        ).apply {

            gravity = Gravity.CENTER
        }

        root.addView(statusTitle)

        space(root, 9)

        statusDescription = label(
            "Ready when you are.",
            14f,
            muted
        ).apply {

            gravity = Gravity.CENTER

            setLineSpacing(
                dp(3).toFloat(),
                1f
            )

            minimumHeight = dp(48)
        }

        root.addView(

            statusDescription,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        space(root, 22)

        // ======================================
        // DEVICE CONNECTION CARD
        // ======================================

        val networkCard = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER_VERTICAL

            background = rounded(
                surface,
                20,
                cardBorder
            )

            setPadding(
                dp(16),
                dp(17),
                dp(16),
                dp(17)
            )
        }

        // ======================================
        // WIFI ICON
        // ======================================

        val wifiIcon = WifiIconView(this).apply {

            background = rounded(
                iconBackground,
                12
            )
        }

        networkCard.addView(

            wifiIcon,

            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        // ======================================
        // NETWORK INFORMATION
        // ======================================

        val networkText = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(14),
                0,
                0,
                0
            )
        }

        networkText.addView(

            label(
                "Device connection",
                12f,
                muted
            )
        )

        space(networkText, 4)

        networkText.addView(

            label(
                "ALERT-DRIVE",
                16f,
                ivory,
                true
            )
        )

        space(networkText, 7)

        // ======================================
        // CONNECTION STATUS
        // ======================================

        val connectionRow = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER_VERTICAL
        }

        networkDot = View(this).apply {

            background = rounded(
                beige,
                50
            )
        }

        connectionRow.addView(

            networkDot,

            LinearLayout.LayoutParams(
                dp(8),
                dp(8)
            ).apply {

                marginEnd = dp(7)
            }
        )

        networkStatus = label(
            "Checking",
            12f,
            beige,
            true
        )

        connectionRow.addView(

            networkStatus,

            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        networkText.addView(connectionRow)

        networkCard.addView(

            networkText,

            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        root.addView(

            networkCard,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // ======================================
        // LIVE CAMERA PREVIEW
        // ======================================

        space(root, 16)

        createCameraPreview(root)

        space(root, 24)

        // ======================================
        // START MONITORING BUTTON
        // ======================================

        startButton = Button(this).apply {

            text = "Start monitoring"

            textSize = 16f

            isAllCaps = false

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(bg)

            backgroundTintList = null

            stateListAnimator = null

            background = rounded(
                beige,
                18
            )

            setOnClickListener {

                if (!hasLocalNetworkPermission()) {

                    requestAppPermissions()

                    return@setOnClickListener
                }

                if (MonitoringStatus.current.running) {

                    return@setOnClickListener
                }

                try {

                    startRequestedAt =
                        SystemClock.elapsedRealtime()

                    startForegroundService(

                        Intent(
                            this@MainActivity,
                            DetectionService::class.java
                        )
                    )

                    refreshStatus()

                } catch (e: Exception) {

                    startRequestedAt = 0L

                    Toast.makeText(
                        this@MainActivity,
                        e.message ?: "Unable to start",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        root.addView(

            startButton,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        space(root, 13)

        // ======================================
        // STOP MONITORING BUTTON
        // ======================================

        stopButton = Button(this).apply {

            text = "Stop monitoring"

            textSize = 16f

            isAllCaps = false

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(beige)

            backgroundTintList = null

            stateListAnimator = null

            background = rounded(
                bg,
                18,
                beige
            )

            setOnClickListener {

                startRequestedAt = 0L

                MonitoringStatus.setPreviewEnabled(false)

                clearCameraImage()

                stopService(

                    Intent(
                        this@MainActivity,
                        DetectionService::class.java
                    )
                )

                lastHealthCheck = 0L

                idleConnected = null

                refreshStatus()
            }
        }

        root.addView(

            stopButton,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            )
        )

        space(root, 16)

        requestAppPermissions()

        refreshStatus()
    }

    // ==========================================
    // CREATE CAMERA PREVIEW UI
    // ==========================================

    private fun createCameraPreview(
        root: LinearLayout
    ) {

        // SHOW / HIDE CAMERA BUTTON

        cameraToggle = Button(this).apply {

            text = "Show camera preview"

            textSize = 14f

            isAllCaps = false

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(beige)

            backgroundTintList = null

            stateListAnimator = null

            background = rounded(
                surface,
                16,
                cardBorder
            )

            setOnClickListener {

                cameraExpanded = !cameraExpanded

                cameraCard.visibility =
                    if (cameraExpanded)
                        View.VISIBLE
                    else
                        View.GONE

                text =
                    if (cameraExpanded)
                        "Hide camera preview"
                    else
                        "Show camera preview"

                refreshCameraPreview()
            }
        }

        root.addView(

            cameraToggle,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )

        // ======================================
        // CAMERA CARD
        // ======================================

        cameraCard = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            background = rounded(
                surface,
                18,
                cardBorder
            )

            setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
            )

            visibility = View.GONE
        }

        // ======================================
        // CAMERA HEADER
        // ======================================

        val cameraHeader = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER_VERTICAL
        }

        cameraHeader.addView(

            label(
                "Live camera",
                14f,
                ivory,
                true
            ),

            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        cameraHeader.addView(

            label(
                "GC2145",
                11f,
                muted
            )
        )

        cameraCard.addView(cameraHeader)

        space(cameraCard, 10)

        // ======================================
        // LIVE CAMERA IMAGE
        // ======================================

        cameraImage = ImageView(this).apply {

            scaleType = ImageView.ScaleType.FIT_CENTER

            background = rounded(
                bg,
                12
            )

            clipToOutline = true

            contentDescription =
                "AlertDrive live camera preview"
        }

        cameraCard.addView(

            cameraImage,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(180)
            )
        )

        space(cameraCard, 10)

        // ======================================
        // CAMERA STATUS
        // ======================================

        cameraMessage = label(
            "Start monitoring to view camera",
            12f,
            muted
        ).apply {

            gravity = Gravity.CENTER
        }

        cameraCard.addView(

            cameraMessage,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(

            cameraCard,

            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin = dp(10)
            }
        )
    }

    // ==========================================
    // REFRESH LIVE CAMERA PREVIEW
    // ==========================================

    private fun refreshCameraPreview() {

        if (!::cameraImage.isInitialized) return

        val snapshot = MonitoringStatus.current

        val now = SystemClock.elapsedRealtime()

        val enabled =
            cameraExpanded &&
                    activityVisible &&
                    snapshot.running

        MonitoringStatus.setPreviewEnabled(enabled)

        // Preview hidden or app in background

        if (!cameraExpanded || !activityVisible) {

            clearCameraImage()

            return
        }

        // Monitoring has not started

        if (!snapshot.running) {

            clearCameraImage()

            cameraMessage.text =
                "Start monitoring to view camera"

            cameraMessage.setTextColor(muted)

            return
        }

        // Monitoring is initializing

        if (snapshot.state == "CONNECTING") {

            clearCameraImage()

            cameraMessage.text =
                "Connecting to camera..."

            cameraMessage.setTextColor(muted)

            return
        }

        // Camera or communication unavailable

        val connectionUnavailable =

            !snapshot.connected ||

                    now - snapshot.updatedAt > 5000L ||

                    snapshot.state == "CONNECTION_LOST" ||

                    snapshot.state == "MONITORING_INTERRUPTED" ||

                    snapshot.state == "CAMERA_ERROR" ||

                    snapshot.state == "ERROR"

        if (connectionUnavailable) {

            clearCameraImage()

            cameraMessage.text =
                "Camera unavailable"

            cameraMessage.setTextColor(purple)

            return
        }

        // Obtain latest preview frame

        val frame = MonitoringStatus.previewFrame

        // Reject missing or stale frames

        if (
            frame == null ||
            now - frame.capturedAt > previewTimeout
        ) {

            clearCameraImage()

            cameraMessage.text =
                "Waiting for camera frames..."

            cameraMessage.setTextColor(muted)

            return
        }

        // Protect against recycled bitmap

        if (frame.bitmap.isRecycled) {

            clearCameraImage()

            cameraMessage.text =
                "Waiting for camera frames..."

            cameraMessage.setTextColor(muted)

            return
        }

        // Update only when a new frame arrives

        if (lastPreviewFrame !== frame) {

            cameraImage.setImageBitmap(
                frame.bitmap
            )

            lastPreviewFrame = frame
        }

        cameraMessage.text =
            "LIVE • Camera connected"

        cameraMessage.setTextColor(green)
    }

    // ==========================================
    // CLEAR CAMERA IMAGE
    // ==========================================

    private fun clearCameraImage() {

        if (::cameraImage.isInitialized) {

            cameraImage.setImageDrawable(null)
        }

        lastPreviewFrame = null
    }

    // ==========================================
    // ACTIVITY LIFECYCLE
    // ==========================================

    override fun onResume() {

        super.onResume()

        activityVisible = true

        idleConnected = null

        lastHealthCheck = 0L

        handler.removeCallbacks(refreshTask)

        handler.post(refreshTask)
    }

    override fun onPause() {

        activityVisible = false

        handler.removeCallbacks(refreshTask)

        // Stop preview bitmap generation when
        // the application is in the background.

        MonitoringStatus.setPreviewEnabled(false)

        clearCameraImage()

        super.onPause()
    }

    override fun onDestroy() {

        activityVisible = false

        handler.removeCallbacks(refreshTask)

        MonitoringStatus.setPreviewEnabled(false)

        clearCameraImage()

        executor.shutdownNow()

        super.onDestroy()
    }

    // ==========================================
    // VERIFY ESP32 CONNECTION
    // ==========================================

    private fun checkDeviceConnection() {

        if (checkingHealth) return

        checkingHealth = true

        executor.execute {

            val connected = runCatching {

                ESP32Client.checkHealth()

            }.getOrDefault(false)

            handler.post {

                if (isDestroyed) return@post

                idleConnected = connected

                lastHealthCheck =
                    SystemClock.elapsedRealtime()

                checkingHealth = false

                if (activityVisible) {

                    refreshStatus()
                }
            }
        }
    }

    // ==========================================
    // UPDATE UI USING REAL MONITORING DATA
    // ==========================================

    private fun refreshStatus() {

        val snapshot = MonitoringStatus.current

        val now = SystemClock.elapsedRealtime()

        if (snapshot.running) {

            startRequestedAt = 0L
        }

        val pendingStart =
            startRequestedAt > 0L &&
                    now - startRequestedAt < 5000

        val stale =
            snapshot.running &&
                    now - snapshot.updatedAt > 5000 &&
                    snapshot.state != "CONNECTING"

        val state = when {

            stale ->
                "MONITORING_INTERRUPTED"

            pendingStart && !snapshot.running ->
                "CONNECTING"

            else ->
                snapshot.state
        }

        var colour = beige

        // ======================================
        // ALERT STATES
        // ======================================

        when (state) {

            // STARTING MONITORING

            "CONNECTING" -> {

                colour = blue

                statusTitle.text =
                    "Starting monitoring"

                statusDescription.text =
                    "Establishing device communication."
            }

            // DRIVER AWAKE — GREEN

            "NORMAL" -> {

                colour = green

                statusTitle.text =
                    "Driver alert"

                statusDescription.text =
                    "Monitoring eye activity."
            }

            // EYES CLOSING — YELLOW

            "CLOSING" -> {

                colour = yellow

                statusTitle.text =
                    "Eyes closing"

                statusDescription.text =
                    "Prolonged eye closure detected."
            }

            // DROWSINESS WARNING — ORANGE

            "WARNING" -> {

                colour = orange

                statusTitle.text =
                    "Drowsiness warning"

                statusDescription.text =
                    "Audible warning requested."
            }

            // CRITICAL DROWSINESS — RED

            "DROWSY" -> {

                colour = red

                statusTitle.text =
                    "Drowsiness detected"

                statusDescription.text =
                    "Critical alert requested."
            }

            // NO FACE — CYAN

            "NO_FACE" -> {

                colour = blue

                statusTitle.text =
                    "No face detected"

                statusDescription.text =
                    "Ensure your face is visible."
            }

            // CAMERA ERROR — CYAN

            "CAMERA_ERROR" -> {

                colour = blue

                statusTitle.text =
                    "Camera unavailable"

                statusDescription.text =
                    "Unable to process the camera frame."
            }

            // CONNECTION LOST — PURPLE

            "CONNECTION_LOST" -> {

                colour = purple

                statusTitle.text =
                    "Connection lost"

                statusDescription.text =
                    "Device communication interrupted. Retrying..."
            }

            // MONITORING INTERRUPTED — PURPLE

            "MONITORING_INTERRUPTED" -> {

                colour = purple

                statusTitle.text =
                    "Monitoring interrupted"

                statusDescription.text =
                    "Connection or processing error. Retrying..."
            }

            // MONITORING ERROR — PURPLE

            "ERROR" -> {

                colour = purple

                statusTitle.text =
                    "Monitoring failed"

                statusDescription.text =
                    "Check the device and restart monitoring."
            }

            // SYSTEM STANDBY — SILVER

            else -> {

                colour = beige

                statusTitle.text =
                    "System standby"

                statusDescription.text =
                    "Ready when you are."
            }
        }

        // Apply active state colour

        statusTitle.setTextColor(colour)

        statusRing.updateColour(colour)

        // ======================================
        // REAL-TIME DEVICE CONNECTION
        // ======================================

        when {

            !hasLocalNetworkPermission() -> {

                updateNetwork(
                    "Permission needed",
                    yellow
                )
            }

            snapshot.running && stale -> {

                updateNetwork(
                    "Not verified",
                    purple
                )
            }

            snapshot.running &&
                    snapshot.state == "CONNECTION_LOST" -> {

                updateNetwork(
                    "Disconnected",
                    purple
                )
            }

            snapshot.running &&
                    snapshot.connected -> {

                updateNetwork(
                    "Connected",
                    green
                )
            }

            snapshot.running &&
                    snapshot.state == "CONNECTING" -> {

                updateNetwork(
                    "Connecting",
                    blue
                )
            }

            snapshot.running -> {

                updateNetwork(
                    "Not verified",
                    purple
                )
            }

            idleConnected == true &&
                    now - lastHealthCheck < 6000 -> {

                updateNetwork(
                    "Connected",
                    green
                )
            }

            idleConnected == false &&
                    now - lastHealthCheck < 6000 -> {

                updateNetwork(
                    "Disconnected",
                    purple
                )
            }

            else -> {

                updateNetwork(
                    "Checking",
                    beige
                )
            }
        }

        // ======================================
        // BUTTON AVAILABILITY
        // ======================================

        val monitoring =
            snapshot.running || pendingStart

        startButton.isEnabled = !monitoring

        startButton.alpha =
            if (monitoring) 0.5f else 1f

        stopButton.isEnabled = monitoring

        stopButton.alpha =
            if (monitoring) 1f else 0.65f

        // ======================================
        // UPDATE LIVE CAMERA PREVIEW
        // ======================================

        refreshCameraPreview()
    }

    // ==========================================
    // UPDATE CONNECTION STATUS
    // ==========================================

    private fun updateNetwork(
        text: String,
        colour: Int
    ) {

        networkStatus.text = text

        networkStatus.setTextColor(colour)

        networkDot.background =
            rounded(colour, 50)
    }

    // ==========================================
    // CIRCULAR STEERING SHIELD
    // ==========================================

    private inner class StatusRingView(
        context: Context
    ) : View(context) {

        private val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private var ringColour = beige

        // Original logo with silver tint

        private val logo =
            context.getDrawable(
                R.drawable.alertdrive_logo
            )?.mutate()?.apply {

                setTint(beige)
            }

        fun updateColour(colour: Int) {

            if (ringColour != colour) {

                ringColour = colour

                invalidate()
            }
        }

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val size = minOf(
                width,
                height
            ).toFloat()

            if (size <= 0f) return

            val cx = width / 2f

            val cy = height / 2f

            val scale = size / 240f

            canvas.save()

            canvas.translate(cx, cy)

            canvas.scale(scale, scale)

            // ==================================
            // OUTER RING
            // ==================================

            paint.style = Paint.Style.STROKE

            paint.pathEffect = null

            paint.color = ringColour

            paint.alpha = 65

            paint.strokeWidth = 1.2f

            canvas.drawCircle(
                0f,
                0f,
                111f,
                paint
            )

            // ==================================
            // DASHED RING
            // ==================================

            paint.alpha = 165

            paint.strokeWidth = 2f

            paint.pathEffect = DashPathEffect(
                floatArrayOf(3f, 10f),
                0f
            )

            canvas.drawCircle(
                0f,
                0f,
                98f,
                paint
            )

            paint.pathEffect = null

            paint.alpha = 255

            // ==================================
            // INNER BACKGROUND
            // ==================================

            paint.style = Paint.Style.FILL

            paint.color = surface

            canvas.drawCircle(
                0f,
                0f,
                82f,
                paint
            )

            // ==================================
            // INNER BORDER
            // ==================================

            paint.style = Paint.Style.STROKE

            paint.color = ringColour

            paint.strokeWidth = 2f

            canvas.drawCircle(
                0f,
                0f,
                82f,
                paint
            )

            // ==================================
            // SILVER STEERING SHIELD LOGO
            // ==================================

            logo?.setBounds(
                -70,
                -70,
                70,
                70
            )

            logo?.draw(canvas)

            // ==================================
            // STATUS DOT
            // ==================================

            paint.style = Paint.Style.FILL

            paint.color = ringColour

            canvas.drawCircle(
                0f,
                -98f,
                5f,
                paint
            )

            canvas.restore()
        }
    }

    // ==========================================
    // CUSTOM WIFI ICON
    // ==========================================

    private inner class WifiIconView(
        context: Context
    ) : View(context) {

        private val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {

            color = beige

            strokeWidth = 2.5f

            strokeCap = Paint.Cap.ROUND
        }

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            canvas.save()

            canvas.translate(
                width / 2f,
                height / 2f
            )

            val scale = minOf(
                width,
                height
            ) / 44f

            canvas.scale(scale, scale)

            paint.style = Paint.Style.STROKE

            for (radius in listOf(15f, 10f, 5f)) {

                canvas.drawArc(

                    RectF(
                        -radius,
                        9f - radius,
                        radius,
                        9f + radius
                    ),

                    225f,

                    90f,

                    false,

                    paint
                )
            }

            paint.style = Paint.Style.FILL

            canvas.drawCircle(
                0f,
                9f,
                1.6f,
                paint
            )

            canvas.restore()
        }
    }

    // ==========================================
    // PERMISSIONS
    // ==========================================

    private fun hasLocalNetworkPermission(): Boolean {

        return Build.VERSION.SDK_INT < 37 ||
                checkSelfPermission(
                    "android.permission.ACCESS_LOCAL_NETWORK"
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAppPermissions() {

        if (!hasLocalNetworkPermission()) {

            requestPermissions(

                arrayOf(
                    "android.permission.ACCESS_LOCAL_NETWORK"
                ),

                101
            )

        } else if (

            Build.VERSION.SDK_INT >= 33 &&

            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(

                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),

                102
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (

            requestCode == 101 &&

            grantResults.firstOrNull() ==
            PackageManager.PERMISSION_GRANTED
        ) {

            lastHealthCheck = 0L

            requestAppPermissions()
        }
    }

    // ==========================================
    // UI HELPER FUNCTIONS
    // ==========================================

    private fun label(
        value: String,
        size: Float,
        colour: Int,
        bold: Boolean = false
    ): TextView {

        return TextView(this).apply {

            text = value

            textSize = size

            setTextColor(colour)

            typeface = if (bold) {

                Typeface.create(
                    "sans-serif-medium",
                    Typeface.NORMAL
                )

            } else {

                Typeface.create(
                    "sans-serif",
                    Typeface.NORMAL
                )
            }
        }
    }

    private fun rounded(
        colour: Int,
        radius: Int,
        stroke: Int? = null
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(colour)

            cornerRadius =
                dp(radius).toFloat()

            if (stroke != null) {

                setStroke(
                    dp(1),
                    stroke
                )
            }
        }
    }

    private fun space(
        parent: LinearLayout,
        height: Int
    ) {

        parent.addView(

            View(this),

            LinearLayout.LayoutParams(
                1,
                dp(height)
            )
        )
    }

    private fun dp(value: Int): Int {

        return (

                value *
                        resources.displayMetrics.density +
                        0.5f
                ).toInt()
    }
}
