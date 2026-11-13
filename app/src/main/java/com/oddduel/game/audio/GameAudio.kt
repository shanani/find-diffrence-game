package com.oddduel.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.oddduel.game.R

enum class Sfx(val resId: Int) {
    CLICK(R.raw.sfx_click),
    TICK(R.raw.sfx_tick),
    CORRECT(R.raw.sfx_correct),
    WRONG(R.raw.sfx_wrong),
    COIN(R.raw.sfx_coin),
    ROUND_START(R.raw.sfx_round_start),
    VICTORY(R.raw.sfx_victory),
    TIMEOUT(R.raw.sfx_timeout)
}

/**
 * Thin wrapper around [SoundPool] (short one-shot effects) and [MediaPlayer] (looping
 * background music) so the rest of the app only ever deals with simple play/stop calls
 * and never touches the Android media APIs directly.
 */
class GameAudio(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds: Map<Sfx, Int> = Sfx.entries.associateWith { soundPool.load(context, it.resId, 1) }

    private var music: MediaPlayer? = null
    private var musicEnabled = true
    private var sfxEnabled = true
    private var musicVolume = 0.55f
    private var sfxVolume = 0.85f

    // A transient, this-session-only mute from the Pause overlay's "Mute Music" button -
    // deliberately separate from the persisted Settings toggle (`musicEnabled`) above, so
    // a quick mute from the pause menu doesn't need to write through DataStore and doesn't
    // flip the player's saved preference for next time.
    private var musicMutedByUser = false

    /** True while music is actually audible right now; drives the pause/play icon. */
    var musicPlaying by mutableStateOf(false)
        private set

    fun applySettings(musicEnabled: Boolean, sfxEnabled: Boolean, musicVolume: Float, sfxVolume: Float) {
        this.musicEnabled = musicEnabled
        this.sfxEnabled = sfxEnabled
        this.musicVolume = musicVolume
        this.sfxVolume = sfxVolume
        music?.setVolume(musicVolume, musicVolume)
        if (!musicEnabled) stopMusic() else startMusic()
    }

    fun play(sfx: Sfx) {
        if (!sfxEnabled) return
        val id = soundIds[sfx] ?: return
        soundPool.play(id, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun toggleMusicMute() {
        musicMutedByUser = !musicMutedByUser
        if (musicMutedByUser) stopMusic() else startMusic()
    }

    fun startMusic() {
        if (!musicEnabled || musicMutedByUser) return
        if (music != null) {
            music?.let { if (!it.isPlaying) it.start() }
            musicPlaying = true
            return
        }
        music = MediaPlayer.create(context, R.raw.mozart_nachtmusik)?.apply {
            isLooping = true
            setVolume(musicVolume, musicVolume)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            start()
        }
        musicPlaying = music != null
    }

    fun stopMusic() {
        music?.pause()
        musicPlaying = false
    }

    /**
     * Pauses everything audible - music AND any in-flight short sound effect - for the
     * app-wide pause (manual pause button, or the app being backgrounded). Resuming is
     * always an explicit [resumeAudio] call from the Pause overlay's Resume button, never
     * automatic, so returning to the foreground never silently restarts the soundtrack.
     */
    fun pauseAudio() {
        music?.pause()
        musicPlaying = false
        soundPool.autoPause()
    }

    fun resumeAudio() {
        soundPool.autoResume()
        startMusic()
    }

    fun release() {
        soundPool.release()
        music?.release()
        music = null
    }
}
