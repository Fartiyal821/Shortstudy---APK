package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ReaderFontFamily {
    SANS_SERIF, SERIF, MONOSPACE
}

data class ReaderPreferences(
    val fontSizeScale: Float = 1.0f,
    val lineHeightScale: Float = 1.2f,
    val fontFamily: ReaderFontFamily = ReaderFontFamily.SANS_SERIF,
    val showCodeLineNumbers: Boolean = true,
    val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val bloggerApiKey: String = ""
)

class PreferencesManager(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_FONT_SIZE_SCALE = floatPreferencesKey("font_size_scale")
        private val KEY_LINE_HEIGHT_SCALE = floatPreferencesKey("line_height_scale")
        private val KEY_FONT_FAMILY = stringPreferencesKey("font_family")
        private val KEY_SHOW_LINE_NUMBERS = booleanPreferencesKey("show_code_line_numbers")
        private val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        private val KEY_BLOGGER_API_KEY = stringPreferencesKey("blogger_api_key")
    }

    val readerPreferencesFlow: Flow<ReaderPreferences> = dataStore.data.map { preferences ->
        val fontSizeScale = preferences[KEY_FONT_SIZE_SCALE] ?: 1.0f
        val lineHeightScale = preferences[KEY_LINE_HEIGHT_SCALE] ?: 1.2f
        val fontFamilyName = preferences[KEY_FONT_FAMILY] ?: ReaderFontFamily.SANS_SERIF.name
        val fontFamily = runCatching { ReaderFontFamily.valueOf(fontFamilyName) }.getOrDefault(ReaderFontFamily.SANS_SERIF)
        val showLineNumbers = preferences[KEY_SHOW_LINE_NUMBERS] ?: true
        val themeName = preferences[KEY_THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = runCatching { AppThemeMode.valueOf(themeName) }.getOrDefault(AppThemeMode.SYSTEM)
        val apiKey = preferences[KEY_BLOGGER_API_KEY] ?: ""

        ReaderPreferences(
            fontSizeScale = fontSizeScale,
            lineHeightScale = lineHeightScale,
            fontFamily = fontFamily,
            showCodeLineNumbers = showLineNumbers,
            appThemeMode = themeMode,
            bloggerApiKey = apiKey
        )
    }

    suspend fun updateFontSizeScale(scale: Float) {
        dataStore.edit { it[KEY_FONT_SIZE_SCALE] = scale.coerceIn(0.8f, 1.6f) }
    }

    suspend fun updateFontFamily(fontFamily: ReaderFontFamily) {
        dataStore.edit { it[KEY_FONT_FAMILY] = fontFamily.name }
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun toggleLineNumbers(enabled: Boolean) {
        dataStore.edit { it[KEY_SHOW_LINE_NUMBERS] = enabled }
    }

    suspend fun updateBloggerApiKey(key: String) {
        dataStore.edit { it[KEY_BLOGGER_API_KEY] = key.trim() }
    }
}
