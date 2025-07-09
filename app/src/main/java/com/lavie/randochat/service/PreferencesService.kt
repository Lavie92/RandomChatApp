package com.lavie.randochat.service

interface PreferencesService {
    fun putString(key: String, value: String)
    fun getString(key: String, default: String? = null): String?

    fun putInt(key: String, value: Int)
    fun getInt(key: String, default: Int = 0): Int

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, default: Boolean = false): Boolean

    fun putFloat(key: String, value: Float)
    fun getFloat(key: String, default: Float = 0f): Float

    fun putLong(key: String, value: Long)
    fun getLong(key: String, default: Long = 0L): Long

    fun putStringSet(key: String, value: Set<String>)
    fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String>

    fun remove(key: String)
    fun clear()
}
