package com.lavie.randochat.service

import android.content.Context
import com.lavie.randochat.utils.Constants
import androidx.core.content.edit

class SharedPreferencesService(context: Context) : PreferencesService {
    private val prefs = context.getSharedPreferences(Constants.APP_PREFS, Context.MODE_PRIVATE)

    override fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    override fun getString(key: String, default: String?): String? {
        return prefs.getString(key, default)
    }

    override fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    override fun getInt(key: String, default: Int): Int {
        return prefs.getInt(key, default)
    }

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return prefs.getBoolean(key, default)
    }

    override fun putFloat(key: String, value: Float) {
        prefs.edit { putFloat(key, value) }
    }

    override fun getFloat(key: String, default: Float): Float {
        return prefs.getFloat(key, default)
    }

    override fun putLong(key: String, value: Long) {
        prefs.edit { putLong(key, value) }
    }

    override fun getLong(key: String, default: Long): Long {
        return prefs.getLong(key, default)
    }

    override fun putStringSet(key: String, value: Set<String>) {
        prefs.edit { putStringSet(key, value) }
    }

    override fun getStringSet(key: String, default: Set<String>): Set<String> {
        return prefs.getStringSet(key, default) ?: default
    }

    override fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    override fun clear() {
        prefs.edit { clear() }
    }
}
