package com.neelakandan.thamizhanalogclock

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Find the button
        val setWallpaperButton: Button = findViewById(R.id.btn_set_wallpaper)

        // Assign click listener
        setWallpaperButton.setOnClickListener {
            launchWallpaperChooser()
        }

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun launchWallpaperChooser() {
        val intent: Intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    // IMPORTANT: Change this to the new wallpaper service
                    ComponentName(this@MainActivity, AnalogClockWallpaper::class.java)
                )
            }
        } else {
            intent = Intent(Intent.ACTION_SET_WALLPAPER)
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    setClassName(
                        "com.android.settings",
                        "com.android.settings.wallpaper.WallpaperSettings"
                    )
                }
                startActivity(fallbackIntent)
            } catch (_: Exception) {
                startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
            }
        }
    }
}