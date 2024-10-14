package com.example.sonusync.ui.music

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sonusync.R
import com.example.sonusync.data.model.Music
import com.example.sonusync.ui.music.library.AlbumsFragment
import com.example.sonusync.ui.music.library.AllSongsFragment
import com.example.sonusync.ui.music.library.ArtistsFragment
import com.example.sonusync.viewmodel.MusicViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LibraryFragment : Fragment(R.layout.fragment_library){
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var musicViewModel: MusicViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        musicViewModel = ViewModelProvider(this)[MusicViewModel::class.java]

        viewPager = view.findViewById(R.id.view_pager)
        tabLayout = view.findViewById(R.id.tab_layout)

        viewPager.adapter = LibraryPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "All Songs"
                1 -> "Artists"
                2 -> "Albums"
                else -> null
            }
        }.attach()

        musicViewModel.musicList.observe(viewLifecycleOwner) { musicList ->
            notifyChildFragments(musicList)
        }
    }

    private fun notifyChildFragments(musicList: List<Music>) {
        val currentFragment = childFragmentManager.findFragmentById(R.id.view_pager)
        // Update the adapter of the current fragment if it is an instance of AllSongsFragment
        if (currentFragment is AllSongsFragment) {
            //currentFragment.updateSongs(musicList)
        } else if (currentFragment is ArtistsFragment) {
            //currentFragment.updateArtists(musicList) // Assuming you have this method in ArtistsFragment
        } else if (currentFragment is AlbumsFragment) {
           // currentFragment.updateAlbums(musicList) // Assuming you have this method in AlbumsFragment
        }
    }

    private inner class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment){
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AllSongsFragment()
                1 -> ArtistsFragment()
                2 -> AlbumsFragment()
                else -> throw IllegalStateException("Unexpected Position -> $position <-")
            }
        }
    }
}