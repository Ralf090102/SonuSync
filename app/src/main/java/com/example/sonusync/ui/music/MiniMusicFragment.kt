package com.example.sonusync.ui.music

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.media3.exoplayer.ExoPlayer
import coil.load
import com.example.sonusync.R

class MiniMusicFragment : Fragment(R.layout.fragment_mini_music_player) {

    private lateinit var ivMiniMusicCover: ImageView
    private lateinit var tvMiniMusicTitle: TextView
    private lateinit var tvMiniMusicArtist: TextView
    private lateinit var ibMiniPlayPause: ImageButton

    private var exoPlayer: ExoPlayer? = null

    var onFragmentClick: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivMiniMusicCover = view.findViewById(R.id.ivMiniMusicCover)
        tvMiniMusicTitle = view.findViewById(R.id.tvMiniMusicTitle)
        tvMiniMusicArtist = view.findViewById(R.id.tvMiniMusicArtist)
        ibMiniPlayPause = view.findViewById(R.id.ibMiniPlayPause)

        arguments?.let {
            updateMiniMusicUI(
                it.getString("title") ?: "",
                it.getString("artist") ?: "",
                it.getString("cover")
            )
        }

        ibMiniPlayPause.setOnClickListener { togglePlayPause() }

        view.setOnClickListener {
            onFragmentClick?.invoke()
        }
    }

    private fun updateMiniMusicUI(title: String, artist: String, cover: String?) {
        val albumCover = Uri.parse(cover)

        ivMiniMusicCover.load(albumCover) {
            placeholder(R.drawable.default_album_cover)
            error(R.drawable.default_album_cover)
        }

        tvMiniMusicTitle.text = title
        tvMiniMusicArtist.text = artist
    }
    private fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
            updatePlayPauseIcon()
        }
    }


    fun updatePlayPauseIcon() {
        ibMiniPlayPause.setImageResource(
            if (exoPlayer?.isPlaying == true) R.drawable.ic_music_pause_mini else R.drawable.ic_music_play_mini
        )
    }

    fun setMiniExoPlayer(exoPlayer: ExoPlayer) {
        this.exoPlayer = exoPlayer
    }
}