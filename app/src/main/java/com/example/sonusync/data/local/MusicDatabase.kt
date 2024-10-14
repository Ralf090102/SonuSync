package com.example.sonusync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sonusync.domain.model.Music

@Database(entities = [Music::class], version = 1, exportSchema = false)
abstract  class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object{
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}