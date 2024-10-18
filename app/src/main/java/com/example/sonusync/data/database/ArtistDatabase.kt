package com.example.sonusync.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sonusync.data.dao.ArtistDao
import com.example.sonusync.data.model.Artist
import com.example.sonusync.data.typeconverter.MusicListTypeConverter

@Database(entities = [Artist::class], version = 1, exportSchema = false)
@TypeConverters(MusicListTypeConverter::class)
abstract class ArtistDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao

    companion object {
        @Volatile
        private var INSTANCE: ArtistDatabase? = null

        fun getDatabase(context: Context): ArtistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArtistDatabase::class.java,
                    "artist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}