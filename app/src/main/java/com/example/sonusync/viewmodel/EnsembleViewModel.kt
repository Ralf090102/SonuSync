package com.example.sonusync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonusync.data.dao.AlbumDao
import com.example.sonusync.data.dao.ArtistDao
import com.example.sonusync.data.dao.PlaylistDao
import com.example.sonusync.data.model.Album
import com.example.sonusync.data.model.Artist
import com.example.sonusync.data.model.Playlist
import com.example.sonusync.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnsembleViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> get() = _albums

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> get() = _artists

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    init {
        loadAlbums()
        loadArtists()
        loadPlaylists()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            val albumList = albumDao.getAllAlbums()
            _albums.postValue(albumList)
        }
    }

    private fun loadArtists() {
        viewModelScope.launch {
            val aristList = artistDao.getAllArtists()
            _artists.postValue(aristList)
        }
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            val playlistList = playlistDao.getAllPlaylists()
            _playlists.postValue(playlistList)
        }
    }
}