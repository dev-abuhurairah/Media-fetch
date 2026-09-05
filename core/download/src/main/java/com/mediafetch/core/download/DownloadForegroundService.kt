package com.mediafetch.core.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mediafetch.core.common.Formatters
import com.mediafetch.core.model.DownloadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadForegroundService : Service() {

    @Inject
    lateinit var downloadManager: DownloadManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val CHANNEL_ID = "mediafetch_downloads_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_PAUSE = "com.mediafetch.ACTION_PAUSE"
        const val ACTION_CANCEL = "com.mediafetch.ACTION_CANCEL"
        const val EXTRA_DOWNLOAD_ID = "extra_download_id"

        fun start(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildInitialNotification())
        observeActiveDownloads()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val downloadId = intent?.getStringExtra(EXTRA_DOWNLOAD_ID)

        if (downloadId != null) {
            when (action) {
                ACTION_PAUSE -> serviceScope.launch { downloadManager.pauseDownload(downloadId) }
                ACTION_CANCEL -> serviceScope.launch { downloadManager.cancelDownload(downloadId) }
            }
        }

        return START_STICKY
    }

    private fun observeActiveDownloads() {
        serviceScope.launch {
            downloadManager.activeDownloads.collectLatest { downloads ->
                if (downloads.isEmpty()) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    val current = downloads.first()
                    updateNotification(
                        title = current.mediaInfo.title,
                        progressPercent = (current.progress * 100).toInt(),
                        speed = Formatters.formatSpeed(current.speedBytesPerSec),
                        eta = Formatters.formatRemainingTime(current.remainingSeconds),
                        downloadId = current.id
                    )
                }
            }
        }
    }

    private fun updateNotification(
        title: String,
        progressPercent: Int,
        speed: String,
        eta: String,
        downloadId: String
    ) {
        val pauseIntent = Intent(this, DownloadForegroundService::class.java).apply {
            action = ACTION_PAUSE
            putExtra(EXTRA_DOWNLOAD_ID, downloadId)
        }
        val pausePendingIntent = PendingIntent.getService(
            this,
            101,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, DownloadForegroundService::class.java).apply {
            action = ACTION_CANCEL
            putExtra(EXTRA_DOWNLOAD_ID, downloadId)
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            102,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$progressPercent% • $speed • $eta")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progressPercent, progressPercent <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildInitialNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("MediaFetch Download Manager")
        .setContentText("Preparing downloads...")
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setOngoing(true)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MediaFetch Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live progress for media downloads"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
