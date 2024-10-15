package com.example.sonusync.ui.music

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import coil.load
import com.example.sonusync.R
import com.google.android.material.imageview.ShapeableImageView

class MusicFragment : Fragment(R.layout.fragment_music) {

    private lateinit var tvMusicTitle: TextView
    private lateinit var tvMusicArtist: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var sivAlbumCover: ShapeableImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("MUSIC_TITLE")
        val artist = arguments?.getString("MUSIC_ARTIST")
        val duration = arguments?.getLong("MUSIC_DURATION")
        val albumCoverUri = arguments?.getString("MUSIC_ALBUM_COVER")

        tvMusicTitle = view.findViewById(R.id.tvMusicTitle)
        tvMusicArtist = view.findViewById(R.id.tvMusicArtist)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        sivAlbumCover = view.findViewById(R.id.sivAlbumCover)


        setMusicFragmentUI(title, artist, duration?.let { formatDuration(it) }, albumCoverUri)
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun setMusicFragmentUI(title: String?, artist: String?, duration: String?, albumArtUri: String?){
        tvMusicTitle.text = title
        tvMusicArtist.text = artist
        tvTotalTime.text = duration
        val albumArt = Uri.parse(albumArtUri)

        sivAlbumCover.load(albumArt) {
            placeholder(R.drawable.default_album_cover)
            error(R.drawable.default_album_cover)
        }
    }
}