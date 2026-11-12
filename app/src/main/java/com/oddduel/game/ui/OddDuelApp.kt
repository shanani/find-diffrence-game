package com.oddduel.game.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.audio.Sfx
import com.oddduel.game.data.GameSettings
import com.oddduel.game.data.SettingsRepository
import com.oddduel.game.ui.screens.accounts.AccountsScreen
import com.oddduel.game.ui.screens.game.QuickPlayScreen
import com.oddduel.game.ui.screens.home.HomeScreen
import com.oddduel.game.ui.screens.settings.SettingsScreen
import com.oddduel.game.ui.screens.tournament.TournamentScreen

private object Routes {
    const val HOME = "home"
    const val QUICK_PLAY = "quick_play"
    const val TOURNAMENT = "tournament"
    const val ACCOUNTS = "accounts"
    const val SETTINGS = "settings"
}

@Composable
fun OddDuelApp() {
    val context = LocalContext.current
    val audio = remember { GameAudio(context) }
    DisposableEffect(Unit) { onDispose { audio.release() } }

    val settingsRepo = remember { SettingsRepository(context) }
    val settings by settingsRepo.settings.collectAsState(initial = GameSettings())

    LaunchedEffect(settings.musicEnabled, settings.sfxEnabled, settings.musicVolume, settings.sfxVolume) {
        audio.applySettings(settings.musicEnabled, settings.sfxEnabled, settings.musicVolume, settings.sfxVolume)
    }

    val navController = rememberNavController()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        NavHost(navController = navController, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(
                    onQuickPlay = { audio.play(Sfx.CLICK); navController.navigate(Routes.QUICK_PLAY) },
                    onTournament = { audio.play(Sfx.CLICK); navController.navigate(Routes.TOURNAMENT) },
                    onAccounts = { audio.play(Sfx.CLICK); navController.navigate(Routes.ACCOUNTS) },
                    onSettings = { audio.play(Sfx.CLICK); navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.QUICK_PLAY) {
                QuickPlayScreen(settings = settings, audio = audio, onExit = { navController.popBackStack() })
            }
            composable(Routes.TOURNAMENT) {
                TournamentScreen(settings = settings, audio = audio, onExit = { navController.popBackStack() })
            }
            composable(Routes.ACCOUNTS) {
                AccountsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
