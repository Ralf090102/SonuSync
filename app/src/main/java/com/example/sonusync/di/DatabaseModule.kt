package com.example.sonusync.di

import android.content.Context
import androidx.room.Room
import com.example.sonusync.data.dao.AlbumDao
import com.example.sonusync.data.dao.ArtistDao
import com.example.sonusync.data.dao.MusicDao
import com.example.sonusync.data.dao.PlaylistDao
import com.example.sonusync.data.database.AlbumDatabase
import com.example.sonusync.data.database.ArtistDatabase
import com.example.sonusync.data.database.MusicDatabase
import com.example.sonusync.data.database.PlaylistDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providesMusicDao(musicDatabase: MusicDatabase): MusicDao {
        return musicDatabase.musicDao()
    }

    @Provides
    @Singleton
    fun provideAlbumDatabase(@ApplicationContext context: Context): AlbumDatabase {
        return Room.databaseBuilder(
            context,
            AlbumDatabase::class.java,
            "album_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAlbumDao(albumDatabase: AlbumDatabase): AlbumDao {
        return albumDatabase.albumDao()
    }

    @Provides
    @Singleton
    fun provideArtistDatabase(@ApplicationContext context: Context): ArtistDatabase {
        return Room.databaseBuilder(
            context,
            ArtistDatabase::class.java,
            "artist_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideArtistDao(artistDatabase: ArtistDatabase): ArtistDao {
        return artistDatabase.artistDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDatabase(@ApplicationContext context: Context): PlaylistDatabase {
        return Room.databaseBuilder(
            context,
            PlaylistDatabase::class.java,
            "playlist_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(playlistDatabase: PlaylistDatabase): PlaylistDao {
        return playlistDatabase.playlistDao()
    }
}