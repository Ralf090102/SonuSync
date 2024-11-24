package com.example.sonusync.ui.music

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import coil.load
import com.example.sonusync.R
import com.example.sonusync.viewmodel.MusicViewModel
import com.example.sonusync.data.enums.RepeatMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicFragment : Fragment(R.layout.fragment_music_player){

    private lateinit var gestureDetector: GestureDetector

    private val musicViewModel: MusicViewModel by activityViewModels()

    private var currentToast: Toast? = null
    private lateinit var tvMusicTitle: TextView
    private lateinit var tvMusicArtist: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var sivAlbumCover: ShapeableImageView
    private lateinit var fabMusic: FloatingActionButton
    private lateinit var sbPlayback: SeekBar
    private lateinit var ibPrev: ImageButton
    private lateinit var ibNext: ImageButton
    private lateinit var ibShuffle: ImageButton
    private lateinit var ibRepeat: ImageButton

    private lateinit var miniMusicFragment: MiniMusicFragment

    private var currentAlbumCover: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            TODO()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (velocityY > 0) {
                    hideMusicFragment()
                    return true
                }
                return false
            }
        })

        initializeViews(view)

        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateRunnable)

        TODO()
    }

    override fun onDestroy() {
        super.onDestroy()

        TODO()
    }

    private fun initializeViews(view: View){
        tvMusicTitle = view.findViewById(R.id.tvMusicTitle)
        tvMusicArtist = view.findViewById(R.id.tvMusicArtist)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        sivAlbumCover = view.findViewById(R.id.sivAlbumCover)
        fabMusic = view.findViewById(R.id.fabMusic)
        sbPlayback = view.findViewById(R.id.sbPlayback)
        ibPrev = view.findViewById(R.id.ibPrev)
        ibNext = view.findViewById(R.id.ibNext)
        ibShuffle = view.findViewById(R.id.ibShuffle)
        ibRepeat = view.findViewById(R.id.ibRepeat)
    }

    private fun setMusicFragmentUI(title: String?, artist: String?, duration: String?, albumArtUri: String?){
        tvMusicTitle.text = title
        tvMusicArtist.text = artist
        tvTotalTime.text = duration
        currentAlbumCover = albumArtUri
        val albumArt = Uri.parse(albumArtUri)

        sivAlbumCover.load(albumArt) {
            placeholder(R.drawable.default_album_cover)
            error(R.drawable.default_album_cover)
        }

        showMiniMusicFragment()
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun hideMusicFragment() {
        view?.let { fragmentView ->
            val animation = ObjectAnimator.ofFloat(fragmentView, "alpha", 1f, 0f)
            animation.duration = 300

            animation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    fragmentView.visibility = View.GONE
                }
            })

            animation.start()
        }
    }

    private fun showMiniMusicFragment() {
        miniMusicFragment = MiniMusicFragment().apply {
            arguments = Bundle().apply {
                putString("title", tvMusicTitle.text.toString())
                putString("artist", tvMusicArtist.text.toString())
                putString("cover", currentAlbumCover)
            }

            onFragmentClick = {
                showMusicFragment()
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flMiniMusic, miniMusicFragment, "Library")
            .commit()
    }

    private fun showMusicFragment() {
        view?.let { fragmentView ->
            fragmentView.visibility = View.VISIBLE
            val animation = ObjectAnimator.ofFloat(fragmentView, "alpha", 0f, 1f)
            animation.duration = 300
            animation.start()
        }
    }
}