package com.example.sonusync.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sonusync.data.dao.PlaylistDao
import com.example.sonusync.data.model.Playlist
import com.example.sonusync.data.typeconverter.MusicListTypeConverter

@Database(entities = [Playlist::class], version = 1, exportSchema = false)
@TypeConverters(MusicListTypeConverter::class)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: PlaylistDatabase? = null

        fun getDatabase(context: Context): PlaylistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaylistDatabase::class.java,
                    "playlist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}