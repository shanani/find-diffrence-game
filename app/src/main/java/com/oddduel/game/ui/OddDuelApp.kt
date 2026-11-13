package com.oddduel.game.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oddduel.game.BuildConfig
import com.oddduel.game.audio.GameAudio
import com.oddduel.game.audio.Sfx
import com.oddduel.game.data.GameSettings
import com.oddduel.game.data.SettingsRepository
import com.oddduel.game.engine.GamePauseController
import com.oddduel.game.ui.components.PauseButton
import com.oddduel.game.ui.components.PauseOverlay
import com.oddduel.game.ui.components.UpdateProgressIndicator
import com.oddduel.game.ui.screens.about.AboutScreen
import com.oddduel.game.ui.screens.accounts.AccountsScreen
import com.oddduel.game.ui.screens.game.QuickPlayScreen
import com.oddduel.game.ui.screens.home.HomeScreen
import com.oddduel.game.ui.screens.settings.SettingsScreen
import com.oddduel.game.ui.screens.solo.MemoryFlashScreen
import com.oddduel.game.ui.screens.solo.ReflexHunterScreen
import com.oddduel.game.ui.screens.solo.SoloModeSelectScreen
import com.oddduel.game.ui.screens.solo.SurvivalStreakScreen
import com.oddduel.game.ui.screens.solo.TimeAttackScreen
import com.oddduel.game.ui.screens.tournament.TournamentScreen
import com.oddduel.game.update.UpdateAvailableDialog
import com.oddduel.game.update.UpdateChecker
import com.oddduel.game.update.UpdateInfo
import com.oddduel.game.update.UpdateInstaller
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

private object Routes {
    const val HOME = "home"
    const val QUICK_PLAY = "quick_play"
    const val TOURNAMENT = "tournament"
    const val ACCOUNTS = "accounts"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val SOLO_SELECT = "solo_select"
    const val SOLO_TIME_ATTACK = "solo_time_attack"
    const val SOLO_REFLEX_HUNTER = "solo_reflex_hunter"
    const val SOLO_SURVIVAL_STREAK = "solo_survival_streak"
    const val SOLO_MEMORY_FLASH = "solo_memory_flash"
}

private const val TRANSITION_MS = 260

// One consistent slide+fade used by every destination in the graph so switching between
// any two screens (home <-> solo select <-> a solo game <-> settings, etc.) feels the same.
private fun NavGraphBuilder.navDestination(
    route: String,
    content: @Composable AnimatedContentScope.(androidx.navigation.NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_MS)
            ) + fadeIn(tween(TRANSITION_MS))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(TRANSITION_MS)
            ) + fadeOut(tween(TRANSITION_MS))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_MS)
            ) + fadeIn(tween(TRANSITION_MS))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(TRANSITION_MS)
            ) + fadeOut(tween(TRANSITION_MS))
        },
        content = content
    )
}

// Every call site plays a click and uses launchSingleTop so a double-tapped button
// (or a fast finger on a TV remote) can never push the same destination twice.
private fun navigate(navController: NavHostController, audio: GameAudio, route: String) {
    audio.play(Sfx.CLICK)
    navController.navigate(route) { launchSingleTop = true }
}

