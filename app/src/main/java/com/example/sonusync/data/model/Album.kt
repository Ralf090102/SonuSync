package com.example.sonusync.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "album")
data class Album(
    @PrimaryKey override val id: Long,
    override val name: String,
    override val artUri: String
) : Ensemble
