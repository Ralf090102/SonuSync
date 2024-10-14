package com.example.sonusync.di

import com.example.sonusync.data.adapters.MusicAdapter
import com.example.sonusync.ui.listeners.MusicClickListener
import com.example.sonusync.ui.music.library.AllSongsFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent


@Module
@InstallIn(FragmentComponent::class)
object AdapterModule {

    @Provides
    fun provideMusicClickListener(fragment: AllSongsFragment): MusicClickListener {
        return fragment
    }

    @Provides
    fun provideMusicAdapter(listener: MusicClickListener): MusicAdapter{
        return MusicAdapter(listener)
    }
}