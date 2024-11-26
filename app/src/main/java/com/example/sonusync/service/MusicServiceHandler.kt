package com.example.sonusync.service

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val sharedPreferences: SharedPreferences
) : Player.Listener {

    companion object {
        private const val PREF_SHUFFLE_STATE = "pref_shuffle_state"
        private const val PREF_REPEAT_STATE = "pref_repeat_state"
        private const val PREF_CURRENT_MUSIC_INDEX = "pref_current_music_index"
    }

    private val _selectedIndex = MutableLiveData<Int>().apply { value = sharedPreferences.getInt(PREF_CURRENT_MUSIC_INDEX, 0) }
    val selectedIndex: LiveData<Int> get() = _selectedIndex

    private val _musicState: MutableStateFlow<MusicState> = MutableStateFlow(MusicState.Initial)
    val musicState: StateFlow<MusicState> = _musicState.asStateFlow()

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private var filteredMusicIndices: List<Int> = emptyList()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    sealed class PlayerEvent {
        data object PlayPause : PlayerEvent()
        data object SelectedAudioChange : PlayerEvent()
        data object Forward : PlayerEvent()
        data object Backward : PlayerEvent()
        data object SeekToNext : PlayerEvent()
        data object SeekToPrevious : PlayerEvent()
        data object SeekTo : PlayerEvent()
        data object Stop : PlayerEvent()
        data object Shuffle : PlayerEvent()
        data object Repeat : PlayerEvent()
        data class UpdateProgress(val newProgress: Float) : PlayerEvent()
    }

    sealed class MusicState {
        data object Initial : MusicState()
        data class Ready(val duration: Long) : MusicState()
        data class Progress(val progress: Long) : MusicState()
        data class Buffering(val progress: Long) : MusicState()
        data class Playing(val isPlaying: Boolean) : MusicState()
        data class CurrentPlaying(val mediaItemIndex: Int) : MusicState()
    }

    init {
        exoPlayer.shuffleModeEnabled = sharedPreferences.getBoolean(PREF_SHUFFLE_STATE, false)
        exoPlayer.repeatMode = sharedPreferences.getInt(PREF_REPEAT_STATE, Player.REPEAT_MODE_OFF)

        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun setFilteredMusicIndices(indices: List<Int>) {
        filteredMusicIndices = indices
    }

    fun setMediaItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    fun getCurrentMediaDuration(): Long {
        return exoPlayer.duration
    }

    fun getCurrentPlaybackPosition(): Long {
        return exoPlayer.currentPosition
    }

    fun getShuffleMode(): Boolean {
        return exoPlayer.shuffleModeEnabled
    }

    fun getRepeatMode(): RepeatMode {
        return when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> RepeatMode.OFF
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
    }

    fun toggleShuffle() {
        val currentShuffleState = exoPlayer.shuffleModeEnabled
        val newShuffleState = !currentShuffleState
        exoPlayer.shuffleModeEnabled = newShuffleState

        sharedPreferences.edit().putBoolean(PREF_SHUFFLE_STATE, newShuffleState).apply()
    }

    fun toggleRepeatMode() {
        val currentRepeatMode = exoPlayer.repeatMode
        val newRepeatMode = when (currentRepeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }

        exoPlayer.repeatMode = newRepeatMode

        sharedPreferences.edit().putInt(PREF_REPEAT_STATE, newRepeatMode).apply()
    }

    fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0,
    ) {
        when (playerEvent) {
            PlayerEvent.SeekToNext -> {
                if (filteredMusicIndices.isNotEmpty()) {
                    val currentIndex = exoPlayer.currentMediaItemIndex
                    val nextIndex = if (exoPlayer.shuffleModeEnabled) {
                        filteredMusicIndices.random()
                    } else {
                        filteredMusicIndices.firstOrNull { it > currentIndex } ?: filteredMusicIndices.first()
                    }

                    exoPlayer.seekToDefaultPosition(nextIndex)
                    exoPlayer.playWhenReady = true
                    if (!exoPlayer.isPlaying) {
                        exoPlayer.play()
                    }
                } else {
                    if (exoPlayer.hasNextMediaItem()) {
                        exoPlayer.seekToDefaultPosition(exoPlayer.nextMediaItemIndex)
                        exoPlayer.playWhenReady = true
                        if (!exoPlayer.isPlaying) {
                            exoPlayer.play()
                        }
                    }
                }
            }

            PlayerEvent.SeekToPrevious -> {
                if (filteredMusicIndices.isNotEmpty()) {
                    val currentIndex = exoPlayer.currentMediaItemIndex
                    val previousIndex = if (exoPlayer.shuffleModeEnabled) {
                        filteredMusicIndices.random()
                    } else {
                        filteredMusicIndices.lastOrNull { it < currentIndex } ?: filteredMusicIndices.last()
                    }

                    exoPlayer.seekToDefaultPosition(previousIndex)
                    exoPlayer.playWhenReady = true

                    if (!exoPlayer.isPlaying) {
                        exoPlayer.play()
                    }
                } else {
                    if (exoPlayer.previousMediaItemIndex >= 0) {
                        exoPlayer.seekToDefaultPosition(exoPlayer.previousMediaItemIndex)
                        exoPlayer.playWhenReady = true
                        if (!exoPlayer.isPlaying) {
                            exoPlayer.play()
                        }
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

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            when (exoPlayer.repeatMode) {
                Player.REPEAT_MODE_OFF -> {
                    if (exoPlayer.hasNextMediaItem()) { _selectedIndex.value = exoPlayer.currentMediaItemIndex }
                }
                Player.REPEAT_MODE_ALL -> _selectedIndex.value = exoPlayer.currentMediaItemIndex
                Player.REPEAT_MODE_ONE -> return
            }
        } else {
            _selectedIndex.value = exoPlayer.currentMediaItemIndex
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> _musicState.value = MusicState.Buffering(exoPlayer.currentPosition)
            Player.STATE_READY -> _musicState.value = MusicState.Ready(exoPlayer.duration)
            Player.STATE_ENDED -> return
            Player.STATE_IDLE -> return
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
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
}