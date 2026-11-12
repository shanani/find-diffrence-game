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
 * Checks GitHub's "latest release" for this repo and compares its version against
 * the running app. The repo must be public for the unauthenticated API call to work.
 * Release tags are expected in the "build-<versionCode>" form the CI workflow uses,
 * e.g. "build-14" means versionCode 14 - that number is what gets compared, not the
 * tag string itself.
 */
object UpdateChecker {
    private const val API_URL = "https://api.github.com/repos/shanani/find-diffrence-game/releases/latest"

    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
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

            val json = JSONObject(body)
            val tag = json.optString("tag_name")
            val latestVersionCode = tag.substringAfterLast('-').toIntOrNull() ?: return@withContext null
            if (latestVersionCode <= currentVersionCode) return@withContext null

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
                versionCode = latestVersionCode,
                versionName = json.optString("name", tag),
                downloadUrl = downloadUrl,
                releaseNotes = json.optString("body", "")
            )
        } catch (e: Exception) {
            null
        }
    }
}
