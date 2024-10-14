package com.example.sonusync.di

import com.example.sonusync.data.adapters.MusicAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent


@Module
@InstallIn(FragmentComponent::class)
object AdapterModule {

    @Provides
    fun provideMusicAdapter(): MusicAdapter {
        return MusicAdapter(emptyList())
    }
}