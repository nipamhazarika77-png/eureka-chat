package com.example

import android.content.Context
import android.content.SharedPreferences

object SecurityPreferences {
    private const val PREF_NAME = "eureka_security_prefs"
    private const val KEY_FINGERPRINT_LOCK = "fingerprint_lock_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return getPrefsInternal(context)
    }

    private fun getPrefsInternal(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFingerprintLockEnabled(context: Context): Boolean {
        return getPrefsInternal(context).getBoolean(KEY_FINGERPRINT_LOCK, false)
    }

    fun setFingerprintLockEnabled(context: Context, enabled: Boolean) {
        getPrefsInternal(context).edit().putBoolean(KEY_FINGERPRINT_LOCK, enabled).apply()
    }
}
