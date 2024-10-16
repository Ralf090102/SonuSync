package com.example.sonusync.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonusync.data.model.Music
import com.example.sonusync.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel  @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _musicList = MutableLiveData<List<Music>>(emptyList())
    val musicList: LiveData<List<Music>> get() = _musicList

    private val _currentMusicIndex = MutableLiveData<Int>().apply { value = 0 }
    val currentMusicIndex: LiveData<Int> get() = _currentMusicIndex

    fun insertMusic() {
        viewModelScope.launch {
            try {
                val musicList = musicRepository.getMusicFromStorage()
                musicRepository.saveMusicListToLocal(musicList)
                loadMusic()
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error inserting music", e)
            }
        }
    }

    fun loadMusic(){
        viewModelScope.launch {
            try {
                val music = musicRepository.getMusicListFromLocal()
                _musicList.postValue(music)
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error loading music from database", e)
            }
        }
    }

    fun playNext() {
        val index = _currentMusicIndex.value ?: return
        val newIndex = (index + 1) % (_musicList.value?.size ?: 1)
        _currentMusicIndex.postValue(newIndex)
    }

    fun playPrevious() {
        val index = _currentMusicIndex.value ?: return
        val newIndex = if (index > 0) index - 1 else (_musicList.value?.size ?: 1) - 1
        _currentMusicIndex.postValue(newIndex)
    }

    fun findMusic(title: String) {
        viewModelScope.launch {
            try {
                val music = musicRepository.findMusicByTitle(title)
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error finding music by title: $title", e)
            }
        }
    }

    fun deleteMusic(musicId: Long) {
        viewModelScope.launch {
            try {
                musicRepository.deleteMusic(musicId)
                loadMusic()
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error deleting music with ID: $musicId", e)
            }
        }
    }
}