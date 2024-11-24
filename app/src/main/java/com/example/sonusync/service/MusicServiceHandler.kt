package com.example.sonusync.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MusicServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
) : Player.Listener {
    private val _audioState: MutableStateFlow<AudioState> = MutableStateFlow(AudioState.Initial)
    private var job: Job? = null
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    sealed class PlayerEvent {
        object PlayPause : PlayerEvent()
        object SelectedAudioChange : PlayerEvent()
        object Backward : PlayerEvent()
        object SeekToNext : PlayerEvent()
        object Forward : PlayerEvent()
        object SeekTo : PlayerEvent()
        object Stop : PlayerEvent()
        data class UpdateProgress(val newProgress: Float) : PlayerEvent()
    }

    sealed class AudioState {
        object Initial : AudioState()
        data class Ready(val duration: Long) : AudioState()
        data class Progress(val progress: Long) : AudioState()
        data class Buffering(val progress: Long) : AudioState()
        data class Playing(val isPlaying: Boolean) : AudioState()
        data class CurrentPlaying(val mediaItemIndex: Int) : AudioState()
    }

    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun setMediaItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }
}