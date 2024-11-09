package com.example.sonusync.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.sonusync.R
import com.example.sonusync.data.adapters.MusicAdapter
import com.example.sonusync.data.model.Music
import com.example.sonusync.ui.music.MusicFragment
import com.example.sonusync.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search), MusicAdapter.MusicClickListener {

    private val searchViewModel: SearchViewModel by activityViewModels()
    private lateinit var musicAdapter: MusicAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextSearch = view.findViewById<EditText>(R.id.etSearch)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMusic)

        val musicAll = searchViewModel.musicList.value ?: emptyList()
        val filteredMusic = searchViewModel.filteredMusicList.value ?: emptyList()

        musicAdapter = MusicAdapter(filteredMusic, musicAll, this).apply{
            recyclerView.adapter = this
        }

        searchViewModel.filteredMusicList.observe(viewLifecycleOwner) { filteredList ->
            musicAdapter.submitList(filteredList)
        }

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchViewModel.setSearchQuery(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onMusicClick(music: Music, globalIndex: Int) {
        searchViewModel.selectMusicAtIndex(globalIndex)

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.flMusic, MusicFragment())
            .addToBackStack(null)
            .commit()
    }
}