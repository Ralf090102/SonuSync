package com.example.sonusync.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sonusync.data.model.Album

@Dao
interface AlbumDao {
    @Query("SELECT * FROM album")
    suspend fun getAllAlbums(): List<Album>

    @Query("SELECT * FROM album WHERE id = :albumId")
    suspend fun getAlbumById(albumId: Long): Album?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(vararg albums: Album)

    @Delete
    suspend fun deleteAlbum(album: Album)
}