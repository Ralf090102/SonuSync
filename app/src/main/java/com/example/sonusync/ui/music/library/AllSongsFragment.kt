package com.example.sonusync.ui.music.library

import android.os.Bundle
import android.os.Parcelable
import android.view.View
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
class AllSongsFragment : Fragment(R.layout.fragment_music_recycler), MusicAdapter.MusicClickListener {

    private val musicViewModel: MusicViewModel by activityViewModels()

    private lateinit var musicAdapter: MusicAdapter

    private var recyclerViewState: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.rvAllSongs)
        recyclerView.layoutManager = LinearLayoutManager(context)

        musicAdapter = MusicAdapter(this).apply {
            recyclerView.adapter = this
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                musicViewModel.musicFlow.collect { musicList ->
                    musicAdapter.submitList(musicList)
                }
            }
        }
    }

    override fun onMusicClick(music: Music) {
        (activity as? ServiceStarter)?.startMusicService()
        musicViewModel.selectAndPlayMusic(music)
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

    fun updateMusic(musicList: List<Music>) {
        musicAdapter.submitList(musicList)
    }
}