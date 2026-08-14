package com.fireboost.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.view.InputDevice
import android.view.KeyEvent
import android.widget.*
import java.net.InetAddress
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val red = Color.rgb(235, 45, 62)
    private val dark = Color.rgb(8, 9, 12)
    private val panel = Color.rgb(18, 20, 25)
    private val white = Color.WHITE
    private val muted = Color.rgb(160, 166, 176)
    private lateinit var content: LinearLayout
    private var activeProfile = "Balanced"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark
        buildShell()
        showHome()
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(dark) }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(20, 22, 20, 14)
        }
        header.addView(TextView(this).apply { text = "◆"; textSize = 28f; setTextColor(red) })
        header.addView(TextView(this).apply {
            text = " FIREBOOST\n GAME LAUNCHER"; textSize = 18f; setTextColor(white)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(button("⚙", red) { showSettings() }, LinearLayout.LayoutParams(54, 54))
        root.addView(header)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 4, 16, 8) }
        root.addView(ScrollView(this).apply { addView(content) }, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { setPadding(8, 8, 8, 12); setBackgroundColor(panel) }
        listOf("HOME", "TOOLS", "MAPPER", "PROFILE", "BOOST").forEach { label ->
            nav.addView(button(label, if (label == "BOOST") red else Color.DKGRAY) {
                when (label) { "HOME" -> showHome(); "TOOLS" -> showTools(); "MAPPER" -> showMapper(); "PROFILE" -> showProfiles(); "BOOST" -> showBoost() }
            }, LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(3, 0, 3, 0) })
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun clear(title: String) {
        content.removeAllViews()
        content.addView(label(title, 26f, white))
    }

    private fun showHome() {
        clear("Welcome back")
        content.addView(label("Optimize. Launch. Play.", 15f, muted))
        content.addView(card("▶  LAUNCH FREE FIRE", "Detect installed Free Fire and start it", red) { launchFreeFire() })
        val r1 = LinearLayout(this); r1.addView(stat("TEMP", temperatureText()), weight()); r1.addView(stat("BATTERY", batteryText()), weight())
        val r2 = LinearLayout(this); r2.addView(stat("DISPLAY", displayText()), weight()); r2.addView(stat("NETWORK", networkText()), weight())
        content.addView(r1); content.addView(r2)
        content.addView(card("🎮  GAME TOOLS", "Mapper, profiles, diagnostics and utilities", red) { showTools() })
        content.addView(card("🔥  BOOST CENTER", "Run safe device-side gaming recommendations", red) { showBoost() })
        content.addView(label("Active profile: $activeProfile", 13f, muted))
    }

    private fun showTools() {
        clear("Game tools")
        content.addView(card("KEY MAPPER", "Keyboard • Mouse • Gamepad detection and mapping", red) { showMapper() })
        content.addView(card("PERFORMANCE", "RAM, display, battery and safe recommendations", red) { showPerformance() })
        content.addView(card("NETWORK TEST", "Connectivity and latency diagnostics", red) { showNetwork() })
        content.addView(card("COOLING / THERMAL", "Monitor battery temperature and thermal state", red) { showThermal() })
        content.addView(card("QUICK CLEANUP", "Open Android storage tools safely", red) { openStorage() })
        content.addView(card("FREE FIRE GUIDE", "Graphics, FPS and control recommendations", red) { showGuide() })
    }

    private fun showMapper() {
        clear("Key mapper")
        content.addView(label("Connected input devices", 16f, muted))
        val devices = InputDevice.getDeviceIds().mapNotNull { InputDevice.getDevice(it) }
        if (devices.isEmpty()) content.addView(label("No external HID devices detected.", 15f, muted))
        devices.forEach { d ->
            val s = d.sources
            val types = mutableListOf<String>()
            if (s and InputDevice.SOURCE_KEYBOARD != 0) types.add("Keyboard")
            if (s and InputDevice.SOURCE_MOUSE != 0) types.add("Mouse")
            if (s and InputDevice.SOURCE_GAMEPAD != 0 || s and InputDevice.SOURCE_JOYSTICK != 0) types.add("Gamepad")
            if (types.isEmpty()) types.add("Other HID")
            content.addView(deviceCard(d.name, types.joinToString(" • "), "ID ${d.id}"))
        }
        content.addView(label("Mapping editor", 19f, white))
        content.addView(label("Capture a physical key/button and save it to the active profile. This is an input configuration tool, not gameplay automation.", 14f, muted))
        val capture = Button(this).apply {
            text = "CAPTURE NEXT KEY / BUTTON"; setTextColor(white); setBackgroundColor(red); isFocusableInTouchMode = true
            setOnClickListener { requestFocus(); Toast.makeText(this@MainActivity, "Press a keyboard key or controller button…", Toast.LENGTH_SHORT).show() }
            setOnKeyListener { _, code, event ->
                if (event.action == KeyEvent.ACTION_DOWN) { text = "MAPPED: ${KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_")}"; true } else false
            }
        }
        content.addView(capture)
        content.addView(card("MOUSE / CAMERA NOTE", "Relative mouse-camera control depends on Android and the game accepting the corresponding HID input. FireBoost does not inject automated aim.", Color.DKGRAY) {})
        content.addView(card("OVERLAY PERMISSION", "Android may require overlay permission for an on-screen dashboard.", Color.DKGRAY) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        })
    }

    private fun showProfiles() {
        clear("Gaming profiles")
        val prefs = getSharedPreferences("profiles", MODE_PRIVATE)
        listOf("Balanced", "Rush", "Sniper", "Low-end", "Custom").forEach { name ->
            val saved = prefs.getString("$name.sensitivity", "Not set") ?: "Not set"
            content.addView(card(name, "Sensitivity: $saved", if (name == activeProfile) red else Color.DKGRAY) { activeProfile = name; showProfiles() })
        }
        content.addView(label("Sensitivity notes", 19f, white))
        val input = EditText(this).apply { hint = "General 95 • Red Dot 90 • 2x 80"; setTextColor(white); setHintTextColor(muted) }
        content.addView(input)
        content.addView(card("SAVE PROFILE", "Save notes to $activeProfile", red) {
            prefs.edit().putString("$activeProfile.sensitivity", input.text.toString().ifBlank { "Not set" }).apply()
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show(); showProfiles()
        })
    }

    private fun showBoost() {
        clear("Boost center")
        content.addView(label("Safe device checks", 16f, muted))
        listOf("Battery" to batteryText(), "Temperature" to temperatureText(), "Display" to displayText(), "Network" to networkText(), "Input devices" to "${InputDevice.getDeviceIds().size} detected").forEach { (a,b) -> content.addView(stat(a.uppercase(Locale.US), b)) }
        content.addView(card("APPLY RECOMMENDATIONS", "Open Android power controls without modifying game files", red) { startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)) })
        content.addView(label("Available controls vary by Android version and device manufacturer.", 13f, muted))
    }

    private fun showPerformance() {
        clear("Performance")
        content.addView(stat("RAM", "${availableRamMb()} MB currently available"))
        content.addView(stat("DISPLAY", displayText()))
        content.addView(stat("BATTERY", batteryText()))
        content.addView(card("BATTERY SETTINGS", "Use Android's official power controls", red) { startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)) })
        content.addView(label("Recommendations: close unused apps, keep storage free, avoid unnecessary charging heat, and use the phone's built-in game mode when available.", 14f, muted))
    }

    private fun showNetwork() {
        clear("Network diagnostics")
        content.addView(stat("CONNECTION", networkText()))
        val ping = label("Testing latency…", 18f, white); content.addView(ping)
        thread {
            val result = try { val start = System.currentTimeMillis(); InetAddress.getByName("1.1.1.1").isReachable(2500); System.currentTimeMillis() - start } catch (_: Exception) { -1L }
            runOnUiThread { ping.text = if (result >= 0) "Ping estimate: $result ms" else "Ping test unavailable on this network" }
        }
    }

    private fun showThermal() {
        clear("Cooling & thermal")
        content.addView(stat("BATTERY TEMPERATURE", temperatureText()))
        content.addView(stat("BATTERY", batteryText()))
        content.addView(label("Public Android APIs vary by manufacturer. FireBoost reports available data; it does not pretend to physically cool hardware.", 14f, muted))
    }

    private fun showGuide() {
        clear("Free Fire guide")
        content.addView(stat("LOW-END", "Smooth graphics • High/available FPS • Effects reduced"))
        content.addView(stat("MID-RANGE", "Standard graphics • High/available FPS • Moderate effects"))
        content.addView(stat("HIGH-END", "Higher graphics • Highest stable FPS available • Optional effects"))
        content.addView(label("Actual settings depend on the installed Free Fire version and device.", 13f, muted))
    }

    private fun showSettings() {
        clear("Settings")
        content.addView(card("OVERLAY", "Manage Android overlay permission", red) { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) })
        content.addView(card("BATTERY", "Open Android battery controls", red) { startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)) })
        content.addView(label("FireBoost • Android 8+ • Safe gaming utilities", 13f, muted))
    }

    private fun launchFreeFire() {
        val pkg = listOf("com.dts.freefireth", "com.dts.freefiremax").firstOrNull { packageManager.getLaunchIntentForPackage(it) != null }
        if (pkg == null) Toast.makeText(this, "Free Fire is not installed or not detected.", Toast.LENGTH_LONG).show()
        else startActivity(packageManager.getLaunchIntentForPackage(pkg))
    }

    private fun openStorage() {
        try { startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) }
        catch (_: Exception) { startActivity(Intent(Settings.ACTION_SETTINGS)) }
    }

    private fun temperatureText(): String {
        val i = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val t = i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        return if (t > 0) String.format(Locale.US, "%.1f°C", t / 10f) else "Unavailable"
    }

    private fun batteryText(): String {
        val i = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val l = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        return if (l >= 0) "$l%" else "Unavailable"
    }

    private fun networkText(): String {
        val cm = getSystemService(ConnectivityManager::class.java)
        val n = cm.activeNetwork ?: return "Offline"
        val c = cm.getNetworkCapabilities(n) ?: return "Unknown"
        return when { c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi‑Fi"; c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"; else -> "Connected" }
    }

    private fun displayText(): String = "${resources.displayMetrics.widthPixels}×${resources.displayMetrics.heightPixels}"
    private fun availableRamMb(): Long { val m = android.app.ActivityManager.MemoryInfo(); getSystemService(android.app.ActivityManager::class.java).getMemoryInfo(m); return m.availMem / 1024 / 1024 }
    private fun weight() = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(4, 4, 4, 4) }
    private fun label(s: String, size: Float, color: Int) = TextView(this).apply { text = s; textSize = size; setTextColor(color); setPadding(4, 8, 4, 8) }
    private fun button(s: String, color: Int, action: () -> Unit) = Button(this).apply { text = s; setTextColor(white); setBackgroundColor(color); setOnClickListener { action() } }
    private fun card(title: String, sub: String, color: Int, action: () -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(18, 14, 18, 14); setBackgroundColor(panel); isClickable = true; setOnClickListener { action() }
        addView(label(title, 17f, color)); addView(label(sub, 13f, muted)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 6, 0, 6) }
    }
    private fun stat(name: String, value: String) = card(name, value, red) {}
    private fun deviceCard(name: String, kind: String, id: String) = card(name, "$kind • $id", red) {}
}
