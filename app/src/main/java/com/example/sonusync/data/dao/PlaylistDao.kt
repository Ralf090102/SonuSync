package com.example.sonusync.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sonusync.data.model.Playlist

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    suspend fun getAllPlaylists(): List<Playlist>

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(vararg playlists: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
}