package com.example.sonusync.di

import android.content.ContentResolver
import com.example.sonusync.data.local.MusicDao
import com.example.sonusync.data.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMusicRepository(contentResolver: ContentResolver, musicDao: MusicDao): MusicRepository {
        return MusicRepository(contentResolver, musicDao)
    }
}