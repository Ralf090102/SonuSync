package com.example.sonusync.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sonusync.data.dao.AlbumDao
import com.example.sonusync.data.model.Album
import com.example.sonusync.data.typeconverter.MusicListTypeConverter

@Database(entities = [Album::class], version = 1, exportSchema = false)
@TypeConverters(MusicListTypeConverter::class)
abstract class AlbumDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao

    companion object {
        @Volatile
        private var INSTANCE: AlbumDatabase? = null

        fun getDatabase(context: Context): AlbumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlbumDatabase::class.java,
                    "album_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}