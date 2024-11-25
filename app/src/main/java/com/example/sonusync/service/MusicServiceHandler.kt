package com.example.sonusync.service

import android.content.SharedPreferences
import android.util.Log
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

    private val _isShuffled = MutableLiveData<Boolean>().apply { value = sharedPreferences.getBoolean(PREF_SHUFFLE_STATE, false) }
    val isShuffleEnabled: LiveData<Boolean> get() = _isShuffled

    private val _repeatMode = MutableLiveData<RepeatMode>().apply { value = RepeatMode.OFF }
    val repeatMode: LiveData<RepeatMode> get() = _repeatMode

    private val _selectedIndex = MutableLiveData<Int>().apply { value = sharedPreferences.getInt(PREF_CURRENT_MUSIC_INDEX, 0) }
    val selectedIndex: LiveData<Int> get() = _selectedIndex

    private val _musicState: MutableStateFlow<MusicState> = MutableStateFlow(MusicState.Initial)
    val musicState: StateFlow<MusicState> = _musicState.asStateFlow()

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
        val savedRepeatModeOrdinal = sharedPreferences.getInt(PREF_REPEAT_STATE, RepeatMode.OFF.ordinal)
        val savedRepeatMode = RepeatMode.entries.toTypedArray().getOrElse(savedRepeatModeOrdinal) { RepeatMode.OFF }

        _repeatMode.value = savedRepeatMode
        exoPlayer.repeatMode = when (savedRepeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }

        val savedMusicIndex = sharedPreferences.getInt(PREF_CURRENT_MUSIC_INDEX, 0)
        _selectedIndex.value = savedMusicIndex

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
        return _isShuffled.value ?: false
    }

    fun getRepeatMode(): RepeatMode {
        return _repeatMode.value ?: RepeatMode.OFF
    }


    fun toggleShuffle() {
        val newShuffleState = !(_isShuffled.value ?: false)
        _isShuffled.value = newShuffleState

        sharedPreferences.edit().putBoolean(PREF_SHUFFLE_STATE, newShuffleState).apply()
    }

    fun toggleRepeatMode() {
        val currentMode = _repeatMode.value ?: RepeatMode.OFF

        val newRepeatMode = when (currentMode) {
            RepeatMode.OFF -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.OFF
        }

        _repeatMode.value = newRepeatMode

        exoPlayer.repeatMode = when (newRepeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }

        sharedPreferences.edit().putInt(PREF_REPEAT_STATE, newRepeatMode.ordinal).apply()
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
                if (isShuffleEnabled.value == true) {
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

                    _selectedIndex.value = nextIndex
                }
            }

            PlayerEvent.SeekToPrevious -> {
                if (isShuffleEnabled.value == true) {
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

                    _selectedIndex.value = previousIndex
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
                        _selectedIndex.value = selectedAudioIndex

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
            Player.STATE_BUFFERING -> _musicState.value = MusicState.Buffering(exoPlayer.currentPosition)
            Player.STATE_READY -> _musicState.value = MusicState.Ready(exoPlayer.duration)
            Player.STATE_ENDED -> return
            Player.STATE_IDLE -> return
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
            _selectedIndex.value = randomIndex

            startProgressUpdate()
        }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        job = scope.launch {
            while (isActive) {
                delay(500)
                _musicState.value = MusicState.Progress(exoPlayer.currentPosition)
                Log.d("DebugLog", "Current position: ${exoPlayer.currentPosition}, Duration: ${exoPlayer.duration}")
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _musicState.value = MusicState.Playing(isPlaying = false)
    }
}