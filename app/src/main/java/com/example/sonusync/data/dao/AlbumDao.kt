package com.example.sonusync.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sonusync.data.model.Album

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAlbumListToLocal(albumList: List<Album>)

    @Query("SELECT * FROM album ORDER BY name ASC")
    suspend fun getAlbumListFromLocal(): List<Album>

    @Delete
    suspend fun deleteAlbum(album: Album)
}