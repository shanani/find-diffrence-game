package com.oddduel.game.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

/**
 * Checks GitHub releases for this repo and compares the newest available version
 * against the running app. The repo must be public for the unauthenticated API call
 * to work. Release tags are expected in the "build-<versionCode>" form the CI workflow
 * uses, e.g. "build-14" means versionCode 14 - that number is what gets compared, not
 * the tag string itself.
 *
 * Deliberately does NOT call the /releases/latest endpoint: that endpoint just returns
 * whichever release GitHub currently has flagged as "latest" by publish time, which is
 * usually but not guaranteedly the highest version code. Instead this fetches a page of
 * the most recent releases and picks whichever one actually has the highest versionCode,
 * so an update always targets the true newest build, never a "next" one short of it.
 */
object UpdateChecker {
    private const val RELEASES_URL = "https://api.github.com/repos/shanani/find-diffrence-game/releases?per_page=20"

    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(RELEASES_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "OddOneOutDuel-App")
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return@withContext null
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val releases = org.json.JSONArray(body)
            var best: JSONObject? = null
            var bestVersionCode = -1
            for (i in 0 until releases.length()) {
                val release = releases.getJSONObject(i)
                if (release.optBoolean("draft", false)) continue
                val tag = release.optString("tag_name")
                val versionCode = tag.substringAfterLast('-').toIntOrNull() ?: continue
                if (versionCode > bestVersionCode) {
                    bestVersionCode = versionCode
                    best = release
                }
            }

            val json = best ?: return@withContext null
            if (bestVersionCode <= currentVersionCode) return@withContext null

            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name")
                if (name.endsWith(".apk")) {
                    apkUrl = asset.optString("browser_download_url")
                    break
                }
            }
            val downloadUrl = apkUrl ?: return@withContext null

            UpdateInfo(
                versionCode = bestVersionCode,
                versionName = json.optString("name", json.optString("tag_name")),
                downloadUrl = downloadUrl,
                releaseNotes = json.optString("body", "")
            )
        } catch (e: Exception) {
            null
        }
    }
}
