package com.example.sonusync.ui.music

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.sonusync.R
import com.example.sonusync.viewmodel.MusicViewModel

class MiniMusicFragment : Fragment(R.layout.fragment_mini_music_player) {

    private val musicViewModel: MusicViewModel by activityViewModels()

    private lateinit var ivMiniMusicCover: ImageView
    private lateinit var tvMiniMusicTitle: TextView
    private lateinit var tvMiniMusicArtist: TextView
    private lateinit var ibMiniPlayPause: ImageButton

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

        ibMiniPlayPause.setOnClickListener {
            musicViewModel.onUiEvents(MusicViewModel.UIEvents.PlayPause)
        }

        view.setOnClickListener {
            onFragmentClick?.invoke()
        }

        observeMusicState()
    }

    override fun onResume() {
        super.onResume()
        musicViewModel.getSelectedIndex()?.let { index ->
            val music = musicViewModel.musicFlow.value.getOrNull(index)
            music?.let {
                updateMiniMusicUI(it.title, it.artist, it.albumArtUri)
            }
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

    private fun observeMusicState() {
        musicViewModel.ldIsPlaying.observe(viewLifecycleOwner) { isPlaying ->
            ibMiniPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_music_pause_mini else R.drawable.ic_music_play_mini
            )
        }
    }
}