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

// Key for shared preference
private const val PREF_NAME = "wallpaper_prefs"
private const val PREF_KEY_TEXT = "selected_text"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Find all buttons
        val buttons = listOf<Button>(
            findViewById(R.id.btn_option_1),
            findViewById(R.id.btn_option_2),
            findViewById(R.id.btn_option_3),
            findViewById(R.id.btn_option_4),
            findViewById(R.id.btn_option_5)
        )

        // Assign click listener
        buttons.forEach { button ->
            val wallpaperText = button.tag.toString()
            button.setOnClickListener {
                saveSelectedText(wallpaperText)
                launchWallpaperChooser()
            }
        }

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saveSelectedText(text: String) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit()
            .putString(PREF_KEY_TEXT, text)
            .apply()
    }

    private fun launchWallpaperChooser() {
        val intent: Intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@MainActivity, TextLiveWallpaper::class.java)
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
