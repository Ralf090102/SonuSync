package com.example.sonusync.data.model

data class Music (
    val id: Long,
    val title: String,
    val artist: String,
    val albumArtist: String,
    val album: String,
    val trackNumber: Int,
    val discNumber: Int,
    val year: Int,
    val duration: Long,
    val path: String,
    val albumArtUri: String?
)