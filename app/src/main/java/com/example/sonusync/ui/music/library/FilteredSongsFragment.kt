package com.example.sonusync.ui.music.library

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonusync.R
import com.example.sonusync.data.adapters.MusicAdapter
import com.example.sonusync.data.model.Music
import com.example.sonusync.ui.music.MusicFragment
import com.example.sonusync.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilteredSongsFragment : Fragment(R.layout.fragment_music_recycler), MusicAdapter.MusicClickListener {

    private val musicViewModel: MusicViewModel by activityViewModels()

    private lateinit var musicAdapter: MusicAdapter

    private var recyclerViewState: Parcelable? = null
    private var albumName: String? = null
    private var artistName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumName = arguments?.getString("album_name")
        artistName = arguments?.getString("artist_name")

        val recyclerView: RecyclerView = view.findViewById(R.id.rvAllSongs)
        recyclerView.layoutManager = LinearLayoutManager(context)

        musicAdapter = MusicAdapter(this).apply {
            recyclerView.adapter = this
        }

        musicViewModel.musicList.observe(viewLifecycleOwner) { musicList ->
            musicAdapter.submitGlobalList(musicList)
        }

        if (albumName != null || artistName != null) {
            observeFilteredMusic()

            if (albumName != null) {
                musicViewModel.filterMusicByAlbum(albumName!!)
            } else {
                musicViewModel.filterMusicByArtist(artistName!!)
            }
        } else {
            Toast.makeText(context, "No album or artist name provided", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onMusicClick(music: Music, globalIndex: Int) {
        musicViewModel.selectMusicAtIndex(globalIndex)

        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.flMusic, MusicFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onPause() {
        super.onPause()
        recyclerViewState = view?.findViewById<RecyclerView>(R.id.rvAllSongs)
            ?.layoutManager?.onSaveInstanceState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        musicViewModel.clearFilteredMusicList()
    }

    private fun observeFilteredMusic() {
        musicViewModel.filteredMusicList.observe(viewLifecycleOwner) { filteredSongs ->
            updateMusic(filteredSongs)
        }
    }

    fun updateMusic(musicList: List<Music>) {
        musicAdapter.submitList(musicList)
    }
}