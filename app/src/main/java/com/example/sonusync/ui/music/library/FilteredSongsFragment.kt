package com.example.sonusync.ui.music.library

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonusync.R
import com.example.sonusync.data.adapters.MusicAdapter
import com.example.sonusync.data.model.Music
import com.example.sonusync.service.ServiceStarter
import com.example.sonusync.ui.music.MusicFragment
import com.example.sonusync.viewmodel.MusicViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

    override fun onMusicClick(music: Music) {
        (activity as? ServiceStarter)?.startMusicService()

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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                musicViewModel.filteredMusicFlow.collect { filteredSongs ->
                    musicAdapter.submitList(filteredSongs)
                }
            }
        }
    }
}