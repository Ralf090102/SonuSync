package com.example.sonusync.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "music")
data class Music (
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val albumArtist: String,
    val album: String,
    val trackNumber: Int,
    val discNumber: Int,
    val year: Int,
    val duration: Long,
    val path: String,
    val albumArtUri: String?,
    val uri: String
) : Parcelable