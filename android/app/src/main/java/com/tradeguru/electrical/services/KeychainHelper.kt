package com.tradeguru.electrical.services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

class KeychainHelper(private val context: Context) {
    private val gson = Gson()

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun save(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun <T> save(key: String, value: T, clazz: Class<T>) {
        val json = gson.toJson(value)
        prefs.edit().putString(key, json).apply()
    }

    fun load(key: String): String? {
        return prefs.getString(key, null)
    }

    fun <T> load(key: String, clazz: Class<T>): T? {
        val json = prefs.getString(key, null) ?: return null
        return try {
            gson.fromJson(json, clazz)
        } catch (_: Exception) {
            null
        }
    }

    fun delete(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "com.tradeguru.electrical.secure"
        private const val FALLBACK_PREFS_NAME = "com.tradeguru.electrical.secure_fallback"
    }
}
