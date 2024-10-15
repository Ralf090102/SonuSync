package com.example.sonusync.ui.music.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonusync.R
import com.example.sonusync.data.adapters.MusicAdapter
import com.example.sonusync.data.model.Music
import com.example.sonusync.ui.music.MusicFragment
import com.example.sonusync.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AllSongsFragment : Fragment(R.layout.fragment_all_songs), MusicAdapter.MusicClickListener {

    @Inject
    lateinit var musicViewModel: MusicViewModel

    private lateinit var musicAdapter: MusicAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.rvAllSongs)
        musicAdapter = MusicAdapter(this)
        recyclerView.adapter = musicAdapter

        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onMusicClick(music: Music) {
        val musicFragment = MusicFragment().apply {
            arguments = Bundle().apply {
                putString("MUSIC_TITLE", music.title)
                putString("MUSIC_ARTIST", music.artist)
                putLong("MUSIC_DURATION", music.duration)
                putString("MUSIC_ALBUM_COVER", music.albumArtUri)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.flMusic, musicFragment)
            .commit()
    }

    fun updateMusic(musicList: List<Music>) {
        musicAdapter.submitList(musicList)
    }
}