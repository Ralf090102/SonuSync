package com.example.sonusync.viewmodel

import android.content.SharedPreferences
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
    private val musicRepository: MusicRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    companion object {
        private const val PREF_CURRENT_MUSIC_INDEX = "pref_current_music_index"
        private const val PREF_SHUFFLE_STATE = "pref_shuffle_state"
    }

    private val _musicList = MutableLiveData<List<Music>>(emptyList())
    val musicList: LiveData<List<Music>> get() = _musicList

    private val _currentMusicIndex = MutableLiveData<Int>().apply { value = sharedPreferences.getInt(PREF_CURRENT_MUSIC_INDEX, 0) }
    val currentMusicIndex: LiveData<Int> get() = _currentMusicIndex

    private val _isShuffled = MutableLiveData<Boolean>().apply { value = sharedPreferences.getBoolean(PREF_SHUFFLE_STATE, false) }
    val isShuffleEnabled: LiveData<Boolean> get() = _isShuffled

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

    fun selectMusicAtIndex(index: Int) {
        if (index in 0 until (_musicList.value?.size ?: 0)) {
            _currentMusicIndex.value = index

            sharedPreferences.edit()
                .putInt(PREF_CURRENT_MUSIC_INDEX, index)
                .apply()
        }
    }

    fun playNext() {
        val currentIndex = _currentMusicIndex.value ?: return
        val musicListSize = _musicList.value?.size ?: 1

        val newIndex = if (_isShuffled.value == true) {
            var randomIndex = (0 until musicListSize).random()
            while (randomIndex == currentIndex) {
                randomIndex = (0 until musicListSize).random()
            }
            randomIndex
        } else {
            (currentIndex + 1) % musicListSize
        }
        _currentMusicIndex.postValue(newIndex)

        sharedPreferences.edit()
            .putInt(PREF_CURRENT_MUSIC_INDEX, newIndex)
            .apply()
    }

    fun playPrevious() {
        val currentIndex = _currentMusicIndex.value ?: return
        val musicListSize = _musicList.value?.size ?: 1

        val newIndex = if (_isShuffled.value == true) {
            var randomIndex = (0 until musicListSize).random()
            while (randomIndex == currentIndex) {
                randomIndex = (0 until musicListSize).random()
            }
            randomIndex
        } else {
            if (currentIndex > 0) currentIndex - 1 else musicListSize - 1
        }
        _currentMusicIndex.postValue(newIndex)

        sharedPreferences.edit()
            .putInt(PREF_CURRENT_MUSIC_INDEX, newIndex)
            .apply()
    }

    fun toggleShuffle() {
        _isShuffled.value = _isShuffled.value?.not()
        sharedPreferences.edit()
            .putBoolean(PREF_SHUFFLE_STATE, _isShuffled.value ?: false)
            .apply()
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