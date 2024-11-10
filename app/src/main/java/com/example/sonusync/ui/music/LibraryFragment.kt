package com.example.sonusync.ui.music

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sonusync.R
import com.example.sonusync.data.model.Album
import com.example.sonusync.data.model.Artist
import com.example.sonusync.data.model.Music
import com.example.sonusync.data.model.Playlist
import com.example.sonusync.ui.music.library.AlbumsFragment
import com.example.sonusync.ui.music.library.AllSongsFragment
import com.example.sonusync.ui.music.library.ArtistsFragment
import com.example.sonusync.viewmodel.EnsembleViewModel
import com.example.sonusync.viewmodel.MusicViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment(R.layout.fragment_library){
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val musicViewModel: MusicViewModel by activityViewModels()
    private val ensembleViewModel: EnsembleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

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

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                handleTabSelection()
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                handleTabSelection()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            private fun handleTabSelection() {
                childFragmentManager.findFragmentById(R.id.flContentContainer)?.let { fragment ->
                    childFragmentManager.beginTransaction().remove(fragment).commit()

                    viewPager.animate().alpha(0f).setDuration(0).withEndAction {
                        viewPager.alpha = 0f
                        viewPager.animate().alpha(1f).setDuration(300).start()
                    }.start()
                }
            }
        })

        musicViewModel.musicList.observe(viewLifecycleOwner) { musicList -> notifyMusicFragments(musicList) }
        ensembleViewModel.artists.observe(viewLifecycleOwner) { artists -> notifyArtistFragments(artists) }
        ensembleViewModel.albums.observe(viewLifecycleOwner) { albums -> notifyAlbumFragments(albums) }
        ensembleViewModel.playlists.observe(viewLifecycleOwner) { playlists -> notifyPlaylistFragments(playlists) }
    }

    private fun notifyMusicFragments(musicList: List<Music>) {
        val currentFragment = childFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
        if (currentFragment is AllSongsFragment) {
            currentFragment.updateMusic(musicList)
        }
    }

    private fun notifyArtistFragments(artistList: List<Artist>) {
        val currentFragment = childFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
        if (currentFragment is ArtistsFragment) {
            currentFragment.updateArtists(artistList)
        }
    }

    private fun notifyAlbumFragments(albumList: List<Album>) {
        val currentFragment = childFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
        if (currentFragment is AlbumsFragment) {
            currentFragment.updateAlbums(albumList)
        }
    }

    private fun notifyPlaylistFragments(playlistList: List<Playlist>) {
        val currentFragment = childFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
        if (currentFragment is AlbumsFragment) {
            //currentFragment.updatePlaylists(playlistList)
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