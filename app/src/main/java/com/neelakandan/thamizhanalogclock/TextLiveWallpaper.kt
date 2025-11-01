package com.neelakandan.thamizhanalogclock

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AnalogClockWallpaper : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return ClockWallpaperEngine()
    }

    private inner class ClockWallpaperEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var isVisible = false
        private var screenWidth = 0
        private var screenHeight = 0
        private var centerX = 0f
        private var centerY = 0f
        private var radius = 0f

        // Paints for different clock parts
        private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        private val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
        }

        private val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
        }

        private val secondHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
        }

        private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        private val drawRunnable = object : Runnable {
            override fun run() {
                if (isVisible) {
                    drawFrame()
                    // Schedule the next draw in 1 second
                    handler.postDelayed(this, 1000)
                }
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            screenWidth = width
            screenHeight = height
            centerX = width / 2f
            centerY = height / 2f
            // Set radius to be 80% of the smallest screen dimension
            radius = min(width, height) / 2f * 0.8f

            if (isVisible) {
                handler.removeCallbacks(drawRunnable)
                handler.post(drawRunnable)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) {
                // Start drawing when visible
                handler.post(drawRunnable)
            } else {
                // Stop drawing when not visible
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            isVisible = false
            handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder?.lockCanvas()
                if (canvas != null) {
                    // Get current time
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR)
                    val minute = calendar.get(Calendar.MINUTE)
                    val second = calendar.get(Calendar.SECOND)

                    // 1. Clear the canvas
                    canvas.drawColor(Color.BLACK)

                    // 2. Draw the clock face (a simple circle)
                    canvas.drawCircle(centerX, centerY, radius, facePaint)

                    // 3. Draw the hour hand
                    // 12 hours on the clock, 360 degrees / 12 = 30 degrees per hour
                    // Also move based on minutes: 30 degrees / 60 minutes = 0.5 degrees per minute
                    val hourAngle = (hour + minute / 60f) * 30 - 90
                    drawHand(canvas, hourAngle, radius * 0.5f, hourHandPaint) // 50% of radius

                    // 4. Draw the minute hand
                    // 60 minutes on the clock, 360 degrees / 60 = 6 degrees per minute
                    val minuteAngle = minute * 6 - 90
                    drawHand(canvas, minuteAngle.toFloat(), radius * 0.7f, minuteHandPaint) // 70% of radius

                    // 5. Draw the second hand
                    // 60 seconds on the clock, 360 degrees / 60 = 6 degrees per second
                    val secondAngle = second * 6 - 90
                    drawHand(canvas, secondAngle.toFloat(), radius * 0.9f, secondHandPaint) // 90% of radius

                    // 6. Draw center point
                    canvas.drawCircle(centerX, centerY, 10f, centerPaint)
                }
            } finally {
                canvas?.let { surfaceHolder?.unlockCanvasAndPost(it) }
            }
        }

        private fun drawHand(canvas: Canvas, angleDegrees: Float, length: Float, paint: Paint) {
            // Convert angle to radians for trig functions
            val angleRadians = Math.toRadians(angleDegrees.toDouble())
            val endX = (centerX + length * cos(angleRadians)).toFloat()
            val endY = (centerY + length * sin(angleRadians)).toFloat()
            canvas.drawLine(centerX, centerY, endX, endY, paint)
        }
    }
}