@Composable
fun OddDuelApp() {
    val context = LocalContext.current
    val audio = remember { GameAudio(context) }
    DisposableEffect(Unit) { onDispose { audio.release() } }

    // One pause switch for the WHOLE game - every timer-driven ViewModel (duel round
    // countdown, the solo-mode clocks) checks this, not just the music - shared across
    // the manual pause button and the app being backgrounded.
    val pauseController = remember { GamePauseController() }

    val scope = rememberCoroutineScope()

    // Backgrounding the app (switching to another app, locking the screen, etc.) must
    // freeze gameplay AND cut the music immediately. Coming back to the foreground
    // deliberately does NOT auto-resume gameplay: the Pause overlay stays up until the
    // player taps Resume, so a round can never silently keep running - or silently
    // restart the soundtrack - behind their back while they were away.
    //
    // Separately, ON_START also resumes a pending APK update: granting "install unknown
    // apps" always sends the player out to Settings and back, so without this the
    // download would just silently never continue and they'd have to notice and tap
    // Update Now a second time - exactly the "did that button even do anything?" feeling
    // this is meant to eliminate.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, audio, pauseController, scope) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    pauseController.pause()
                    audio.pauseAudio()
                }
                Lifecycle.Event.ON_START -> {
                    scope.launch { UpdateInstaller.resumeIfPermissionNowGranted(context) }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val settingsRepo = remember { SettingsRepository(context) }
    val settings by settingsRepo.settings.collectAsState(initial = GameSettings())

    LaunchedEffect(settings.musicEnabled, settings.sfxEnabled, settings.musicVolume, settings.sfxVolume) {
        audio.applySettings(settings.musicEnabled, settings.sfxEnabled, settings.musicVolume, settings.sfxVolume)
    }

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    LaunchedEffect(Unit) {
        updateInfo = UpdateChecker.checkForUpdate(BuildConfig.VERSION_CODE)
    }

    val navController = rememberNavController()
    Box(Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                navDestination(Routes.HOME) {
                    HomeScreen(
                        onQuickPlay = { navigate(navController, audio, Routes.QUICK_PLAY) },
                        onTournament = { navigate(navController, audio, Routes.TOURNAMENT) },
                        onSolo = { navigate(navController, audio, Routes.SOLO_SELECT) },
                        onAccounts = { navigate(navController, audio, Routes.ACCOUNTS) },
                        onSettings = { navigate(navController, audio, Routes.SETTINGS) },
                        onExitApp = {
                            (context as? Activity)?.finishAffinity()
                            exitProcess(0)
                        }
                    )
                }
                navDestination(Routes.QUICK_PLAY) {
                    QuickPlayScreen(
                        settings = settings,
                        audio = audio,
                        pauseController = pauseController,
                        onExit = { navController.popBackStack() }
                    )
                }
                navDestination(Routes.TOURNAMENT) {
                    TournamentScreen(
                        settings = settings,
                        audio = audio,
                        pauseController = pauseController,
                        onExit = { navController.popBackStack() }
                    )
                }
                navDestination(Routes.ACCOUNTS) {
                    AccountsScreen(onBack = { navController.popBackStack() })
                }
                navDestination(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onAbout = { navigate(navController, audio, Routes.ABOUT) }
                    )
                }
                navDestination(Routes.ABOUT) {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
                navDestination(Routes.SOLO_SELECT) {
                    SoloModeSelectScreen(
                        onBack = { navController.popBackStack() },
                        onTimeAttack = { navigate(navController, audio, Routes.SOLO_TIME_ATTACK) },
                        onReflexHunter = { navigate(navController, audio, Routes.SOLO_REFLEX_HUNTER) },
                        onSurvivalStreak = { navigate(navController, audio, Routes.SOLO_SURVIVAL_STREAK) },
                        onMemoryFlash = { navigate(navController, audio, Routes.SOLO_MEMORY_FLASH) }
                    )
                }
                navDestination(Routes.SOLO_TIME_ATTACK) {
                    TimeAttackScreen(audio = audio, pauseController = pauseController, onBack = { navController.popBackStack() })
                }
                navDestination(Routes.SOLO_REFLEX_HUNTER) {
                    ReflexHunterScreen(audio = audio, pauseController = pauseController, onBack = { navController.popBackStack() })
                }
                navDestination(Routes.SOLO_SURVIVAL_STREAK) {
                    SurvivalStreakScreen(audio = audio, onBack = { navController.popBackStack() })
                }
                navDestination(Routes.SOLO_MEMORY_FLASH) {
                    MemoryFlashScreen(audio = audio, pauseController = pauseController, onBack = { navController.popBackStack() })
                }
            }
        }

        // Fixed in the same spot above every screen in the graph, so the player never has
        // to hunt for it and it never collides with a given screen's own top bar content.
        PauseButton(
            onClick = { pauseController.pause(); audio.pauseAudio() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        )

        // Visible on every screen the instant a download is actually running - including
        // one resumed automatically on ON_START after a permission round trip to Settings,
        // so "I hit Update Now" never again looks like it did nothing.
        if (UpdateInstaller.isDownloading) {
            UpdateProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }

        if (pauseController.isPaused) {
            PauseOverlay(
                audio = audio,
                onResume = { pauseController.resume(); audio.resumeAudio() },
                onQuitToHome = {
                    pauseController.resume()
                    audio.resumeAudio()
                    navController.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }
    }

    updateInfo?.let { info ->
        UpdateAvailableDialog(
            info = info,
            onUpdate = {
                scope.launch { UpdateInstaller.downloadAndInstall(context, info) }
                updateInfo = null
            },
            onDismiss = { updateInfo = null }
        )
    }
}
