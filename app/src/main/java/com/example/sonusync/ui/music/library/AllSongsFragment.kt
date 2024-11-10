package com.example.sonusync.ui.music.library

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.FrameLayout
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

        val recyclerView: RecyclerView = view.findViewById(R.id.rvAllSongs)
        recyclerView.layoutManager = LinearLayoutManager(context)

        musicAdapter = MusicAdapter(this).apply {
            recyclerView.adapter = this
        }

        musicViewModel.musicList.observe(viewLifecycleOwner) { musicList ->
            musicAdapter.submitGlobalList(musicList)
            updateMusic(musicList)
        }

        if (albumName != null) {
            musicViewModel.filterMusicByAlbum(albumName!!)
            musicViewModel.filteredMusicList.observe(viewLifecycleOwner) { filteredMusicList ->
                updateMusic(filteredMusicList)
            }
        } else if (artistName != null) {
            musicViewModel.filterMusicByArtist(artistName!!)
            musicViewModel.filteredMusicList.observe(viewLifecycleOwner) { filteredMusicList ->
                updateMusic(filteredMusicList)
            }
        }
    }

    override fun onMusicClick(music: Music, globalIndex: Int) {
        musicViewModel.selectMusicAtIndex(globalIndex)

        val flMusic = requireActivity().findViewById<FrameLayout>(R.id.flMusic)
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()

        fragmentTransaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out
        )

        fragmentTransaction.replace(R.id.flMusic, MusicFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
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

    fun updateMusic(musicList: List<Music>) {
        musicAdapter.submitList(musicList)
    }
}