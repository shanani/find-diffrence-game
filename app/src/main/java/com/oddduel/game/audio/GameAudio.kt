package com.oddduel.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
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

    fun applySettings(musicEnabled: Boolean, sfxEnabled: Boolean, musicVolume: Float, sfxVolume: Float) {
        this.musicEnabled = musicEnabled
        this.sfxEnabled = sfxEnabled
        this.musicVolume = musicVolume
        this.sfxVolume = sfxVolume
        music?.setVolume(musicVolume, musicVolume)
        if (!musicEnabled) stopMusic() else if (music == null || music?.isPlaying == false) startMusic()
    }

    fun play(sfx: Sfx) {
        if (!sfxEnabled) return
        val id = soundIds[sfx] ?: return
        soundPool.play(id, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun startMusic() {
        if (!musicEnabled) return
        if (music != null) {
            music?.let { if (!it.isPlaying) it.start() }
            return
        }
        music = MediaPlayer.create(context, R.raw.music_loop)?.apply {
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
    }

    fun stopMusic() {
        music?.pause()
    }

    fun release() {
        soundPool.release()
        music?.release()
        music = null
    }
}
