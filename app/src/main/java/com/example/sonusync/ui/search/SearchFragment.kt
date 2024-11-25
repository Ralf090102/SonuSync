package com.example.sonusync.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
class SearchFragment : Fragment(R.layout.fragment_search), MusicAdapter.MusicClickListener {

    private val musicViewModel: MusicViewModel by activityViewModels()
    private lateinit var musicAdapter: MusicAdapter

    private var recyclerViewState: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextSearch = view.findViewById<EditText>(R.id.etSearch)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMusic)
        recyclerView.layoutManager = LinearLayoutManager(context)

        musicAdapter = MusicAdapter(this).apply {
            recyclerView.adapter = this
        }

        observeFilteredMusic()

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                musicViewModel.setSearchQuery(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<RecyclerView>(R.id.rvAllSongs)?.layoutManager?.let { layoutManager ->
            recyclerViewState = layoutManager.onSaveInstanceState()
        }
    }

    @SuppressLint("ServiceCast")
    override fun onMusicClick(music: Music) {
        (activity as? ServiceStarter)?.startMusicService()
        musicViewModel.selectAndPlayMusic(music)

        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = requireActivity().currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()

        fragmentTransaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out
        )

        fragmentTransaction.replace(R.id.flMusic, MusicFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun observeFilteredMusic() {
        musicViewModel.queryMusicList.observe(viewLifecycleOwner) { filteredList ->
            musicAdapter.submitList(filteredList)
        }
    }
}