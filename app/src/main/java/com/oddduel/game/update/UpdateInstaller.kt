package com.oddduel.game.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
 */
object UpdateInstaller {

    suspend fun downloadAndInstall(context: Context, info: UpdateInfo) {
        val appContext = context.applicationContext
        toast(appContext, "Downloading update …")

        val destDir = appContext.getExternalFilesDir("updates")
        if (destDir == null) {
            toast(appContext, "Update failed: no storage available for the download")
            return
        }
        val destFile = File(destDir, "update.apk")

        val error = withContext(Dispatchers.IO) {
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

        if (error != null) {
            toast(appContext, "Update download failed: $error")
            return
        }

        toast(appContext, "Download complete - opening installer")
        promptInstall(appContext, destFile)
    }

    private suspend fun toast(context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun promptInstall(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            // One-time system setting the user must grant before any install-from-APK
            // can proceed; send them there, then they can tap the downloaded APK again.
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settingsIntent)
            return
        }

        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
    }
}
