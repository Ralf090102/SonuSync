package com.example.sonusync.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonusync.data.model.Music
import com.example.sonusync.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    companion object {
        private const val PREF_CURRENT_MUSIC_INDEX = "pref_current_music_index"
    }

    private val _currentMusicIndex = MutableLiveData<Int>().apply { value = sharedPreferences.getInt(
        PREF_CURRENT_MUSIC_INDEX, 0) }
    val currentMusicIndex: LiveData<Int> get() = _currentMusicIndex

    private val _musicList = MutableLiveData<List<Music>>(emptyList())
    val musicList: LiveData<List<Music>> get() = _musicList

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredMusicList = MediatorLiveData<List<Music>>()
    val filteredMusicList: LiveData<List<Music>> = _filteredMusicList

    init {
        _filteredMusicList.addSource(musicList) { filterMusic() }
        _filteredMusicList.addSource(_searchQuery) { filterMusic() }
    }

    private fun filterMusic() {
        val query = _searchQuery.value ?: ""
        _filteredMusicList.value = if (query.isBlank()) {
            musicList.value
        } else {
            musicList.value?.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertMusic() {
        viewModelScope.launch {
            try {
                val musicData = musicRepository.getMusicFromStorage()
                val musicList = musicData.musicList
                musicRepository.saveMusicListToLocal(musicList)
                loadMusic()
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error inserting music", e)
            }
        }
    }

    fun loadMusic(){
        viewModelScope.launch {
            try {
                val music = musicRepository.getMusicListFromLocal()
                _musicList.postValue(music)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error loading music from database", e)
            }
        }
    }

    fun selectMusicAtIndex(index: Int) {
        if (index in 0 until (_musicList.value?.size ?: 0)) {
            _currentMusicIndex.value = index

            sharedPreferences.edit()
                .putInt(SearchViewModel.PREF_CURRENT_MUSIC_INDEX, index)
                .apply()
        }
    }
}