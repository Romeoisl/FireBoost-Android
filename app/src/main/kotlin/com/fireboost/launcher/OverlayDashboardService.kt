package com.fireboost.launcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class OverlayDashboardService : Service() {
    private var windowManager: WindowManager? = null
    private var overlay: LinearLayout? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(4127, notification())
        showOverlay()
    }

    private fun showOverlay() {
        if (!android.provider.Settings.canDrawOverlays(this)) { stopSelf(); return }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 14, 18, 14)
            setBackgroundColor(Color.rgb(18, 20, 25))
        }
        val title = TextView(this).apply { text = "FIREBOOST  •  GAMING DASHBOARD"; textSize = 12f; setTextColor(Color.rgb(245,48,66)) }
        val status = TextView(this).apply { text = "Battery • Temperature • Input"; textSize = 12f; setTextColor(Color.WHITE); setPadding(0,8,0,8) }
        val close = Button(this).apply { text = "CLOSE"; setOnClickListener { stopSelf() } }
        root.addView(title); root.addView(status); root.addView(close)
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 12; y = 70 }
        overlay = root
        windowManager?.addView(root, params)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel("fireboost", "FireBoost Gaming Dashboard", NotificationManager.IMPORTANCE_LOW))
        }
    }

    private fun notification(): Notification {
        return if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "fireboost").setContentTitle("FireBoost dashboard active").setContentText("Gaming overlay is running").setSmallIcon(android.R.drawable.ic_menu_compass).build()
        else Notification.Builder(this).setContentTitle("FireBoost dashboard active").setContentText("Gaming overlay is running").setSmallIcon(android.R.drawable.ic_menu_compass).build()
    }

    override fun onDestroy() {
        overlay?.let { try { windowManager?.removeView(it) } catch (_: Exception) {} }
        overlay = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
