package com.example.sonusync.di

import android.content.SharedPreferences
import com.example.sonusync.viewmodel.MusicViewModel
import com.example.sonusync.data.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object ViewModelModule {

    @Provides
    fun provideMusicViewModel(musicRepository: MusicRepository, sharedPreferences: SharedPreferences): MusicViewModel {
        return MusicViewModel(musicRepository, sharedPreferences)
    }
}