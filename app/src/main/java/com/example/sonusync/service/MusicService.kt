package com.example.sonusync.service

import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sonusync.notification.MusicNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var notificationManager: MusicNotificationManager

    @UnstableApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.startNotificationService(
                mediaSession = mediaSession,
                mediaSessionService = this
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession.player
        if (player.playWhenReady) {
            player.pause()
        }
        stopSelf()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        Log.d("DebugLogs", "Service Destroyed")
        super.onDestroy()
        mediaSession.apply {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
    }
}