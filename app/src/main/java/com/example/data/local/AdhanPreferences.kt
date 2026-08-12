package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class AdhanPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isAdhanEnabledGlobal: Boolean
        get() = prefs.getBoolean(KEY_GLOBAL_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_GLOBAL_ENABLED, value).apply()

    fun isPrayerEnabled(prayerId: String): Boolean {
        // Default: Fajr, Dhuhr, Asr, Maghrib, Isha are enabled. Sunrise is disabled by default for full adhan.
        val defaultVal = prayerId.lowercase() != "sunrise"
        return prefs.getBoolean(KEY_PRAYER_ENABLED_PREFIX + prayerId.lowercase(), defaultVal)
    }

    fun setPrayerEnabled(prayerId: String, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRAYER_ENABLED_PREFIX + prayerId.lowercase(), enabled).apply()
    }

    var selectedMuezzinUrl: String
        get() = prefs.getString(KEY_MUEZZIN_URL, "https://download.quranicaudio.com/adhan/makkah.mp3") ?: "https://download.quranicaudio.com/adhan/makkah.mp3"
        set(value) = prefs.edit().putString(KEY_MUEZZIN_URL, value).apply()

    var selectedMuezzinName: String
        get() = prefs.getString(KEY_MUEZZIN_NAME, "أذان مكة المكرمة") ?: "أذان مكة المكرمة"
        set(value) = prefs.edit().putString(KEY_MUEZZIN_NAME, value).apply()

    var notificationMode: String
        get() = prefs.getString(KEY_NOTIFICATION_MODE, "صوت الأذان كامل") ?: "صوت الأذان كامل"
        set(value) = prefs.edit().putString(KEY_NOTIFICATION_MODE, value).apply()

    companion object {
        private const val PREF_NAME = "alufuq_adhan_prefs"
        private const val KEY_GLOBAL_ENABLED = "global_adhan_enabled"
        private const val KEY_PRAYER_ENABLED_PREFIX = "prayer_enabled_"
        private const val KEY_MUEZZIN_URL = "selected_muezzin_url"
        private const val KEY_MUEZZIN_NAME = "selected_muezzin_name"
        private const val KEY_NOTIFICATION_MODE = "notification_mode"
    }
}
