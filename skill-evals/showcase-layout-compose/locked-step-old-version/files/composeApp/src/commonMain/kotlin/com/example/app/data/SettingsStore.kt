package com.example.app.data

/** Key-value settings, backed by DataStore on Android and NSUserDefaults on iOS in the real app. */
interface SettingsStore {
    suspend fun getBoolean(key: String, default: Boolean = false): Boolean
    suspend fun putBoolean(key: String, value: Boolean)
}

/** Used by previews and tests. */
class InMemorySettingsStore : SettingsStore {
    private val values = mutableMapOf<String, Boolean>()
    override suspend fun getBoolean(key: String, default: Boolean) = values[key] ?: default
    override suspend fun putBoolean(key: String, value: Boolean) { values[key] = value }
}
