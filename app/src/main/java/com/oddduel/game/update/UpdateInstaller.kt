package com.oddduel.game.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads the release APK directly (plain HTTP, not DownloadManager - GitHub's
 * release asset URLs redirect to a separate CDN host, and DownloadManager has a
 * history of failing on that redirect on some devices with zero visible error) and
 * then launches the system package installer for it. Every failure path shows a
 * Toast so "I tapped Update Now and nothing happened" can't happen silently again.
 *
 * Granting the "install unknown apps" permission always sends the player out to
 * Settings and back - there is no way to ask for it in-line. [pendingInfo] remembers
 * that an update was requested but blocked only on that permission, so
 * [resumeIfPermissionNowGranted] (called from an app-level lifecycle observer on
 * ON_START) can continue the download the moment they return with it granted,
 * instead of leaving them to notice nothing happened and tap Update Now again.
 */
object UpdateInstaller {

    /** True while a download is actually in flight; drives a small progress indicator. */
    var isDownloading by mutableStateOf(false)
        private set

    private var pendingInfo: UpdateInfo? = null

    /** Call from an ON_START lifecycle observer to pick back up after a Settings round trip. */
    suspend fun resumeIfPermissionNowGranted(context: Context) {
        val info = pendingInfo ?: return
        val appContext = context.applicationContext
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || appContext.packageManager.canRequestPackageInstalls()) {
            pendingInfo = null
            downloadAndInstall(context, info)
        }
    }

    suspend fun downloadAndInstall(context: Context, info: UpdateInfo) {
        val appContext = context.applicationContext

        // Check this BEFORE downloading ~10+ MB: no point fetching the APK if we
        // can't even launch the installer for it yet, and this failing silently is
        // exactly the "nothing happens" symptom we're trying to eliminate.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !appContext.packageManager.canRequestPackageInstalls()) {
            pendingInfo = info
            toast(appContext, "Allow \"install unknown apps\" on the next screen - the download will continue on its own when you come back")
            try {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${appContext.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                withContext(Dispatchers.Main) { appContext.startActivity(settingsIntent) }
            } catch (e: Exception) {
                pendingInfo = null
                toast(appContext, "Couldn't open the permission screen: ${e.message ?: e.javaClass.simpleName}")
            }
            return
        }

        pendingInfo = null
        isDownloading = true
        try {
            val destDir = appContext.getExternalFilesDir("updates")
            if (destDir == null) {
                toast(appContext, "Update failed: no storage available for the download")
                return
            }
            val destFile = File(destDir, "update.apk")

            val downloadError = withContext(Dispatchers.IO) {
                try {
                    if (destFile.exists()) destFile.delete()
                    val connection = (URL(info.downloadUrl).openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = true
                        connectTimeout = 15000
                        readTimeout = 20000
                        setRequestProperty("User-Agent", "OddOneOutDuel-App")
                    }
                    val code = connection.responseCode
                    if (code !in 200..299) {
                        connection.disconnect()
                        return@withContext "server returned HTTP $code"
                    }
                    connection.inputStream.use { input ->
                        destFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    connection.disconnect()
                    if (!destFile.exists() || destFile.length() == 0L) "downloaded file was empty" else null
                } catch (e: Exception) {
                    e.message ?: e.javaClass.simpleName
                }
            }

            if (downloadError != null) {
                toast(appContext, "Update download failed: $downloadError")
                return
            }

            val installError = withContext(Dispatchers.Main) { launchInstaller(appContext, destFile) }
            if (installError != null) {
                toast(appContext, "Couldn't open the install screen: $installError")
            }
        } finally {
            isDownloading = false
        }
    }

    private suspend fun toast(context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    /** Returns null on success, or an error description if anything here threw. */
    private fun launchInstaller(context: Context, apkFile: File): String? {
        if (!apkFile.exists()) return "downloaded file is missing"
        return try {
            val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
            null
        } catch (e: Exception) {
            e.message ?: e.javaClass.simpleName
        }
    }
}
