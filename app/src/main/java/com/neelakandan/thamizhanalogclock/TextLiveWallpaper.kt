package com.neelakandan.thamizhanalogclock

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class TextLiveWallpaper : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return TextWallpaperEngine()
    }

    private inner class TextWallpaperEngine : Engine() {
        private val text = "there is no tomorrow"

        // Setup the paint object for the text
        private val paint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            // Draw the static text whenever the surface changes (e.g., orientation change)
            drawStaticText(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                // Redraw when the wallpaper becomes visible
                drawStaticText(surfaceHolder)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
        }

        private fun drawStaticText(holder: SurfaceHolder?) {
            var canvas: Canvas? = null
            try {
                canvas = holder?.lockCanvas()
                if (canvas != null) {
                    draw(canvas)
                }
            } finally {
                if (canvas != null)
                    holder?.unlockCanvasAndPost(canvas)
            }
        }

        private fun draw(canvas: Canvas) {
            // 1. Clear the canvas and set the background to black
            canvas.drawColor(Color.BLACK)

            // 2. Calculate text position to center it
            val xPos = canvas.width / 2f
            // Center the text vertically based on the ascent/descent of the font
            val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)

            // 3. Draw the text
            canvas.drawText(text, xPos, yPos, paint)
        }
    }
}