package com.example.sonusync.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.sonusync.data.dao.AlbumDao
import com.example.sonusync.data.dao.ArtistDao
import com.example.sonusync.data.dao.MusicDao
import com.example.sonusync.data.model.Album
import com.example.sonusync.data.model.Artist
import com.example.sonusync.data.model.Music
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val musicDao: MusicDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao
) {

    data class MusicData(
        val musicList: List<Music>,
        val artistList: List<Artist>,
        val albumList: List<Album>
    )

    fun getMusicFromStorage(): MusicData {
        val musicList = mutableListOf<Music>()
        val artistSet = mutableSetOf<Artist>()
        val albumSet = mutableSetOf<Album>()

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
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
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
            val artistIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)

            try {
                while (it.moveToNext()) {
                    try {
                        val id = it.getLong(idColumn)
                        val title = it.getString(titleColumn) ?: "Unknown Title"
                        val artist = it.getString(artistColumn) ?: "Unknown Artist"
                        val albumArtist = it.getString(albumArtistColumn) ?: "Unknown Album Artist"
                        val album = it.getString(albumColumn) ?: "Unknown Album"
                        val track = it.getInt(trackColumn)
                        val discNumber = it.getInt(discNumberColumn)
                        val year = it.getInt(yearColumn)
                        val duration = it.getLong(durationColumn)
                        val data = it.getString(dataColumn) ?: ""
                        val albumId = it.getLong(albumIdColumn)
                        val artistId = it.getLong(artistIdColumn)

                        val albumArtUri = getAlbumArtUri(albumId)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                        val albumEntry = Album(id = albumId, name = album)
                        val artistEntry = Artist(id = artistId, name = artist)

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
                            albumArtUri = albumArtUri,
                            uri = contentUri.toString()
                        )

                        musicList.add(music)
                        artistSet.add(artistEntry)
                        albumSet.add(albumEntry)

                    } catch (e: Exception) {
                        Log.e("MusicRepository", "Error retrieving music data: ${e.message}", e)
                    }

                }

            } catch (e: Exception) {
                Log.e("MusicRepository", "Error iterating over music cursor: ${e.message}", e)
            }

        }

        return MusicData(
            musicList = musicList,
            artistList = artistSet.toList(),
            albumList = albumSet.toList()
        )
    }

    private fun getAlbumArtUri(albumId: Long): String {
        val uri: Uri = Uri.withAppendedPath(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId.toString())
        return uri.toString()
    }

    suspend fun getMusicListFromLocal(): List<Music> {
        return musicDao.getMusicListFromLocal()
    }

    suspend fun saveMusicListToLocal(musicList: List<Music>) {
        musicDao.saveMusicListToLocal(musicList)
    }

    suspend fun findMusicByTitle(title: String): Music {
        return musicDao.findMusicByTitle(title)
    }

    suspend fun deleteMusic(musicId: Long) {
        musicDao.deleteMusic(musicId)
    }

    suspend fun getArtistListFromLocal(): List<Artist> {
        return artistDao.getArtistListFromLocal()
    }

    suspend fun saveArtistListToLocal(artistList: List<Artist>) {
        artistDao.saveArtistListToLocal(artistList)
    }

    suspend fun getAlbumListFromLocal(): List<Album> {
        return albumDao.getAlbumListFromLocal()
    }

    suspend fun saveAlbumListToLocal(albumList: List<Album>) {
        albumDao.saveAlbumListToLocal(albumList)
    }
}