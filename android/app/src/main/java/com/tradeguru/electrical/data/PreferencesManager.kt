package com.tradeguru.electrical.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tradeguru_prefs")

object PrefsKeys {
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    val HAS_ACCEPTED_DISCLAIMER = booleanPreferencesKey("has_accepted_disclaimer")
    val SELECTED_MODE = stringPreferencesKey("selected_mode")
}

class PreferencesManager(private val context: Context) {

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[PrefsKeys.HAS_COMPLETED_ONBOARDING] ?: false }

    val hasAcceptedDisclaimer: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[PrefsKeys.HAS_ACCEPTED_DISCLAIMER] ?: false }

    val selectedMode: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[PrefsKeys.SELECTED_MODE] ?: "fault_finder" }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.HAS_ACCEPTED_DISCLAIMER] = accepted
        }
    }

    suspend fun setSelectedMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.SELECTED_MODE] = mode
        }
    }
}
