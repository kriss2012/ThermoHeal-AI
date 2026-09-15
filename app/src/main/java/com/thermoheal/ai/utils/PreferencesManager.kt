package com.thermoheal.ai.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "thermoheal_prefs")

/**
 * Single source of truth for everything that must survive process death
 * and app restarts (spec section 90): login session, theme, units,
 * notification prefs, demo-mode/simulation settings, research mode flag.
 */
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    private object Keys {
        val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        val THEME_MODE = stringPreferencesKey("theme_mode") // LIGHT / DARK / SYSTEM
        val UNIT_SYSTEM = stringPreferencesKey("unit_system") // METRIC / IMPERIAL
        val TEMP_UNIT = stringPreferencesKey("temperature_unit") // CELSIUS / FAHRENHEIT
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DEMO_MODE_ENABLED = booleanPreferencesKey("demo_mode_enabled")
        val RESEARCH_MODE_ENABLED = booleanPreferencesKey("research_mode_enabled")
        val DEMO_SCENARIO = stringPreferencesKey("demo_scenario")
        val DEMO_SPEED = stringPreferencesKey("demo_speed")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val currentUserId: Flow<String?> = dataStore.data.map { it[Keys.CURRENT_USER_ID] }
    suspend fun setCurrentUserId(id: String?) = dataStore.edit {
        if (id == null) it.remove(Keys.CURRENT_USER_ID) else it[Keys.CURRENT_USER_ID] = id
    }

    val themeMode: Flow<String> = dataStore.data.map { it[Keys.THEME_MODE] ?: "SYSTEM" }
    suspend fun setThemeMode(mode: String) = dataStore.edit { it[Keys.THEME_MODE] = mode }

    val unitSystem: Flow<String> = dataStore.data.map { it[Keys.UNIT_SYSTEM] ?: "METRIC" }
    suspend fun setUnitSystem(system: String) = dataStore.edit { it[Keys.UNIT_SYSTEM] = system }

    val temperatureUnit: Flow<String> = dataStore.data.map { it[Keys.TEMP_UNIT] ?: "CELSIUS" }
    suspend fun setTemperatureUnit(unit: String) = dataStore.edit { it[Keys.TEMP_UNIT] = unit }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    suspend fun setNotificationsEnabled(enabled: Boolean) = dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }

    val demoModeEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.DEMO_MODE_ENABLED] ?: false }
    suspend fun setDemoModeEnabled(enabled: Boolean) = dataStore.edit { it[Keys.DEMO_MODE_ENABLED] = enabled }

    val researchModeEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.RESEARCH_MODE_ENABLED] ?: false }
    suspend fun setResearchModeEnabled(enabled: Boolean) = dataStore.edit { it[Keys.RESEARCH_MODE_ENABLED] = enabled }

    val demoScenario: Flow<String> = dataStore.data.map { it[Keys.DEMO_SCENARIO] ?: "NORMAL" }
    suspend fun setDemoScenario(scenario: String) = dataStore.edit { it[Keys.DEMO_SCENARIO] = scenario }

    val demoSpeed: Flow<String> = dataStore.data.map { it[Keys.DEMO_SPEED] ?: "NORMAL" }
    suspend fun setDemoSpeed(speed: String) = dataStore.edit { it[Keys.DEMO_SPEED] = speed }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }
    suspend fun setOnboardingComplete(complete: Boolean) = dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
}
