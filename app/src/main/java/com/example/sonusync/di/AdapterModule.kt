package com.example.sonusync.di

import com.example.sonusync.data.adapters.MusicAdapter
import com.example.sonusync.ui.listeners.MusicClickListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent


@Module
@InstallIn(FragmentComponent::class)
object AdapterModule {

    @Provides
    fun provideMusicAdapter(listener: MusicClickListener): MusicAdapter{
        return MusicAdapter(listener)
    }
}