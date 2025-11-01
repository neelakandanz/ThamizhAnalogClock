package com.neelakandan.thamizhanalogclock

import android.content.Context
import android.content.SharedPreferences
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

// Define constants for SharedPreferences (must match MainActivity)
private const val PREF_NAME = "clock_settings"
private const val KEY_NUMBER_STYLE = "number_style"
private const val STYLE_STANDARD = "standard"
private const val STYLE_TAMIL = "tamil"

class AnalogClockWallpaper : WallpaperService() {

    // Tamil numerals from 1 to 12
    // 1(க), 2(உ), 3(௩), 4(௪), 5(௫), 6(௬), 7(௭), 8(௮), 9(௯), 10(௧௦), 11(௧௧), 12(௧௨)
    private val tamilNumbers = arrayOf(
        "க", "உ", "௩", "௪", "௫", "௬", "௭", "௮", "௯", "௧௦", "௧௧", "௧௨"
    )
    private val standardNumbers = Array(12) { (it + 1).toString() }

    override fun onCreateEngine(): Engine {
        return ClockWallpaperEngine()
    }

    private inner class ClockWallpaperEngine :
        Engine(), SharedPreferences.OnSharedPreferenceChangeListener {

        private val handler = Handler(Looper.getMainLooper())
        private var isVisible = false
        private var screenWidth = 0
        private var screenHeight = 0
        private var centerX = 0f
        private var centerY = 0f
        private var radius = 0f

        private lateinit var prefs: SharedPreferences
        private var currentNumberStyle = STYLE_STANDARD

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

        private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
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

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)

            // Initialize preferences
            prefs = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener(this)

            // Load the initial setting
            loadSettings()
        }

        // This is called when the user changes the setting in MainActivity
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == KEY_NUMBER_STYLE) {
                loadSettings()
                // Redraw immediately
                if (isVisible) {
                    drawFrame()
                }
            }
        }

        private fun loadSettings() {
            currentNumberStyle = prefs.getString(KEY_NUMBER_STYLE, STYLE_STANDARD) ?: STYLE_STANDARD
        }

        override fun onDestroy() {
            super.onDestroy()
            prefs.unregisterOnSharedPreferenceChangeListener(this)
            handler.removeCallbacks(drawRunnable)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            screenWidth = width
            screenHeight = height
            centerX = width / 2f
            centerY = height / 2f
            radius = min(width, height) / 2f * 0.8f

            // Set dynamic text size for numbers
            numberPaint.textSize = radius * 0.15f // 15% of the clock radius

            if (isVisible) {
                handler.removeCallbacks(drawRunnable)
                handler.post(drawRunnable)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) {
                handler.post(drawRunnable)
            } else {
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

                    // 2. Draw the clock face
                    canvas.drawCircle(centerX, centerY, radius, facePaint)

                    // 2a. Draw the clock numbers (This will now use the new logic)
                    drawNumbers(canvas)

                    // 3. Draw the hour hand
                    val hourAngle = (hour + minute / 60f) * 30 - 90
                    drawHand(canvas, hourAngle, radius * 0.5f, hourHandPaint)

                    // 4. Draw the minute hand
                    val minuteAngle = minute * 6 - 90
                    drawHand(canvas, minuteAngle.toFloat(), radius * 0.7f, minuteHandPaint)

                    // 5. Draw the second hand
                    val secondAngle = second * 6 - 90
                    drawHand(canvas, secondAngle.toFloat(), radius * 0.9f, secondHandPaint)

                    // 6. Draw center point
                    canvas.drawCircle(centerX, centerY, 10f, centerPaint)
                }
            } finally {
                canvas?.let { surfaceHolder?.unlockCanvasAndPost(it) }
            }
        }

        private fun drawHand(canvas: Canvas, angleDegrees: Float, length: Float, paint: Paint) {
            val angleRadians = Math.toRadians(angleDegrees.toDouble())
            val endX = (centerX + length * cos(angleRadians)).toFloat()
            val endY = (centerY + length * sin(angleRadians)).toFloat()
            canvas.drawLine(centerX, centerY, endX, endY, paint)
        }

        private fun drawNumbers(canvas: Canvas) {
            val numberRadius = radius * 0.85f
            val textOffsetY = (numberPaint.descent() + numberPaint.ascent()) / 2f

            // Select the correct number set based on the saved preference
            val numbersToDraw = if (currentNumberStyle == STYLE_TAMIL) {
                tamilNumbers
            } else {
                standardNumbers
            }

            for (n in 1..12) {
                val numberString = numbersToDraw[n - 1] // Get from the selected array

                val angleRadians = Math.toRadians((n * 30 - 90).toDouble())

                val x = (centerX + numberRadius * cos(angleRadians)).toFloat()
                val y = (centerY + numberRadius * sin(angleRadians)).toFloat()

                // Draw the text
                canvas.drawText(numberString, x, y - textOffsetY, numberPaint)
            }
        }
    }
}