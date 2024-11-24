package com.example.sonusync.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.sonusync.data.enums.RepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class MusicServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
) : Player.Listener {
    private val _audioState: MutableStateFlow<AudioState> = MutableStateFlow(AudioState.Initial)
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private var isShuffleEnabled = false
    private var repeatMode: RepeatMode = RepeatMode.ALL

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    sealed class PlayerEvent {
        object PlayPause : PlayerEvent()
        object SelectedAudioChange : PlayerEvent()
        object Forward : PlayerEvent()
        object Backward : PlayerEvent()
        object SeekToNext : PlayerEvent()
        object SeekToPrevious : PlayerEvent()
        object SeekTo : PlayerEvent()
        object Stop : PlayerEvent()
        object Shuffle : PlayerEvent()
        object Repeat : PlayerEvent()
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

    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0,
    ) {
        when (playerEvent) {
            PlayerEvent.SeekToNext -> {
                if (isShuffleEnabled) {
                    seekToRandom()
                } else {
                    exoPlayer.seekToNext()
                }
            }

            PlayerEvent.SeekToPrevious -> {
                if (isShuffleEnabled) {
                    seekToRandom()
                } else {
                    exoPlayer.seekToPrevious()
                }
            }

            PlayerEvent.PlayPause -> playOrPause()
            PlayerEvent.Backward -> exoPlayer.seekBack()
            PlayerEvent.Forward -> exoPlayer.seekForward()
            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
            PlayerEvent.SelectedAudioChange -> {
                when (selectedAudioIndex) {
                    exoPlayer.currentMediaItemIndex -> {
                        playOrPause()
                    }

                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                        _audioState.value = AudioState.Playing(
                            isPlaying = true
                        )
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }

            PlayerEvent.Stop -> stopProgressUpdate()
            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong()
                )
            }

            PlayerEvent.Shuffle -> toggleShuffle()
            PlayerEvent.Repeat -> toggleRepeatMode()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> _audioState.value = AudioState.Buffering(exoPlayer.currentPosition)
            ExoPlayer.STATE_READY -> _audioState.value = AudioState.Ready(exoPlayer.duration)
            Player.STATE_ENDED -> handleEndOfPlayback()
            Player.STATE_IDLE -> return
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _audioState.value = AudioState.Playing(isPlaying = isPlaying)
        _audioState.value = AudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        if (isPlaying) {
            scope.launch { startProgressUpdate() }
        } else {
            stopProgressUpdate()
        }
    }

    private fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }

    private fun toggleRepeatMode() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.OFF
        }
    }

    private suspend fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            _audioState.value = AudioState.Playing(isPlaying = true)
            startProgressUpdate()
        }
    }

    private fun seekToRandom() {
        val mediaItemsCount = exoPlayer.mediaItemCount
        if (mediaItemsCount > 0) {
            val randomIndex = (0 until mediaItemsCount).random()
            exoPlayer.seekToDefaultPosition(randomIndex)
            _audioState.value = AudioState.CurrentPlaying(randomIndex)
            exoPlayer.playWhenReady = true
            startProgressUpdate()
        }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        job = scope.launch {
            while (isActive) {
                delay(500)
                _audioState.value = AudioState.Progress(exoPlayer.currentPosition)
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _audioState.value = AudioState.Playing(isPlaying = false)
    }

    private fun handleEndOfPlayback() {
        when (repeatMode) {
            RepeatMode.OFF -> {
                exoPlayer.seekToDefaultPosition()
                _audioState.value = AudioState.Playing(isPlaying = false)
                exoPlayer.pause()
            }
            RepeatMode.ALL -> {
                if (isShuffleEnabled) {
                    seekToRandom()
                } else {
                    exoPlayer.seekToNext()
                }
            }
            RepeatMode.ONE -> {
                exoPlayer.seekTo(0)
                exoPlayer.playWhenReady = true
            }
        }
    }
}