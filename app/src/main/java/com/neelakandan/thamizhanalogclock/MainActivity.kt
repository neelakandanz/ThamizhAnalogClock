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

        // Find the button from the layout
        val setWallpaperButton: Button = findViewById(R.id.set_wallpaper_button)

        // Set the click listener
        setWallpaperButton.setOnClickListener {
            launchWallpaperChooser()
        }

        // Handle system insets (status/navigation bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Launches the wallpaper chooser to set the live wallpaper.
     * Uses modern intent for API >= 17 and fallback for older devices.
     */
    private fun launchWallpaperChooser() {
        val intent: Intent

        // Use modern Live Wallpaper chooser for Android 4.2+ (API 17+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@MainActivity, TextLiveWallpaper::class.java)
                )
            }
        } else {
            // Fallback for very old versions
            intent = Intent(Intent.ACTION_SET_WALLPAPER)
        }

        try {
            startActivity(intent)
        } catch (ignored: Exception) {
            // Fallback: open Android wallpaper settings directly
            Intent(Intent.ACTION_VIEW).also {
                it.setClassName(
                    "com.android.settings",
                    "com.android.settings.wallpaper.WallpaperSettings"
                )
                try {
                    startActivity(it)
                } catch (ignored2: Exception) {
                    // Final fallback: open basic wallpaper chooser
                    startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
                }
            }
        }
    }
}
