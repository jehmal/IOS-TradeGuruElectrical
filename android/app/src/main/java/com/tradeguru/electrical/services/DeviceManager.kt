package com.tradeguru.electrical.services

import android.content.Context
import android.preference.PreferenceManager
import java.util.UUID

class DeviceManager(private val keychainHelper: KeychainHelper) {
    @Volatile
    private var inMemoryId: String? = null

    fun getOrCreateDeviceId(): String {
        keychainHelper.load(SECURE_KEY)?.let { return it }

        val newId = UUID.randomUUID().toString()
        keychainHelper.save(SECURE_KEY, newId)
        inMemoryId = newId
        return newId
    }

    @Suppress("DEPRECATION")
    fun deviceIdOrFallback(context: Context): String {
        keychainHelper.load(SECURE_KEY)?.let { return it }

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.getString(FALLBACK_KEY, null)
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        inMemoryId?.let { return it }

        val newId = UUID.randomUUID().toString()
        keychainHelper.save(SECURE_KEY, newId)
        prefs.edit().putString(FALLBACK_KEY, newId).apply()
        inMemoryId = newId
        return newId
    }

    fun save(deviceId: String) {
        keychainHelper.save(SECURE_KEY, deviceId)
        inMemoryId = deviceId
    }

    fun save(deviceId: String, context: Context) {
        keychainHelper.save(SECURE_KEY, deviceId)
        @Suppress("DEPRECATION")
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putString(FALLBACK_KEY, deviceId).apply()
        inMemoryId = deviceId
    }

    companion object {
        private const val SECURE_KEY = "device_id"
        private const val FALLBACK_KEY = "tradeguru_device_id_fallback"
    }
}
