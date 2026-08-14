package com.fireboost.launcher

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.View
import android.widget.*
import java.net.InetAddress
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val red = Color.rgb(245, 48, 66)
    private val redDark = Color.rgb(135, 22, 34)
    private val bg = Color.rgb(7, 8, 11)
    private val panel = Color.rgb(18, 20, 25)
    private val panel2 = Color.rgb(24, 26, 32)
    private val white = Color.WHITE
    private val muted = Color.rgb(163, 169, 180)
    private lateinit var content: LinearLayout
    private var activeProfile = "Balanced"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0
        buildShell()
        showHome()
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(10))
        }
        header.addView(TextView(this).apply {
            text = "F"; textSize = 30f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white); gravity = Gravity.CENTER
            background = rounded(red, 18f)
        }, LinearLayout.LayoutParams(dp(50), dp(50)))
        header.addView(TextView(this).apply {
            text = "  FIREBOOST\n  GAMING CONTROL CENTER"; textSize = 16f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(button("⚙", red, dp(52), dp(52)) { showSettings() })
        root.addView(header)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(4), dp(16), dp(12)) }
        root.addView(ScrollView(this).apply { isFillViewport = true; addView(content) }, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { setPadding(dp(7), dp(7), dp(7), dp(10)); setBackgroundColor(panel) }
        listOf("HOME", "TOOLS", "MAPPER", "PROFILE", "BOOST").forEach { label ->
            nav.addView(button(label, if (label == "BOOST") red else panel2, 0, dp(52)) {
                when (label) { "HOME" -> showHome(); "TOOLS" -> showTools(); "MAPPER" -> showMapper(); "PROFILE" -> showProfiles(); "BOOST" -> showBoost() }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(3), 0, dp(3), 0) })
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun clear(title: String, subtitle: String? = null) {
        content.removeAllViews()
        content.addView(label(title, 28f, white, true))
        subtitle?.let { content.addView(label(it, 14f, muted)) }
    }

    private fun showHome() {
        clear("Welcome back", "Optimize. Launch. Play.")
        val detected = freeFirePackage() != null
        content.addView(hero(detected))
        content.addView(section("LIVE DEVICE STATUS"))
        val row1 = LinearLayout(this); row1.addView(stat("BATTERY", batteryText()), weight()); row1.addView(stat("TEMP", temperatureText()), weight())
        val row2 = LinearLayout(this); row2.addView(stat("RAM", "${availableRamMb()} MB free"), weight()); row2.addView(stat("NETWORK", networkText()), weight())
        content.addView(row1); content.addView(row2)
        content.addView(section("QUICK ACCESS"))
        content.addView(twoCards(
            card("KEY MAPPER", "Keyboard • mouse • gamepad", red) { showMapper() },
            card("PERFORMANCE", "Device optimization", red) { showPerformance() }
        ))
        content.addView(twoCards(
            card("NETWORK", "Ping & connection test", red) { showNetwork() },
            card("PROFILES", "Sensitivity & controls", red) { showProfiles() }
        ))
        content.addView(card("🎮  GAMING DASHBOARD", "Floating controls require Android overlay permission", red) { startOverlay() })
    }

    private fun hero(installed: Boolean): View {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(20), dp(20), dp(20)); background = rounded(panel2, 24f) }
        box.addView(label("FIREBOOST", 13f, red, true))
        box.addView(label("Dominate your setup.", 25f, white, true))
        box.addView(label(if (installed) "Free Fire detected and ready to launch." else "Free Fire not detected on this device.", 14f, muted))
        val b = button(if (installed) "▶  LAUNCH FREE FIRE" else "CHECK FOR FREE FIRE", red, 0, dp(56)) { launchFreeFire() }
        box.addView(b, LinearLayout.LayoutParams(-1, dp(56)).apply { topMargin = dp(14) })
        box.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(8), 0, dp(8)) }
        return box
    }

    private fun showTools() {
        clear("Game tools", "Everything FireBoost can safely do on Android")
        content.addView(card("KEY MAPPER", "Detect keyboard, mouse and gamepad devices; capture keys/buttons into profiles", red) { showMapper() })
        content.addView(card("PERFORMANCE", "RAM, display, battery and safe gaming recommendations", red) { showPerformance() })
        content.addView(card("NETWORK TEST", "Connection type, reachability and latency diagnostics", red) { showNetwork() })
        content.addView(card("COOLING / THERMAL", "Battery temperature and thermal information", red) { showThermal() })
        content.addView(card("QUICK CLEANUP", "Open Android storage controls without deleting personal data", red) { openStorage() })
        content.addView(card("FREE FIRE GUIDE", "Low-end, mid-range and high-end graphics/FPS recommendations", red) { showGuide() })
        content.addView(card("GAMING DASHBOARD", "Floating dashboard for supported Android devices", red) { startOverlay() })
    }

    private fun showMapper() {
        clear("Key mapper", "Physical input detection and profile mapping")
        val devices = InputDevice.getDeviceIds().mapNotNull { InputDevice.getDevice(it) }
        content.addView(section("CONNECTED DEVICES • ${devices.size}"))
        if (devices.isEmpty()) content.addView(card("NO EXTERNAL DEVICE", "Connect a USB/Bluetooth keyboard, mouse or controller and reopen this page", Color.DKGRAY) {})
        devices.forEach { d ->
            val sources = d.sources
            val types = mutableListOf<String>()
            if (sources and InputDevice.SOURCE_KEYBOARD != 0) types.add("Keyboard")
            if (sources and InputDevice.SOURCE_MOUSE != 0) types.add("Mouse")
            if (sources and InputDevice.SOURCE_GAMEPAD != 0 || sources and InputDevice.SOURCE_JOYSTICK != 0) types.add("Gamepad")
            if (types.isEmpty()) types.add("Other HID")
            val details = buildString {
                append(types.joinToString(" • ")); append("\nID ${d.id} • Vendor ${d.vendorId} • Product ${d.productId}")
                if (sources and InputDevice.SOURCE_MOUSE != 0) append("\nRelative pointer device detected")
            }
            content.addView(card(d.name, details, red) {})
        }
        content.addView(section("MAPPING EDITOR"))
        content.addView(label("Capture a physical key or controller button and save it as a note in the active profile. FireBoost does not automate gameplay or inject aim/recoil actions.", 14f, muted))
        val capture = Button(this).apply {
            text = "CAPTURE NEXT KEY / BUTTON"; setTextColor(white); background = rounded(red, 16f); isFocusableInTouchMode = true
            setOnClickListener { requestFocus(); Toast.makeText(this@MainActivity, "Press a keyboard key or controller button…", Toast.LENGTH_SHORT).show() }
            setOnKeyListener { _, code, event ->
                if (event.action == KeyEvent.ACTION_DOWN) { text = "MAPPED: ${KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_")}"; saveMapping(text.toString()); true } else false
            }
        }
        content.addView(capture, LinearLayout.LayoutParams(-1, dp(54)).apply { setMargins(0, dp(8), 0, dp(8)) })
        content.addView(card("MOUSE SETTINGS", "Detected mice can be classified by available HID information. DPI/polling rate are shown only when the device exposes them; FireBoost never invents values.", red) { showMouseInfo() })
        content.addView(card("CAMERA CONTROL", "Relative mouse-camera behavior depends on Android and the game accepting HID relative input. FireBoost does not inject automated aiming.", Color.DKGRAY) {})
    }

    private fun showMouseInfo() {
        clear("Mouse settings", "Device-specific information and smooth-control recommendations")
        val mice = InputDevice.getDeviceIds().mapNotNull { InputDevice.getDevice(it) }.filter { it.sources and InputDevice.SOURCE_MOUSE != 0 }
        if (mice.isEmpty()) { content.addView(card("NO MOUSE DETECTED", "Connect a mouse and return here", Color.DKGRAY) {}); return }
        mice.forEach { d ->
            val range = d.motionRanges.firstOrNull { it.axis == android.view.MotionEvent.AXIS_X }
            val desc = "${d.name}\nVendor ${d.vendorId} • Product ${d.productId}\nX range: ${range?.min ?: "unknown"} → ${range?.max ?: "unknown"}"
            content.addView(card("MOUSE", desc, red) {})
            content.addView(label("Recommended baseline: medium pointer speed, disable acceleration if your Android device exposes that control, and tune in-game sensitivity separately.", 13f, muted))
        }
    }

    private fun saveMapping(value: String) {
        getSharedPreferences("profiles", MODE_PRIVATE).edit().putString("$activeProfile.mapping", value).apply()
        Toast.makeText(this, "Saved to $activeProfile", Toast.LENGTH_SHORT).show()
    }

    private fun showProfiles() {
        clear("Gaming profiles", "Save sensitivity notes and control mappings")
        val prefs = getSharedPreferences("profiles", MODE_PRIVATE)
        listOf("Balanced", "Rush", "Sniper", "Low-end", "Custom").forEach { name ->
            val saved = prefs.getString("$name.sensitivity", "Not set") ?: "Not set"
            content.addView(card(if (name == activeProfile) "✓ $name" else name, "Sensitivity: $saved\nMapping: ${prefs.getString("$name.mapping", "Not set")}", if (name == activeProfile) red else Color.DKGRAY) { activeProfile = name; showProfiles() })
        }
        val input = EditText(this).apply { hint = "General 95 • Red Dot 90 • 2x 80"; setTextColor(white); setHintTextColor(muted); setPadding(dp(14), dp(10), dp(14), dp(10)); background = rounded(panel2, 14f) }
        content.addView(input)
        content.addView(card("SAVE SENSITIVITY NOTES", "Save to $activeProfile", red) {
            prefs.edit().putString("$activeProfile.sensitivity", input.text.toString().ifBlank { "Not set" }).apply(); Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show(); showProfiles()
        })
    }

    private fun showBoost() {
        clear("Boost center", "Safe checks before you launch")
        listOf("Battery" to batteryText(), "Temperature" to temperatureText(), "RAM" to "${availableRamMb()} MB free", "Network" to networkText(), "Input devices" to "${InputDevice.getDeviceIds().size} detected").forEach { (a, b) -> content.addView(stat(a.uppercase(Locale.US), b)) }
        content.addView(card("APPLY SAFE RECOMMENDATIONS", "Open Android battery controls; no game files are modified", red) { startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)) })
        content.addView(card("LAUNCH FREE FIRE", "Start the detected Free Fire package", red) { launchFreeFire() })
    }

    private fun showPerformance() {
        clear("Performance", "Device-side information and safe recommendations")
        content.addView(stat("RAM", "${availableRamMb()} MB currently available"))
        content.addView(stat("DISPLAY", displayText()))
        content.addView(stat("BATTERY", batteryText()))
        content.addView(stat("THERMAL", temperatureText()))
        content.addView(card("ANDROID POWER CONTROLS", "Use the phone's official performance/battery settings", red) { startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)) })
        content.addView(label("For best gaming stability: keep storage free, close unused apps, avoid excessive heat and use the device manufacturer's built-in game mode when available.", 14f, muted))
    }

    private fun showNetwork() {
        clear("Network diagnostics", "Connection and latency test")
        content.addView(stat("CONNECTION", networkText()))
        val ping = label("Testing Cloudflare reachability…", 18f, white, true); content.addView(ping)
        thread {
            val result = try { val start = System.currentTimeMillis(); InetAddress.getByName("1.1.1.1").isReachable(2500); System.currentTimeMillis() - start } catch (_: Exception) { -1L }
            runOnUiThread { ping.text = if (result >= 0) "Reachability estimate: $result ms" else "Ping test unavailable on this network" }
        }
    }

    private fun showThermal() {
        clear("Cooling & thermal", "Monitor available thermal information")
        content.addView(stat("BATTERY TEMPERATURE", temperatureText()))
        content.addView(stat("BATTERY", batteryText()))
        content.addView(label("Android exposes different thermal APIs on different manufacturers. FireBoost reports available data and recommendations; it cannot physically cool hardware.", 14f, muted))
    }

    private fun showGuide() {
        clear("Free Fire guide", "Starting points; tune for your device and preference")
        content.addView(stat("LOW-END", "Smooth graphics • High/available FPS • Effects reduced"))
        content.addView(stat("MID-RANGE", "Standard graphics • High/available FPS • Moderate effects"))
        content.addView(stat("HIGH-END", "Higher graphics • Highest stable FPS available • Optional effects"))
        content.addView(label("Settings vary by installed Free Fire version. FireBoost does not modify protected game files.", 13f, muted))
    }

    private fun showSettings() {
        clear("Settings", "FireBoost configuration")
        content.addView(card("FLOATING DASHBOARD", "Open Android overlay permission", red) { openOverlaySettings() })
        content.addView(card("BATTERY", "Open Android battery controls", red) { startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)) })
        content.addView(card("APP INFO", "Android application details", red) { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:$packageName"))) })
        content.addView(label("FireBoost • Android 8+ • Safe gaming utilities\nNo cheats, anti-cheat bypasses or automated aim.", 13f, muted))
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) { openOverlaySettings(); return }
        val intent = Intent(this, OverlayDashboardService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
        Toast.makeText(this, "Gaming dashboard started", Toast.LENGTH_SHORT).show()
    }

    private fun openOverlaySettings() {
        try { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))) }
        catch (_: Exception) { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) }
    }

    private fun launchFreeFire() {
        val pkg = freeFirePackage()
        if (pkg == null) Toast.makeText(this, "Free Fire is not installed or not detected.", Toast.LENGTH_LONG).show()
        else startActivity(packageManager.getLaunchIntentForPackage(pkg))
    }

    private fun freeFirePackage(): String? = listOf("com.dts.freefireth", "com.dts.freefiremax").firstOrNull { packageManager.getLaunchIntentForPackage(it) != null }

    private fun openStorage() { try { startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) } catch (_: Exception) { startActivity(Intent(Settings.ACTION_SETTINGS)) } }

    private fun temperatureText(): String {
        val i = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)); val t = i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        return if (t > 0) String.format(Locale.US, "%.1f°C", t / 10f) else "Unavailable"
    }

    private fun batteryText(): String {
        val i = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)); val l = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        return if (l >= 0) "$l%" else "Unavailable"
    }

    private fun networkText(): String {
        val cm = getSystemService(ConnectivityManager::class.java); val n = cm.activeNetwork ?: return "Offline"; val c = cm.getNetworkCapabilities(n) ?: return "Unknown"
        return when { c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi‑Fi"; c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"; else -> "Connected" }
    }

    private fun displayText(): String = "${resources.displayMetrics.widthPixels} × ${resources.displayMetrics.heightPixels}"
    private fun availableRamMb(): Long { val m = ActivityManager.MemoryInfo(); getSystemService(ActivityManager::class.java).getMemoryInfo(m); return m.availMem / 1024 / 1024 }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun weight() = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), dp(4), dp(4), dp(4)) }
    private fun section(s: String) = label(s, 12f, red, true).apply { setPadding(dp(4), dp(14), dp(4), dp(6)) }
    private fun label(s: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply { text = s; textSize = size; setTextColor(color); if (bold) typeface = Typeface.DEFAULT_BOLD; setPadding(dp(4), dp(6), dp(4), dp(6)) }
    private fun button(s: String, color: Int, w: Int, h: Int, action: () -> Unit) = Button(this).apply { text = s; textSize = 11f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white); background = rounded(color, 14f); setOnClickListener { action() }; if (w > 0) layoutParams = LinearLayout.LayoutParams(w, h) }
    private fun card(title: String, sub: String, color: Int, action: () -> Unit) = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(13), dp(16), dp(13)); background = rounded(panel, 18f); isClickable = true; setOnClickListener { action() }; addView(label(title, 16f, color, true)); addView(label(sub, 13f, muted)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(5), 0, dp(5)) } }
    private fun stat(name: String, value: String) = card(name, value, red) {}
    private fun twoCards(a: View, b: View): View { val row = LinearLayout(this); row.addView(a, LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(0, 0, dp(4), 0) }); row.addView(b, LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), 0, 0, 0) }); return row }
    private fun rounded(color: Int, radius: Float) = android.graphics.drawable.GradientDrawable().apply { setColor(color); cornerRadius = dp(radius.toInt()).toFloat() }
}
