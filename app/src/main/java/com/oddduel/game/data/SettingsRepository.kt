package com.oddduel.game.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "oddduel_settings")

data class GameSettings(
    val roundTimeoutSeconds: Int = 45,
    val gridCols: Int = 4,
    val gridRows: Int = 4,
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val musicVolume: Float = 0.55f,
    val sfxVolume: Float = 0.85f
) {
    val cellCount: Int get() = gridCols * gridRows
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val TIMEOUT = intPreferencesKey("round_timeout_seconds")
        val COLS = intPreferencesKey("grid_cols")
        val ROWS = intPreferencesKey("grid_rows")
        val MUSIC_ON = booleanPreferencesKey("music_enabled")
        val SFX_ON = booleanPreferencesKey("sfx_enabled")
        val MUSIC_VOL = floatPreferencesKey("music_volume")
        val SFX_VOL = floatPreferencesKey("sfx_volume")
    }

    val settings: Flow<GameSettings> = context.dataStore.data.map { prefs ->
        GameSettings(
            roundTimeoutSeconds = prefs[Keys.TIMEOUT] ?: 45,
            gridCols = prefs[Keys.COLS] ?: 4,
            gridRows = prefs[Keys.ROWS] ?: 4,
            musicEnabled = prefs[Keys.MUSIC_ON] ?: true,
            sfxEnabled = prefs[Keys.SFX_ON] ?: true,
            musicVolume = prefs[Keys.MUSIC_VOL] ?: 0.55f,
            sfxVolume = prefs[Keys.SFX_VOL] ?: 0.85f
        )
    }

    suspend fun setTimeout(seconds: Int) = context.dataStore.edit { it[Keys.TIMEOUT] = seconds }
    suspend fun setGridSize(cols: Int, rows: Int) = context.dataStore.edit {
        it[Keys.COLS] = cols
        it[Keys.ROWS] = rows
    }
    suspend fun setMusicEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.MUSIC_ON] = enabled }
    suspend fun setSfxEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.SFX_ON] = enabled }
    suspend fun setMusicVolume(volume: Float) = context.dataStore.edit { it[Keys.MUSIC_VOL] = volume }
    suspend fun setSfxVolume(volume: Float) = context.dataStore.edit { it[Keys.SFX_VOL] = volume }
}
