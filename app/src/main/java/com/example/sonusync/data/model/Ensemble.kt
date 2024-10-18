package com.example.sonusync.data.model

interface Ensemble {
    val id: Long
    val name: String
    val coverUri: String?
    val songs: List<Music>
}