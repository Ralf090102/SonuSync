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
    private val _musicState: MutableStateFlow<MusicState> = MutableStateFlow(MusicState.Initial)
    val musicState: StateFlow<MusicState> = _musicState.asStateFlow()

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

    sealed class MusicState {
        object Initial : MusicState()
        data class Ready(val duration: Long) : MusicState()
        data class Progress(val progress: Long) : MusicState()
        data class Buffering(val progress: Long) : MusicState()
        data class Playing(val isPlaying: Boolean) : MusicState()
        data class CurrentPlaying(val mediaItemIndex: Int) : MusicState()
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

    fun getPlayback(): Boolean {
        return exoPlayer.isPlaying
    }

    fun getCurrentMediaDuration(): Long {
        return exoPlayer.duration
    }

    fun getCurrentPlaybackPosition(): Long {
        return exoPlayer.currentPosition
    }

    fun getShuffleMode(): Boolean {
        return isShuffleEnabled
    }

    fun getRepeatMode(): RepeatMode {
        return repeatMode
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }

    fun toggleRepeatMode() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.OFF
        }
    }

    fun releasePlayer() {
        exoPlayer.release()
    }

    fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0,
    ) {
        when (playerEvent) {
            PlayerEvent.SeekToNext -> {
                if (isShuffleEnabled) {
                    seekToRandom()
                } else {
                    val nextIndex = if (exoPlayer.hasNextMediaItem()) {
                        exoPlayer.nextMediaItemIndex
                    } else {
                        0
                    }
                    exoPlayer.seekToDefaultPosition(nextIndex)
                    exoPlayer.playWhenReady = true
                    if (!exoPlayer.isPlaying) {
                        exoPlayer.play()
                    }
                }
            }

            PlayerEvent.SeekToPrevious -> {
                if (isShuffleEnabled) {
                    seekToRandom()
                } else {
                    val previousIndex = if (exoPlayer.previousMediaItemIndex >= 0) {
                        exoPlayer.previousMediaItemIndex
                    } else {
                        exoPlayer.mediaItemCount - 1
                    }
                    exoPlayer.seekToDefaultPosition(previousIndex)
                    exoPlayer.playWhenReady = true
                    if (!exoPlayer.isPlaying) {
                        exoPlayer.play()
                    }
                }
            }

            PlayerEvent.SelectedAudioChange -> {
                when (selectedAudioIndex) {
                    exoPlayer.currentMediaItemIndex -> {
                        playOrPause()
                    }

                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                        exoPlayer.playWhenReady = true

                        if (!exoPlayer.isPlaying){
                            exoPlayer.play()
                        }

                        _musicState.value = MusicState.Playing(isPlaying = true)
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
            PlayerEvent.PlayPause -> playOrPause()
            PlayerEvent.Backward -> exoPlayer.seekBack()
            PlayerEvent.Forward -> exoPlayer.seekForward()
            PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> _musicState.value = MusicState.Buffering(exoPlayer.currentPosition)
            ExoPlayer.STATE_READY -> _musicState.value = MusicState.Ready(exoPlayer.duration)
            ExoPlayer.STATE_ENDED -> handleEndOfPlayback()
            ExoPlayer.STATE_IDLE -> return
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _musicState.value = MusicState.Playing(isPlaying = isPlaying)
        _musicState.value = MusicState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
        if (isPlaying) {
            scope.launch { startProgressUpdate() }
        } else {
            stopProgressUpdate()
        }
    }

    private fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            _musicState.value = MusicState.Playing(isPlaying = true)
            startProgressUpdate()
        }
    }

    private fun seekToRandom() {
        val mediaItemsCount = exoPlayer.mediaItemCount
        if (mediaItemsCount > 0) {
            val randomIndex = (0 until mediaItemsCount).random()
            exoPlayer.seekToDefaultPosition(randomIndex)
            _musicState.value = MusicState.CurrentPlaying(randomIndex)
            exoPlayer.playWhenReady = true
            startProgressUpdate()
        }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        job = scope.launch {
            while (isActive) {
                delay(500)
                _musicState.value = MusicState.Progress(exoPlayer.currentPosition)
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _musicState.value = MusicState.Playing(isPlaying = false)
    }

    private fun handleEndOfPlayback() {
        when (repeatMode) {
            RepeatMode.OFF -> {
                exoPlayer.seekToDefaultPosition()
                exoPlayer.pause()
                _musicState.value = MusicState.Playing(isPlaying = false)
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

                if (!exoPlayer.isPlaying){
                    exoPlayer.play()
                }

                _musicState.value = MusicState.Playing(isPlaying = true)
                startProgressUpdate()
            }
        }
    }
}