package com.example.sonusync.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sonusync.data.model.Music


@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(musicList: List<Music>)

    @Query("SELECT * FROM music ORDER BY title ASC")
    suspend fun getAllMusic(): List<Music>

    @Query("SELECT * FROM music WHERE title LIKE :title LIMIT 1")
    suspend fun findMusicByTitle(title: String): Music

    @Query("DELETE FROM music WHERE id = :musicId")
    suspend fun deleteMusic(musicId: Long)
}