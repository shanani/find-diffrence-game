package com.oddduel.game.analytics

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Local-only difficulty telemetry. Every solo mode reports its OWN session the
 * moment it ends - elapsed time, rounds attempted, and the streak/score reached -
 * and this maintains a running average "time per round" per mode, which is a decent
 * proxy for how hard a serious player is actually finding that mode's current
 * tuning: a rising average means the generator's pacing is outrunning real players,
 * a falling one means there's room to push the curve harder.
 *
 * This is deliberately NOT read back by the running app anywhere - nothing here
 * changes live gameplay. It exists purely so a human (or a future Claude session)
 * can read the accumulated numbers later and use them to retune the hardcoded
 * difficulty constants in TimeAttackViewModel/ReflexHunterViewModel/etc. by hand, in
 * a future update. The file never leaves the device on its own: see
 * SettingsScreen's "Share Gameplay Stats" for the one, explicit, player-initiated
 * way it can be sent anywhere. Nothing here ever embeds a network call or a
 * credential - an Android app binary is public the moment it's installed, so baking
 * in anything with write access (e.g. a GitHub token) would hand that access to
 * every single install.
 */
object DifficultyAnalytics {

    private const val FILE_NAME = "difficulty_stats.json"
    private const val SCHEMA_VERSION = 1
    private val mutex = Mutex()

    // Below this streak/score, a session is too short to say anything about real
    // difficulty - filters out instant exits, misclicks, and one-off testing, so the
    // running average only reflects players who were genuinely engaged.
    private val MIN_SERIOUS_RESULT = mapOf(
        "time_attack" to 10,
        "reflex_hunter" to 5,
        "survival_streak" to 5,
        "memory_flash" to 3
    )

    /**
     * Call once, right when a solo mode's run ends. [roundsPlayed] is how many cards/
     * hops/flashes were actually attempted (including the one that ended it);
     * [streakOrScore] is the mode's own "how well did they do" number, used only to
     * decide whether this session counts as serious, never stored itself.
     */
    suspend fun reportSession(context: Context, mode: String, elapsedMs: Long, roundsPlayed: Int, streakOrScore: Int) {
        val threshold = MIN_SERIOUS_RESULT[mode] ?: return
        if (streakOrScore < threshold || roundsPlayed <= 0 || elapsedMs <= 0) return
        val avgMsPerRound = elapsedMs.toDouble() / roundsPlayed

        withContext(Dispatchers.IO) {
            mutex.withLock {
                val file = statsFile(context)
                val root = readOrCreate(file)
                val modes = root.getJSONObject("modes")
                val entry = if (modes.has(mode)) {
                    modes.getJSONObject(mode)
                } else {
                    JSONObject().put("sessionCount", 0).put("avgMsPerRound", 0.0).put("bestResult", 0)
                }

                val oldCount = entry.getInt("sessionCount")
                val oldMean = entry.getDouble("avgMsPerRound")
                val newCount = oldCount + 1
                // Incremental mean update - avoids keeping every raw session around.
                val newMean = oldMean + (avgMsPerRound - oldMean) / newCount

                entry.put("sessionCount", newCount)
                entry.put("avgMsPerRound", newMean)
                entry.put("bestResult", maxOf(entry.getInt("bestResult"), streakOrScore))
                entry.put("lastUpdatedEpochMs", System.currentTimeMillis())

                modes.put(mode, entry)
                root.put("modes", modes)
                file.parentFile?.mkdirs()
                file.writeText(root.toString(2))
            }
        }
    }

    /** For the Settings "Share Gameplay Stats" export - returns the raw JSON as text. */
    suspend fun readStatsJson(context: Context): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            val file = statsFile(context)
            if (file.exists()) file.readText() else readOrCreate(file).toString(2)
        }
    }

    private fun readOrCreate(file: File): JSONObject {
        if (!file.exists()) return emptyRoot()
        return try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            emptyRoot()
        }
    }

    private fun emptyRoot(): JSONObject =
        JSONObject().put("schemaVersion", SCHEMA_VERSION).put("modes", JSONObject())

    private fun statsFile(context: Context): File =
        File(context.applicationContext.getExternalFilesDir("analytics"), FILE_NAME)
}
