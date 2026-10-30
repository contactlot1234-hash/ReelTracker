package com.reeltracker
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class BlockActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val l = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(Color.parseColor("#B71C1C")); setPadding(60, 60, 60, 60) }
        l.addView(TextView(this).apply { text = "🚫 Daily limit reached"; textSize = 28f; setTextColor(Color.WHITE); gravity = Gravity.CENTER })
        l.addView(TextView(this).apply { text = "You've watched ${Prefs.limit(this@BlockActivity)} reels today.\nThese apps are locked until tomorrow."; textSize = 16f; setTextColor(Color.WHITE); gravity = Gravity.CENTER; setPadding(0, 30, 0, 50) })
        l.addView(Button(this).apply { text = "Go home"; setOnClickListener { home() } })
        setContentView(l)
    }
    private fun home() { startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); finish() }
    @Deprecated("") override fun onBackPressed() = home()
}
