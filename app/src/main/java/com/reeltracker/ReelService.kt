package com.reeltracker
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import android.widget.Toast

class ReelService : AccessibilityService() {
    // View IDs that identify the Reels/Shorts screen. Apps change these between versions - edit if counting stops.
    private val reelIds = mapOf(
        "com.instagram.android" to listOf("clips_viewer_view_pager", "clips_viewer_clip_tab", "root_clips_layout"),
        "com.google.android.youtube" to listOf("reel_recycler", "reel_player_page_container", "shorts_container")
    )
    private lateinit var db: Db
    private var tv: TextView? = null
    private var inReel = false
    private var lastCount = 0L; private var lastCheck = 0L; private var lastBlock = 0L

    override fun onServiceConnected() { db = Db(this) }
    override fun onInterrupt() {}

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        val pkg = e.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg !in Prefs.apps(this)) {
            if (e.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !pkg.contains("systemui") && !pkg.contains("inputmethod")) hide()
            return
        }
        val limit = Prefs.limit(this)
        val used = db.countSince(Db.dayStart())
        if (used >= limit) { block(); return }
        val now = System.currentTimeMillis()
        val scroll = e.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
        if (!scroll && now - lastCheck < 300) return
        lastCheck = now
        val root = rootInActiveWindow ?: return
        val onReel = reelIds[pkg]?.any { root.findAccessibilityNodeInfosByViewId("$pkg:id/$it").isNotEmpty() } == true
        if (!onReel) { hide(); return }
        var n = used
        val entering = !inReel
        inReel = true
        if ((scroll || entering) && now - lastCount > 800) { db.add(pkg); lastCount = now; n++ }
        show(n, limit)
        if (n >= limit) block()
    }

    private fun block() {
        val now = System.currentTimeMillis()
        if (now - lastBlock < 2000) return
        lastBlock = now
        hide()
        performGlobalAction(GLOBAL_ACTION_HOME)
        Toast.makeText(this, "Daily reel limit reached", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, BlockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun show(n: Int, l: Int) {
        if (tv == null) {
            val t = TextView(this).apply {
                setTextColor(Color.WHITE); textSize = 16f; setPadding(32, 16, 32, 16)
                background = GradientDrawable().apply { setColor(0xCC000000.toInt()); cornerRadius = 40f }
            }
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x = 24; y = 250 }
            (getSystemService(WINDOW_SERVICE) as WindowManager).addView(t, lp)
            tv = t
        }
        tv?.text = "🎬 $n / $l"
    }

    private fun hide() {
        tv?.let { try { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) } catch (_: Exception) {} }
        tv = null; inReel = false
    }
}
