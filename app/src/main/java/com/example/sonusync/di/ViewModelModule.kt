package com.example.sonusync.di

import android.content.SharedPreferences
import com.example.sonusync.viewmodel.MusicViewModel
import com.example.sonusync.data.repository.MusicRepository
import com.example.sonusync.viewmodel.EnsembleViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
object ViewModelModule {

    @Provides
    @Singleton
    fun provideMusicViewModel(musicRepository: MusicRepository, sharedPreferences: SharedPreferences): MusicViewModel {
        return MusicViewModel(musicRepository, sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideEnsembleViewModel(musicRepository: MusicRepository): EnsembleViewModel {
        return EnsembleViewModel(musicRepository)
    }
}