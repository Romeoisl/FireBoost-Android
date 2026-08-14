package com.fireboost.launcher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val freeFirePackages = listOf("com.dts.freefireth", "com.dts.freefiremax")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 56, 40, 40)
            setBackgroundColor(0xFF080808.toInt())
        }

        val title = TextView(this).apply {
            text = "🔥 FIREBOOST"
            textSize = 30f
            setTextColor(0xFFFFFFFF.toInt())
        }
        val subtitle = TextView(this).apply {
            text = "Free Fire Game Launcher"
            textSize = 16f
            setTextColor(0xFFE50914.toInt())
        }
        val info = TextView(this).apply {
            text = "Android ${android.os.Build.VERSION.RELEASE} • ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            textSize = 14f
            setTextColor(0xFFD0D0D0.toInt())
            setPadding(0, 24, 0, 24)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(info)
        root.addView(actionButton("LAUNCH FREE FIRE") { launchFreeFire() })
        root.addView(actionButton("GAME TOOLS") {
            Toast.makeText(this, "Game Tools coming next.", Toast.LENGTH_SHORT).show()
        })
        root.addView(actionButton("KEY MAPPER") {
            Toast.makeText(this, "Keyboard, mouse and gamepad mapper coming next.", Toast.LENGTH_SHORT).show()
        })
        root.addView(actionButton("PERFORMANCE MODE") {
            Toast.makeText(this, "Safe Android performance controls coming next.", Toast.LENGTH_SHORT).show()
        })
        root.addView(actionButton("BATTERY / COOLING") {
            startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
        })
        setContentView(root)
    }

    private fun actionButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        setOnClickListener { action() }
    }

    private fun launchFreeFire() {
        val intent = freeFirePackages.asSequence()
            .mapNotNull { packageManager.getLaunchIntentForPackage(it) }
            .firstOrNull()

        if (intent == null) {
            Toast.makeText(this, "Free Fire is not installed.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Unable to launch Free Fire.", Toast.LENGTH_LONG).show()
        }
    }
}
