package com.example.sonusync.di

import android.content.Context
import androidx.room.Room
import com.example.sonusync.data.dao.MusicDao
import com.example.sonusync.data.database.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMusicDatabase(appContext: Context): MusicDatabase {
        return Room.databaseBuilder(
            appContext,
            MusicDatabase::class.java,
            "music_database"
        ).build()
    }

    @Provides
    fun providesMusicDao(musicDatabase: MusicDatabase): MusicDao {
        return musicDatabase.musicDao()
    }
}