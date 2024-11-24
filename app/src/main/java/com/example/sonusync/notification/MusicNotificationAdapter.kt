package com.example.sonusync.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import coil.imageLoader
import coil.request.ImageRequest
import com.example.sonusync.R

@androidx.media3.common.util.UnstableApi
class MusicNotificationAdapter(
    private val context: Context,
    private val pendingIntent: PendingIntent?,
) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence =
        player.mediaMetadata.albumTitle ?: "Unknown"

    override fun createCurrentContentIntent(player: Player): PendingIntent? = pendingIntent

    override fun getCurrentContentText(player: Player): CharSequence =
        player.mediaMetadata.displayTitle ?: "Unknown"

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback,
    ): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(player.mediaMetadata.artworkUri)
            .target(
                onSuccess = { result ->
                    callback.onBitmap((result as BitmapDrawable).bitmap)
                },
                onError = { _ ->
                    val placeholderBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.default_album_cover)
                    callback.onBitmap(placeholderBitmap)
                }
            )
            .build()

        context.imageLoader.enqueue(request)

        return null
    }
}