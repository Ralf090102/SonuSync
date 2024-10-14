package com.example.sonusync.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonusync.data.repository.MusicRepository
import kotlinx.coroutines.launch

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {
    private val _musicList = MutableLiveData<List<Music>>()
    val musicList: LiveData<List<Music>> get() = _musicList

    fun loadMusic(){
        viewModelScope.launch {
            val musicFromStorage = repository.getMusicFromStorage()
            _musicList.value = musicFromStorage
            repository.saveMusicListToLocal(musicFromStorage)
        }
    }
}