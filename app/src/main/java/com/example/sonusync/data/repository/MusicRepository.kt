package com.example.sonusync.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.example.sonusync.data.dao.MusicDao
import com.example.sonusync.data.model.Music
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val musicDao: MusicDao
) {
    fun getMusicFromStorage(): List<Music>{
        val musicList = mutableListOf<Music>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DISC_NUMBER,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection: String = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val sort: String = MediaStore.Audio.Media.TITLE + " ASC"

        val cursor = contentResolver.query(uri, projection, selection, null, sort)

        cursor?.use{
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumArtistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val trackColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val discNumberColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISC_NUMBER)
            val yearColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val albumArtist = it.getString(albumArtistColumn)
                val album = it.getString(albumColumn)
                val track = it.getInt(trackColumn)
                val discNumber = it.getInt(discNumberColumn)
                val year = it.getInt(yearColumn)
                val duration = it.getLong(durationColumn)
                val data = it.getString(dataColumn)
                val albumId = it.getLong(albumIdColumn)

                val albumArtUri = getAlbumArtUri(albumId)

                val music = Music(
                    id = id,
                    title = title,
                    artist = artist,
                    albumArtist = albumArtist,
                    album = album,
                    trackNumber = track,
                    discNumber = discNumber,
                    year = year,
                    duration = duration,
                    path = data,
                    albumArtUri = albumArtUri
                )
                musicList.add(music)
            }
        }
        return musicList
    }
    private fun getAlbumArtUri(albumId: Long): String? {
        val uri: Uri = Uri.withAppendedPath(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId.toString())
        return uri.toString()
    }

    suspend fun getLocalMusicList(): List<Music> {
        return musicDao.getAllMusic()
    }

    suspend fun saveMusicListToLocal(musicList: List<Music>) {
        musicDao.insertAll(musicList)
    }
}