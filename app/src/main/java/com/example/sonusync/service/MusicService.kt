package com.example.sonusync.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        //WIP
        return null
    }

    override fun onCreate() {
        super.onCreate()
        TODO()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //WIP
        return START_STICKY
    }
}