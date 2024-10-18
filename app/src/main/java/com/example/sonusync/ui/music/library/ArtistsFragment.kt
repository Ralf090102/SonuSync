package com.example.sonusync.ui.music.library

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonusync.R
import com.example.sonusync.data.adapters.EnsembleAdapter
import com.example.sonusync.data.model.Artist
import com.example.sonusync.data.model.Ensemble
import com.example.sonusync.viewmodel.EnsembleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistsFragment : Fragment(R.layout.fragment_ensemble_grid), EnsembleAdapter.EnsembleClickListener {

    private val ensembleViewModel: EnsembleViewModel by activityViewModels()

    private lateinit var ensembleAdapter: EnsembleAdapter

    private var recyclerViewState: Parcelable? = null

    override fun onPause() {
        super.onPause()
        view?.findViewById<RecyclerView>(R.id.rvEnsemble)?.layoutManager?.let { layoutManager ->
            recyclerViewState = layoutManager.onSaveInstanceState()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.rvEnsemble)
        ensembleAdapter = EnsembleAdapter(this).apply {
            recyclerView.adapter = this
        }

        recyclerView.layoutManager = GridLayoutManager(context, 3)
        observeViewModel()
    }

    override fun onEnsembleClick(ensemble: Ensemble, position: Int) {
        val bundle = Bundle().apply {
            putString("artist_name", ensemble.name)
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.flMusic, AllSongsFragment::class.java, bundle)
            .addToBackStack(null)
            .commit()
    }

    private fun observeViewModel() {
        ensembleViewModel.artists.observe(viewLifecycleOwner, Observer { artistList ->
            updateArtists(artistList)
        })
    }

    fun updateArtists(artistList: List<Artist>) {
        Log.d ("TestLog", "Artist List Size: ${artistList.size}")
        ensembleAdapter.submitList(artistList)
    }
}