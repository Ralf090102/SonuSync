package com.example.sonusync.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sonusync.data.model.Artist

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArtistListToLocal(artistList: List<Artist>)

    @Query("SELECT * FROM artist ORDER BY name ASC")
    suspend fun getArtistListFromLocal(): List<Artist>

    @Delete
    suspend fun deleteArtist(artist: Artist)
}