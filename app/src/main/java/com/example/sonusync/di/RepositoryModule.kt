package com.example.sonusync.di

import android.content.ContentResolver
import com.example.sonusync.data.dao.AlbumDao
import com.example.sonusync.data.dao.ArtistDao
import com.example.sonusync.data.dao.MusicDao
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
    fun provideMusicRepository(
        contentResolver: ContentResolver,
        musicDao: MusicDao,
        artistDao: ArtistDao,
        albumDao: AlbumDao
    ): MusicRepository {
        return MusicRepository(contentResolver, musicDao, artistDao, albumDao)
    }
}