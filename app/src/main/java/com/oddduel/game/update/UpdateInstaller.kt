package com.oddduel.game.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * Downloads the release APK via the system DownloadManager (so it survives the app
 * being backgrounded, shows normal download progress/notifications, and needs no
 * extra storage permission on API 26+ since it's written into this app's own
 * external files dir) and then launches the system package installer for it.
 */
object UpdateInstaller {

    fun downloadAndInstall(context: Context, info: UpdateInfo) {
        val destDir = context.getExternalFilesDir("updates")
        val destFile = File(destDir, "update.apk")
        if (destFile.exists()) destFile.delete()

        val request = DownloadManager.Request(Uri.parse(info.downloadUrl))
            .setTitle("Odd One Out Duel update")
            .setDescription("Downloading ${info.versionName}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, "updates", "update.apk")
            .setMimeType("application/vnd.android.package-archive")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val appContext = context.applicationContext
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (completedId == downloadId) {
                    appContext.unregisterReceiver(this)
                    promptInstall(appContext, destFile)
                }
            }
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
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
