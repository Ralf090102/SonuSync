package com.example.sonusync.ui.music

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.sonusync.R
import com.example.sonusync.data.enums.RepeatMode
import com.example.sonusync.viewmodel.MusicViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

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
            val currentPosition = musicViewModel.getCurrentPlaybackPosition()
            val duration = musicViewModel.getCurrentMediaDuration()

            duration.let {
                if (it > 0) {
                    val progress = (currentPosition * 100 / it).toInt()

                    sbPlayback.progress = progress
                    tvCurrentTime.text = musicViewModel.formatDuration(currentPosition)
                }
            }

            handler.postDelayed(this, 1000)
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

        musicViewModel.selectedIndex.observe(viewLifecycleOwner) { index ->
            val music = musicViewModel.musicList.getOrNull(index)
            if (music != null) {
                setMusicFragmentUI(music.title, music.artist, musicViewModel.formatDuration(music.duration), music.albumArtUri)
            }
        }

        sbPlayback.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val newPosition = (progress / 100.0) * musicViewModel.duration
                    tvCurrentTime.text = musicViewModel.formatDuration(newPosition.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    val newPosition = (it.progress / 100.0) * musicViewModel.duration
                    musicViewModel.onUiEvents(MusicViewModel.UIEvents.SeekTo(newPosition.toFloat()))

                    handler.post(updateRunnable)
                }
            }
        })

        musicViewModel.ldIsPlaying.observe(viewLifecycleOwner) { isPlaying ->
            fabMusic.setImageResource(
                if (isPlaying) R.drawable.ic_music_pause_mini else R.drawable.ic_music_play_mini
            )

            if (isPlaying) {
                handler.post(updateRunnable)
            } else {
                handler.removeCallbacks(updateRunnable)
            }
        }

        fabMusic.setOnClickListener {
            musicViewModel.onUiEvents(MusicViewModel.UIEvents.PlayPause)
        }

        ibPrev.setOnClickListener {
            musicViewModel.onUiEvents(MusicViewModel.UIEvents.SeekToPrevious)
        }

        ibNext.setOnClickListener {
            musicViewModel.onUiEvents(MusicViewModel.UIEvents.SeekToNext)
        }

        ibShuffle.setOnClickListener {
            musicViewModel.onUiEvents(MusicViewModel.UIEvents.Shuffle)
            val isShuffled = musicViewModel.getIsShuffled()

            ibShuffle.setImageResource(
                if (isShuffled) R.drawable.ic_music_shuffle_on else R.drawable.ic_music_shuffle_off
            )

            currentToast?.cancel()
            currentToast = Toast.makeText(context,
                if (isShuffled) "Shuffle On"
                else "Shuffle Off",
                Toast.LENGTH_SHORT).apply { show() }
        }

        ibRepeat.setOnClickListener {
            musicViewModel.onUiEvents(MusicViewModel.UIEvents.Repeat)
            val repeatMode = musicViewModel.fetchRepeatMode()

            val repeatDrawable = when (repeatMode) {
                RepeatMode.OFF -> R.drawable.ic_music_repeat_disabled
                RepeatMode.ONE -> R.drawable.ic_music_repeat_one
                RepeatMode.ALL -> R.drawable.ic_music_repeat_enabled
            }

            ibRepeat.setImageResource(repeatDrawable)

            currentToast?.cancel()
            currentToast = Toast.makeText(
                context,
                "Repeat Mode: $repeatMode",
                Toast.LENGTH_SHORT
            ).apply { show() }
        }

        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        musicViewModel.releasePlayer()
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

    private fun setMusicFragmentUI(title: String?, artist: String?, duration: String?, albumArtUri: String?) {
        val isDefaultAlbumCover = (currentAlbumCover == null || currentAlbumCover == albumArtUri)
        val isDefaultTitle = tvMusicTitle.text.isEmpty() || tvMusicTitle.text == "Unknown Title"
        val isDefaultArtist = tvMusicArtist.text.isEmpty() || tvMusicArtist.text == "Unknown Artist"

        if (!isDefaultAlbumCover) {
            sivAlbumCover.animate().alpha(0f).setDuration(300).withEndAction {
                updateAlbumCover(albumArtUri)
                sivAlbumCover.animate().alpha(1f).setDuration(300).start()
            }.start()
        } else {
            updateAlbumCover(albumArtUri)
            sivAlbumCover.alpha = 1f
        }

        if (!isDefaultTitle) {
            tvMusicTitle.animate().alpha(0f).setDuration(300).withEndAction {
                tvMusicTitle.text = title
                tvMusicTitle.animate().alpha(1f).setDuration(300).start()
            }.start()
        } else {
            tvMusicTitle.text = title
            tvMusicTitle.alpha = 1f
        }

        if (!isDefaultArtist) {
            tvMusicArtist.animate().alpha(0f).setDuration(300).withEndAction {
                tvMusicArtist.text = artist
                tvMusicArtist.animate().alpha(1f).setDuration(300).start()
            }.start()
        } else {
            tvMusicArtist.text = artist
            tvMusicArtist.alpha = 1f
        }

        tvTotalTime.text = duration
        currentAlbumCover = albumArtUri

        val isShuffled = musicViewModel.getIsShuffled()
        val repeatMode = musicViewModel.fetchRepeatMode()
        val repeatDrawable = when (repeatMode) {
            RepeatMode.OFF -> R.drawable.ic_music_repeat_disabled
            RepeatMode.ONE -> R.drawable.ic_music_repeat_one
            RepeatMode.ALL -> R.drawable.ic_music_repeat_enabled
        }

        ibRepeat.setImageResource(repeatDrawable)
        ibShuffle.setImageResource(if (isShuffled) R.drawable.ic_music_shuffle_on else R.drawable.ic_music_shuffle_off)

        showMiniMusicFragment()
    }

    private fun updateAlbumCover(albumArtUri: String?) {
        val albumArt = Uri.parse(albumArtUri)
        sivAlbumCover.load(albumArt) {
            placeholder(R.drawable.default_album_cover)
            error(R.drawable.default_album_cover)
        }
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