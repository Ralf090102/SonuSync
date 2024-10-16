package com.example.sonusync.data.model

import androidx.room.PrimaryKey

data class Playlist (
    @PrimaryKey val id: Long,
    val name: String,
)