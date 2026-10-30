package com.reeltracker
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("p", 0)
    fun limit(c: Context) = sp(c).getInt("limit", 50)
    fun setLimit(c: Context, v: Int) = sp(c).edit().putInt("limit", v).apply()
    fun apps(c: Context): Set<String> =
        sp(c).getStringSet("apps", setOf("com.instagram.android", "com.google.android.youtube"))!!.toSet()
    fun setApps(c: Context, s: Set<String>) = sp(c).edit().putStringSet("apps", s.toSet()).apply()
}

class Db(c: Context) : SQLiteOpenHelper(c, "reels.db", null, 1) {
    override fun onCreate(d: SQLiteDatabase) { d.execSQL("CREATE TABLE ev(ts INTEGER, pkg TEXT)") }
    override fun onUpgrade(d: SQLiteDatabase, o: Int, n: Int) {}
    fun add(pkg: String) { writableDatabase.execSQL("INSERT INTO ev VALUES(?,?)", arrayOf<Any>(System.currentTimeMillis(), pkg)) }
    fun countSince(t: Long): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM ev WHERE ts>=?", arrayOf(t.toString())).use { it.moveToFirst(); it.getInt(0) }
    fun events(since: Long): List<Pair<Long, String>> =
        readableDatabase.rawQuery("SELECT ts,pkg FROM ev WHERE ts>=? ORDER BY ts", arrayOf(since.toString())).use {
            val l = ArrayList<Pair<Long, String>>(); while (it.moveToNext()) l.add(it.getLong(0) to it.getString(1)); l
        }
    companion object {
        fun dayStart(offset: Int = 0): Long = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, offset)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
