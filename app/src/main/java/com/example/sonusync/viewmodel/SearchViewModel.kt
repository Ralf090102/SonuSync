package com.example.sonusync.viewmodel

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
) : ViewModel() {

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
}