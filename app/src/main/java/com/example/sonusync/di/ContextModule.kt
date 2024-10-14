package com.example.sonusync.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ContextModule {

    @Binds
    abstract fun bindContext(@ApplicationContext appContext: Context): Context
}