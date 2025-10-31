package com.neelakandan.thamizhanalogclock

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class TextLiveWallpaper : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return TextWallpaperEngine()
    }

    private inner class TextWallpaperEngine : Engine() {
        // Fetch the text from strings.xml ("There is No Tomorrow Wallpaper")
        private val text = applicationContext.getString(R.string.wallpaper_name)

        // Handler for the animation loop
        private val handler = Handler()
        private val drawDelayMillis: Long = 30 // Approximately 33 FPS for smooth movement
        private var isVisible = false

        // Position and Velocity variables for movement
        private var xPos = 0f
        private var yPos = 0f
        private var xVelocity = 5f // Speed in X direction
        private var yVelocity = 5f // Speed in Y direction
        private var screenWidth = 0
        private var screenHeight = 0

        // Setup the paint object for the text
        private val paint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        // The Runnable that updates position and redraws
        private val drawRunnable = object : Runnable {
            override fun run() {
                if (isVisible) {
                    drawFrame()
                    // Schedule the next frame
                    handler.postDelayed(this, drawDelayMillis)
                }
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            screenWidth = width
            screenHeight = height

            // Initialize position to center only on the first run
            if (xPos == 0f && yPos == 0f) {
                xPos = width / 2f
                yPos = height / 2f - ((paint.descent() + paint.ascent()) / 2f)
            }

            // If already visible, restart the drawing loop in case of screen rotation/size change
            if (isVisible) {
                handler.removeCallbacks(drawRunnable)
                handler.post(drawRunnable)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) {
                // Start drawing loop when the wallpaper becomes visible
                handler.post(drawRunnable)
            } else {
                // Stop drawing loop to save resources when hidden
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            isVisible = false
            // Stop drawing thread immediately
            handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            // 1. Update position based on velocity
            xPos += xVelocity
            yPos += yVelocity

            // 2. Boundary detection and velocity reversal (Bouncing logic)

            // Calculate text boundaries (xPos is center, yPos is baseline)
            val halfTextWidth = paint.measureText(text) / 2
            val textBoundsLeft = xPos - halfTextWidth
            val textBoundsRight = xPos + halfTextWidth
            val textBoundsTop = yPos + paint.ascent()
            val textBoundsBottom = yPos + paint.descent()

            // Handle horizontal bounds (left/right edges)
            if (textBoundsLeft < 0) {
                xVelocity = Math.abs(xVelocity) // Reverse X direction, ensure positive velocity
                xPos = halfTextWidth // Clamp to left edge
            } else if (textBoundsRight > screenWidth) {
                xVelocity = -Math.abs(xVelocity) // Reverse X direction, ensure negative velocity
                xPos = screenWidth - halfTextWidth // Clamp to right edge
            }

            // Handle vertical bounds (top/bottom edges)
            if (textBoundsTop < 0) {
                yVelocity = Math.abs(yVelocity) // Reverse Y direction, ensure positive velocity
                yPos = -paint.ascent() // Clamp to top edge (yPos is the baseline)
            } else if (textBoundsBottom > screenHeight) {
                yVelocity = -Math.abs(yVelocity) // Reverse Y direction, ensure negative velocity
                yPos = screenHeight - paint.descent() // Clamp to bottom edge (yPos is the baseline)
            }


            // 3. Draw to canvas
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder?.lockCanvas()
                if (canvas != null) {
                    // Clear the canvas and set the background to black
                    canvas.drawColor(Color.BLACK)
                    // Draw the text at the current calculated position
                    canvas.drawText(text, xPos, yPos, paint)
                }
            } finally {
                if (canvas != null)
                    surfaceHolder?.unlockCanvasAndPost(canvas)
            }
        }
    }
}