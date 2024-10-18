package com.example.sonusync.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sonusync.data.model.Playlist

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlaylistListToLocal(playlistList: List<Playlist>)

    @Query("SELECT * FROM playlist ORDER BY name ASC")
    suspend fun getPlaylistListFromLocal(): List<Playlist>

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
}