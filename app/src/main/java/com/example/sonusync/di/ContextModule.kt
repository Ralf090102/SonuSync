package com.example.sonusync.di

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContextModule {

    @Binds
    abstract fun bindContext(@ApplicationContext appContext: Context): Context

    companion object {
        @Provides
        @Singleton
        fun bindContentResolver(@ApplicationContext appContext: Context): ContentResolver {
            return appContext.contentResolver
        }
    }
}