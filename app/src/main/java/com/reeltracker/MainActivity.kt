package com.reeltracker
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class BarView(c: Context, private val v: List<Int>, private val labels: List<String>, private val color: Int) : View(c) {
    override fun onDraw(cv: Canvas) {
        val max = (v.maxOrNull() ?: 1).coerceAtLeast(1); val w = width.toFloat() / v.size
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 24f; textAlign = Paint.Align.CENTER }
        for (i in v.indices) {
            val h = (height - 70f) * v[i] / max
            p.color = color; cv.drawRect(i * w + 3, height - 40 - h, (i + 1) * w - 3, height - 40f, p)
            p.color = Color.DKGRAY
            if (v[i] > 0) cv.drawText("${v[i]}", i * w + w / 2, height - 46 - h, p)
            cv.drawText(labels[i], i * w + w / 2, height - 10f, p)
        }
    }
}

class MainActivity : Activity() {
    private val apps = listOf("Instagram Reels" to "com.instagram.android", "YouTube Shorts" to "com.google.android.youtube")
    override fun onResume() { super.onResume(); setContentView(ScrollView(this).apply { addView(build()) }) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun t(s: String, size: Float = 16f, bold: Boolean = false) = TextView(this).apply {
        text = s; textSize = size; if (bold) typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(12), 0, dp(4)) }

    private fun build(): View {
        val db = Db(this); val limit = Prefs.limit(this)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(28), dp(16), dp(28)) }
        val week = db.events(Db.dayStart(-6)); val today = week.filter { it.first >= Db.dayStart() }
        col.addView(t("Today: ${today.size} / $limit reels", 28f, true))
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?.contains(packageName) == true
        if (!enabled) {
            col.addView(t("⚠ Tracking is OFF. Enable 'Reel Tracker' under Accessibility → Installed apps."))
            col.addView(Button(this).apply { text = "Open Accessibility settings"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
        } else col.addView(t("✅ Tracking is ON"))

        col.addView(t("Daily limit", 18f, true))
        val et = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER; setText("$limit") }
        col.addView(et)
        col.addView(Button(this).apply { text = "Save limit"; setOnClickListener {
            et.text.toString().toIntOrNull()?.let { Prefs.setLimit(this@MainActivity, it); recreate() } } })

        col.addView(t("Apps to track & block", 18f, true))
        val sel = Prefs.apps(this).toMutableSet()
        apps.forEach { (name, pkg) -> col.addView(CheckBox(this).apply { text = name; isChecked = pkg in sel
            setOnCheckedChangeListener { _, c -> if (c) sel.add(pkg) else sel.remove(pkg); Prefs.setApps(this@MainActivity, sel) } }) }

        val df = SimpleDateFormat("EEE", Locale.getDefault())
        val days = (0..6).map { i -> val s = Db.dayStart(-6 + i); week.count { it.first >= s && it.first < s + 86400000L } }
        val dl = (0..6).map { df.format(Date(Db.dayStart(-6 + it) + 3600000L)) }
        col.addView(t("Last 7 days", 18f, true))
        col.addView(BarView(this, days, dl, Color.parseColor("#E91E63")), LinearLayout.LayoutParams(-1, dp(180)))

        val hrs = IntArray(24); val cal = Calendar.getInstance()
        week.forEach { cal.timeInMillis = it.first; hrs[cal.get(Calendar.HOUR_OF_DAY)]++ }
        col.addView(t("When you watch (hour of day, last 7 days)", 18f, true))
        col.addView(BarView(this, hrs.toList(), (0..23).map { if (it % 3 == 0) "$it" else "" }, Color.parseColor("#3F51B5")), LinearLayout.LayoutParams(-1, dp(180)))

        col.addView(t("Insights", 18f, true))
        val peak = hrs.indices.maxByOrNull { hrs[it] }
        val sb = StringBuilder()
        sb.append("Average: ${week.size / 7} reels/day\n")
        if (week.isNotEmpty() && peak != null) sb.append("Peak hour: %02d:00–%02d:00 (${hrs[peak]} reels)\n".format(peak, (peak + 1) % 24))
        today.lastOrNull()?.let { sb.append("Last reel today: ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it.first))}\n") }
        if (today.isNotEmpty()) sb.append("First reel today: ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(today.first().first))}\n")
        today.groupBy { it.second }.forEach { (p, l) -> sb.append("${apps.firstOrNull { it.second == p }?.first ?: p}: ${l.size} today\n") }
        col.addView(t(sb.toString()))
        return col
    }
}
