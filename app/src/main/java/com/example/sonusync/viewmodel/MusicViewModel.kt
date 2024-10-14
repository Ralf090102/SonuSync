package com.example.sonusync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonusync.data.dao.MusicDao
import com.example.sonusync.data.model.Music
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel  @Inject constructor(
    private val musicDao: MusicDao
) : ViewModel() {

    private val _musicList = MutableLiveData<List<Music>>()
    val musicList: LiveData<List<Music>> get() = _musicList

    fun loadMusic(){
        viewModelScope.launch {
            _musicList.value = musicDao.getAllMusic()
        }
    }

    fun insertMusic(musicList: List<Music>) {
        viewModelScope.launch {
            musicDao.insertAll(musicList)
            loadMusic()
        }
    }

    fun findMusic(title: String) {
        viewModelScope.launch {
            val music = musicDao.findMusicByTitle(title)
        }
    }

    fun deleteMusic(musicId: Long) {
        viewModelScope.launch {
            musicDao.deleteMusic(musicId)
            loadMusic()
        }
    }
}