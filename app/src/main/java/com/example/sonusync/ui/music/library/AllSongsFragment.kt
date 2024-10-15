package com.example.sonusync.ui.music.library

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonusync.R
import com.example.sonusync.data.adapters.MusicAdapter
import com.example.sonusync.data.model.Music
import com.example.sonusync.ui.music.MusicActivity
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
        val intent =  Intent(activity, MusicActivity::class.java).apply {
            putExtra("MUSIC_TITLE", music.title)
            putExtra("MUSIC_ARTIST", music.artist)
            putExtra("MUSIC_DURATION", music.duration)
            putExtra("MUSIC_ALBUM_COVER", music.albumArtUri)
        }
        startActivity(intent)
    }

    fun updateMusic(musicList: List<Music>) {
        musicAdapter.submitList(musicList)
    }
}