package com.example.sonusync.ui.music

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.sonusync.R

class MusicFragment : Fragment(R.layout.fragment_music) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("MUSIC_TITLE")
        val artist = arguments?.getString("MUSIC_ARTIST")
        val duration = arguments?.getLong("MUSIC_DURATION")
        val albumCoverUri = arguments?.getString("MUSIC_ALBUM_COVER")

    }
}