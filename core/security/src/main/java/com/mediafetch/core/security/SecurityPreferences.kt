package com.mediafetch.core.security

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "mediafetch_security_prefs")

@Singleton
class SecurityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_CLIPBOARD_DETECTION = booleanPreferencesKey("clipboard_detection_enabled")
        private val KEY_ANALYTICS_OPT_OUT = booleanPreferencesKey("analytics_opt_out")
        private val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only_downloads")
        private val KEY_CONCURRENT_DOWNLOAD_LIMIT = intPreferencesKey("max_concurrent_downloads")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, LIGHT, DARK
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        private val KEY_WARN_MOBILE_DATA = booleanPreferencesKey("warn_mobile_data")
    }

    val isClipboardDetectionEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CLIPBOARD_DETECTION] ?: true
    }

    val isAnalyticsOptedOut: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ANALYTICS_OPT_OUT] ?: true // Privacy-first default: opted out
    }

    val isWifiOnly: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIFI_ONLY] ?: false
    }

    val maxConcurrentDownloads: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_CONCURRENT_DOWNLOAD_LIMIT] ?: 3
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val isDynamicColorEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DYNAMIC_COLOR] ?: true
    }

    val warnMobileData: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WARN_MOBILE_DATA] ?: true
    }

    suspend fun setClipboardDetection(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CLIPBOARD_DETECTION] = enabled }
    }

    suspend fun setAnalyticsOptOut(optOut: Boolean) {
        context.dataStore.edit { it[KEY_ANALYTICS_OPT_OUT] = optOut }
    }

    suspend fun setWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { it[KEY_WIFI_ONLY] = wifiOnly }
    }

    suspend fun setMaxConcurrentDownloads(limit: Int) {
        context.dataStore.edit { it[KEY_CONCURRENT_DOWNLOAD_LIMIT] = limit.coerceIn(1, 5) }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setWarnMobileData(warn: Boolean) {
        context.dataStore.edit { it[KEY_WARN_MOBILE_DATA] = warn }
    }
}
