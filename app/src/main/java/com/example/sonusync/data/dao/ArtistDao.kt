package com.example.sonusync.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sonusync.data.model.Artist

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artist")
    suspend fun getAllArtists(): List<Artist>

    @Query("SELECT * FROM artist WHERE id = :artistId")
    suspend fun getArtistById(artistId: Long): Artist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(vararg artists: Artist)

    @Delete
    suspend fun deleteArtist(artist: Artist)
}