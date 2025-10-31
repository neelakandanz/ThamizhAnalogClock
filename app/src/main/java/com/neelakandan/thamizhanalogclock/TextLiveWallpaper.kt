package com.neelakandan.thamizhanalogclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

private const val PREF_NAME = "wallpaper_prefs"
private const val PREF_KEY_TEXT = "selected_text"

class TextLiveWallpaper : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return TextWallpaperEngine()
    }

    private inner class TextWallpaperEngine : Engine() {
        private var dynamicText: String = getString(R.string.text_option_1)

        private val handler = Handler(Looper.getMainLooper())
        private val drawDelayMillis: Long = 30
        private var isVisible = false

        private var xPos = 0f
        private var yPos = 0f
        private var xVelocity = 5f
        private var yVelocity = 5f
        private var screenWidth = 0
        private var screenHeight = 0

        private val paint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        private val drawRunnable = object : Runnable {
            override fun run() {
                if (isVisible) {
                    drawFrame()
                    handler.postDelayed(this, drawDelayMillis)
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)

            // Retrieve saved text from SharedPreferences
            val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            dynamicText = prefs.getString(PREF_KEY_TEXT, getString(R.string.text_option_1))
                ?: getString(R.string.text_option_1)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            screenWidth = width
            screenHeight = height

            if (xPos == 0f && yPos == 0f) {
                xPos = width / 2f
                yPos = height / 2f - ((paint.descent() + paint.ascent()) / 2f)
            }

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
            xPos += xVelocity
            yPos += yVelocity

            val halfTextWidth = paint.measureText(dynamicText) / 2
            val textBoundsLeft = xPos - halfTextWidth
            val textBoundsRight = xPos + halfTextWidth
            val textBoundsTop = yPos + paint.ascent()
            val textBoundsBottom = yPos + paint.descent()

            if (textBoundsLeft < 0) {
                xVelocity = kotlin.math.abs(xVelocity)
                xPos = halfTextWidth
            } else if (textBoundsRight > screenWidth) {
                xVelocity = -kotlin.math.abs(xVelocity)
                xPos = screenWidth - halfTextWidth
            }

            if (textBoundsTop < 0) {
                yVelocity = kotlin.math.abs(yVelocity)
                yPos = -paint.ascent()
            } else if (textBoundsBottom > screenHeight) {
                yVelocity = -kotlin.math.abs(yVelocity)
                yPos = screenHeight - paint.descent()
            }

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder?.lockCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.BLACK)
                    canvas.drawText(dynamicText, xPos, yPos, paint)
                }
            } finally {
                canvas?.let { surfaceHolder?.unlockCanvasAndPost(it) }
            }
        }
    }
}
