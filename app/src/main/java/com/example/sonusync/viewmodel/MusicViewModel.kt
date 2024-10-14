package com.example.sonusync.viewmodel

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
    private val repository: MusicRepository
) : ViewModel() {

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