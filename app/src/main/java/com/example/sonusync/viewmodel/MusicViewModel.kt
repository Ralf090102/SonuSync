package com.example.sonusync.viewmodel

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.sonusync.data.enums.RepeatMode
import com.example.sonusync.data.model.Music
import com.example.sonusync.data.repository.MusicRepository
import com.example.sonusync.service.MusicServiceHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
@OptIn(SavedStateHandleSaveableApi::class)
class MusicViewModel  @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicServiceHandler: MusicServiceHandler,
    private val savedStateHandle: SavedStateHandle,
    private val preferences: SharedPreferences
) : ViewModel() {

    private companion object {
        private const val KEY_REPEAT_MODE = "repeatMode"
        private const val KEY_SHUFFLE = "isShuffleEnabled"
        private const val KEY_SELECTED_INDEX = "selectedIndex"
    }

    var duration by savedStateHandle.saveable { mutableLongStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var selectedMusic by savedStateHandle.saveable { mutableStateOf(savedStateHandle.get<Music?>("selectedMusic")) }
    var musicList by savedStateHandle.saveable { mutableStateOf(listOf<Music>()) }

    var repeatMode: RepeatMode = RepeatMode.valueOf(preferences.getString(KEY_REPEAT_MODE, musicServiceHandler.getRepeatMode().name) ?: RepeatMode.OFF.name)
        set(value) {
            field = value
            preferences.edit().putString(KEY_REPEAT_MODE, value.name).apply()
        }

    var isShuffleEnabled: Boolean = preferences.getBoolean(KEY_SHUFFLE, false)
        set(value) {
            field = value
            preferences.edit().putBoolean(KEY_SHUFFLE, value).apply()
        }

    private val _selectedIndex = MutableLiveData<Int>().apply { value = preferences.getInt(KEY_SELECTED_INDEX, -1) }
    val selectedIndex: LiveData<Int> get() = _selectedIndex

    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _musicFlow = MutableStateFlow(musicList)
    val musicFlow: StateFlow<List<Music>> get() = _musicFlow

    private val _filteredMusicFlow = MutableStateFlow<List<Music>>(emptyList())
    val filteredMusicFlow: StateFlow<List<Music>> get() = _filteredMusicFlow

    private val _queryMusicList = MediatorLiveData<List<Music>>()
    val queryMusicList: LiveData<List<Music>> = _queryMusicList

    private val _ldIsPlaying = MutableLiveData<Boolean>()
    val ldIsPlaying: LiveData<Boolean> = _ldIsPlaying

    private val _searchQuery = MutableLiveData("")

    sealed class UIEvents {
        object PlayPause : UIEvents()
        object SeekToPrevious : UIEvents()
        object SeekToNext : UIEvents()
        object Backward : UIEvents()
        object Forward : UIEvents()
        object Shuffle : UIEvents()
        object Repeat : UIEvents()
        data class SelectedAudioChange(val index: Int) : UIEvents()
        data class SeekTo(val position: Float) : UIEvents()
        data class UpdateProgress(val newProgress: Float) : UIEvents()
    }

    sealed class UIState {
        object Initial : UIState()
        object Ready : UIState()
        data class Error(val message: String) : UIState()
    }

    init {
        _selectedIndex.value?.let { index -> selectedMusic = musicList.getOrNull(index) }
        insertMusic()
        observeMusicServiceState()

        _queryMusicList.addSource(_searchQuery) { filterMusicByQuery() }
    }

    override fun onCleared() {
        viewModelScope.launch {
            musicServiceHandler.onPlayerEvents(MusicServiceHandler.PlayerEvent.Stop)
        }
        super.onCleared()
    }

    fun insertMusic() {
        viewModelScope.launch {
            try {
                val musicData = musicRepository.getMusicFromStorage()
                musicList = musicData.musicList
                _musicFlow.value = musicData.musicList
                loadMusic()
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error inserting music", e)
            }
        }
    }

    fun loadMusic(){
        musicList.map { music ->
            MediaItem.Builder()
                .setUri(music.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(music.artist)
                        .setDisplayTitle(music.title)
                        .build()
                )
                .build()
        }.also {
            musicServiceHandler.setMediaItemList(it)
        }
    }

    fun onUiEvents(uiEvents: UIEvents) = viewModelScope.launch {
        when (uiEvents) {
            UIEvents.Backward -> musicServiceHandler.onPlayerEvents(MusicServiceHandler.PlayerEvent.Backward)
            UIEvents.Forward -> musicServiceHandler.onPlayerEvents(MusicServiceHandler.PlayerEvent.Forward)
            UIEvents.SeekToPrevious -> musicServiceHandler.onPlayerEvents(MusicServiceHandler.PlayerEvent.SeekToPrevious)
            UIEvents.SeekToNext -> musicServiceHandler.onPlayerEvents(MusicServiceHandler.PlayerEvent.SeekToNext)
            UIEvents.Shuffle -> toggleShuffle()
            UIEvents.Repeat -> toggleRepeatMode()

            is UIEvents.PlayPause -> {
                musicServiceHandler.onPlayerEvents(
                    MusicServiceHandler.PlayerEvent.PlayPause
                )

                _ldIsPlaying.value = musicServiceHandler.getPlayback()
            }

            is UIEvents.SeekTo -> {
                musicServiceHandler.onPlayerEvents(
                    MusicServiceHandler.PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }

            is UIEvents.SelectedAudioChange -> {
                musicServiceHandler.onPlayerEvents(
                    MusicServiceHandler.PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
            }

            is UIEvents.UpdateProgress -> {
                musicServiceHandler.onPlayerEvents(
                    MusicServiceHandler.PlayerEvent.UpdateProgress(
                        uiEvents.newProgress
                    )
                )
                progress = uiEvents.newProgress
            }
        }
    }

    fun getIsShuffled(): Boolean {
        return isShuffleEnabled
    }

    fun fetchRepeatMode(): RepeatMode {
        return repeatMode
    }

    fun selectAndPlayMusic(music: Music) {
        val index = musicList.indexOfFirst { it.id == music.id }
        if (index != -1) {
            _selectedIndex.value = index
            preferences.edit().putInt(KEY_SELECTED_INDEX, index).apply()
            onUiEvents(UIEvents.SelectedAudioChange(index))
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun filterMusicByAlbum(albumName: String) {
        _filteredMusicFlow.value = musicList.filter { it.album == albumName }
    }

    fun filterMusicByArtist(artistName: String) {
        _filteredMusicFlow.value = musicList.filter { it.artist == artistName }
    }

    fun clearFilteredMusicList() {
        _filteredMusicFlow.value = emptyList()
    }

    fun updatePlaybackPosition() {
        val currentProgress = musicServiceHandler.getCurrentPlaybackPosition()
        updateProgressState(currentProgress)
    }

    fun onSeekBarChanged(progressPercentage: Int) {
        val positionMs = (progressPercentage / 100f) * duration
        onUiEvents(UIEvents.SeekTo(positionMs))
    }

    fun releasePlayer() {
        musicServiceHandler.releasePlayer()
    }

    @SuppressLint("DefaultLocale")
    fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun observeMusicServiceState() {
        viewModelScope.launch {
            musicServiceHandler.musicState.collectLatest { mediaState ->
                when (mediaState) {
                    MusicServiceHandler.MusicState.Initial -> _uiState.value = UIState.Initial
                    is MusicServiceHandler.MusicState.Buffering -> updateProgressState(mediaState.progress)
                    is MusicServiceHandler.MusicState.Progress -> updateProgressState(mediaState.progress)
                    is MusicServiceHandler.MusicState.Playing -> {
                        isPlaying = mediaState.isPlaying
                        _ldIsPlaying.value = musicServiceHandler.getPlayback()
                    }
                    is MusicServiceHandler.MusicState.CurrentPlaying -> {
                        if (mediaState.mediaItemIndex in musicList.indices) {
                            selectedMusic = musicList[mediaState.mediaItemIndex]
                        } else {
                            Log.e("MusicViewModel", "Invalid media index: ${mediaState.mediaItemIndex}")
                        }
                    }
                    is MusicServiceHandler.MusicState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }
            }
        }
    }

    private fun toggleShuffle() {
        musicServiceHandler.toggleShuffle()
        isShuffleEnabled = musicServiceHandler.getShuffleMode()
    }

    private fun toggleRepeatMode() {
        musicServiceHandler.toggleRepeatMode()
        repeatMode = musicServiceHandler.getRepeatMode()
    }

    private fun updateProgressState(currentProgress: Long) {
        progress = if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f) else 0f
        progressString = formatDuration(currentProgress)
    }

    private fun filterMusicByQuery() {
        val query = _searchQuery.value ?: ""
        _queryMusicList.value = if (query.isBlank()) {
            musicList
        } else {
            musicList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
    }
}