package com.example.sonusync.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artist")
data class Artist(
    @PrimaryKey override val id: Long,
    override val name: String,
    override val coverUri: String?,
    override val songs: List<Music> = emptyList()
) : Ensemble
