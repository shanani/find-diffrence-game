package com.oddduel.game.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oddduel.game.data.GameSettings
import com.oddduel.game.data.SettingsRepository
import com.oddduel.game.data.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsRepo = SettingsRepository(app)
    private val userRepo = UserRepository(app)

    val settings: StateFlow<GameSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameSettings())

    fun setTimeout(seconds: Int) = viewModelScope.launch { settingsRepo.setTimeout(seconds) }
    fun setGridSize(cols: Int, rows: Int) = viewModelScope.launch { settingsRepo.setGridSize(cols, rows) }
    fun setMusicEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepo.setMusicEnabled(enabled) }
    fun setSfxEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepo.setSfxEnabled(enabled) }
    fun setMusicVolume(volume: Float) = viewModelScope.launch { settingsRepo.setMusicVolume(volume) }
    fun setSfxVolume(volume: Float) = viewModelScope.launch { settingsRepo.setSfxVolume(volume) }
    fun resetAllScores() = viewModelScope.launch { userRepo.resetAllScores() }
}
