package com.neelakandan.thamizhanalogclock

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Define constants for SharedPreferences
private const val PREF_NAME = "clock_settings"
private const val KEY_NUMBER_STYLE = "number_style"
private const val STYLE_STANDARD = "standard"
private const val STYLE_TAMIL = "tamil"

class MainActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val setWallpaperButton: Button = findViewById(R.id.btn_set_wallpaper)
        setWallpaperButton.setOnClickListener {
            launchWallpaperChooser()
        }

        radioGroup = findViewById(R.id.radio_group_number_style)

        // Load saved preference and set the UI
        loadSettings()

        // Save preference when user changes selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedStyle = when (checkedId) {
                R.id.radio_tamil -> STYLE_TAMIL
                else -> STYLE_STANDARD
            }
            saveNumberStyle(selectedStyle)
        }

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val style = prefs.getString(KEY_NUMBER_STYLE, STYLE_STANDARD)

        if (style == STYLE_TAMIL) {
            radioGroup.check(R.id.radio_tamil)
        } else {
            radioGroup.check(R.id.radio_standard)
        }
    }

    private fun saveNumberStyle(style: String) {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_NUMBER_STYLE, style)
            .apply()
    }

    private fun launchWallpaperChooser() {
        val intent: Intent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
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