package com.example.sonusync.viewmodel

import android.util.Log
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
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> get() = _albums

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> get() = _artists

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    fun insertEnsembles(){
        viewModelScope.launch {
            try {
                val musicData = musicRepository.getMusicFromStorage()

                val artistList = musicData.artistList
                musicRepository.saveArtistListToLocal(artistList)

                val albumList = musicData.albumList
                musicRepository.saveAlbumListToLocal(albumList)

                // Might Be Implemented In The Future
                val playlistList = null
                musicRepository.saveMusicListToLocal(emptyList())
            } catch (e: Exception) {
                Log.e("EnsembleViewModel", "Error inserting ensembles", e)
            }
        }
    }

    fun loadEnsembles(){
        viewModelScope.launch {
            try {
                val artists = musicRepository.getArtistListFromLocal()
                _artists.postValue(artists)

                val albums = musicRepository.getAlbumListFromLocal()
                _albums.postValue(albums)

                // Might Be Implemented In The Future
                val playlistsList = null
                _playlists.postValue(emptyList())
            } catch (e: Exception) {
                Log.e("EnsembleViewModel", "Error loading ensembles", e)
            }
        }
    }
}