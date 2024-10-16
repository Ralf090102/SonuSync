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
class AllSongsFragment : Fragment(R.layout.fragment_music_recycler), MusicAdapter.MusicClickListener {

    private val musicViewModel: MusicViewModel by activityViewModels()

    private lateinit var musicAdapter: MusicAdapter
    private var recyclerViewState: Parcelable? = null
    private var albumName: String? = null
    private var artistName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumName = arguments?.getString("album_name")
        artistName = arguments?.getString("artist_name")

        val musicAll = musicViewModel.musicList.value ?: emptyList()

        val recyclerView: RecyclerView = view.findViewById(R.id.rvAllSongs)
        recyclerView.layoutManager = LinearLayoutManager(context)

        if (albumName != null) {
            musicViewModel.filterMusicByAlbum(albumName!!)
            musicViewModel.filteredMusicList.observe(viewLifecycleOwner) { filteredMusicList ->
                musicAdapter = MusicAdapter(filteredMusicList, musicAll, this).apply {
                    recyclerView.adapter = this
                }
            }
        } else if (artistName != null) {
            musicViewModel.filterMusicByArtist(artistName!!)
            musicViewModel.filteredMusicList.observe(viewLifecycleOwner) { filteredMusicList ->
                musicAdapter = MusicAdapter(filteredMusicList, musicAll, this).apply {
                    recyclerView.adapter = this
                }
            }
        } else {
            musicViewModel.musicList.observe(viewLifecycleOwner) { musicList ->
                musicAdapter = MusicAdapter(musicList, musicList, this).apply {
                    recyclerView.adapter = this
                }
            }
        }
    }

    override fun onMusicClick(music: Music, globalIndex: Int) {

        musicViewModel.selectMusicAtIndex(globalIndex)

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

    override fun onDestroyView() {
        super.onDestroyView()
        musicViewModel.clearFilteredMusicList()
    }

    fun updateMusic(musicList: List<Music>) {
        musicAdapter.submitList(musicList)
    }

    private fun observeViewModel() {
        musicViewModel.musicList.observe(viewLifecycleOwner, Observer { musicList ->
            updateMusic(musicList)
        })
    }

    private fun observeFilteredMusic() {
        musicViewModel.filteredMusicList.observe(viewLifecycleOwner) { filteredSongs ->
            updateMusic(filteredSongs)
        }
    }
}