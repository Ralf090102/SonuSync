package com.example.sonusync.ui.music.library

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonusync.R
import com.example.sonusync.data.adapters.MusicAdapter
import com.example.sonusync.data.model.Music
import com.example.sonusync.ui.music.MusicFragment
import com.example.sonusync.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllSongsFragment : Fragment(R.layout.fragment_all_songs), MusicAdapter.MusicClickListener {

    private val musicViewModel: MusicViewModel by activityViewModels()

    private lateinit var musicAdapter: MusicAdapter
    private var recyclerViewState: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.rvAllSongs)
        musicAdapter = MusicAdapter(this).apply {
            recyclerView.adapter = this
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        observeViewModel()
    }

    override fun onMusicClick(music: Music, position: Int) {

        musicViewModel.selectMusicAtIndex(position)

        parentFragmentManager.beginTransaction()
            .replace(R.id.flMusic, MusicFragment())
            .commit()
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<RecyclerView>(R.id.rvAllSongs)?.layoutManager?.let { layoutManager ->
            recyclerViewState = layoutManager.onSaveInstanceState()
        }
    }

    fun updateMusic(musicList: List<Music>) {
        musicAdapter.submitList(musicList)
    }

    private fun observeViewModel() {
        musicViewModel.musicList.observe(viewLifecycleOwner, Observer { musicList ->
            updateMusic(musicList)
        })
    }
}