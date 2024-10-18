package com.example.sonusync.ui.music

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
class MusicFragment : Fragment(R.layout.fragment_music_player) {

    private val musicViewModel: MusicViewModel by activityViewModels()

    private var currentToast: Toast? = null
    private lateinit var exoPlayer: ExoPlayer
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

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (exoPlayer.isPlaying) {
                val currentPos = exoPlayer.currentPosition
                tvCurrentTime.text = formatDuration(currentPos)
                sbPlayback.progress = currentPos.toInt()
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)

        musicViewModel.isShuffleEnabled.observe(viewLifecycleOwner) { isShuffled ->
            ibShuffle.setImageResource(if (isShuffled) R.drawable.ic_music_shuffle_on else R.drawable.ic_music_shuffle_off)
        }

        musicViewModel.musicList.observe(viewLifecycleOwner) { musicList ->
            musicViewModel.currentMusicIndex.observe(viewLifecycleOwner) { index ->
                if (index != null && index < musicList.size) {
                    val music = musicList[index]
                    setupExoPlayer(music.uri)
                    setMusicFragmentUI(music.title, music.artist, formatDuration(music.duration), music.albumArtUri)
                    setPlayerListeners(music.duration)
                }
            }
        }

        musicViewModel.repeatMode.observe(viewLifecycleOwner) { mode ->
            ibRepeat.setImageResource(
                when (mode) {
                    RepeatMode.OFF -> R.drawable.ic_music_repeat_disabled
                    RepeatMode.ALL -> R.drawable.ic_music_repeat_enabled
                    RepeatMode.ONE -> R.drawable.ic_music_repeat_one
                    null -> TODO()
                }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateRunnable)
        exoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
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
        val albumArt = Uri.parse(albumArtUri)

        sivAlbumCover.load(albumArt) {
            placeholder(R.drawable.default_album_cover)
            error(R.drawable.default_album_cover)
        }
    }

    private fun setupExoPlayer(musicUri: String?) {
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        val mediaItem = MediaItem.fromUri(Uri.parse(musicUri))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    @OptIn(UnstableApi::class)
    private fun setPlayerListeners(duration: Long?) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                fabMusic.setImageResource(if (isPlaying) R.drawable.ic_music_pause else R.drawable.ic_music_play)
            }
        })

        fabMusic.setOnClickListener {
            if (exoPlayer.isPlaying)
                exoPlayer.pause()
            else
                exoPlayer.play()
        }

        ibNext.setOnClickListener {
            exoPlayer.pause()
            musicViewModel.playNext()
        }

        ibPrev.setOnClickListener {
            exoPlayer.pause()
            musicViewModel.playPrevious()
        }

        ibShuffle.setOnClickListener {
            musicViewModel.toggleShuffle()

            currentToast?.cancel()
            currentToast = Toast.makeText(context,
                if (musicViewModel.isShuffleEnabled.value == true) "Shuffle On"
                else "Shuffle Off",
                Toast.LENGTH_SHORT).apply { show() }
        }

        ibRepeat.setOnClickListener {
            musicViewModel.toggleRepeat()

            currentToast?.cancel()
            currentToast = Toast.makeText(context,
                "Repeat Mode: ${musicViewModel.repeatMode.value}",
                Toast.LENGTH_SHORT).apply { show() }        }

        sbPlayback.max = duration?.toInt() ?: 0

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    tvTotalTime.text = formatDuration(exoPlayer.duration)
                    sbPlayback.max = exoPlayer.duration.toInt()
                    handler.post(updateRunnable)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    when (musicViewModel.repeatMode.value) {
                        RepeatMode.ALL -> musicViewModel.playNext()
                        RepeatMode.ONE -> exoPlayer.seekTo(0)
                        RepeatMode.OFF -> {
                            exoPlayer.seekTo(0)
                            exoPlayer.pause()
                        }
                        null -> TODO()
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onPositionDiscontinuity(reason: Int) {
                sbPlayback.progress = exoPlayer.currentPosition.toInt()
                tvCurrentTime.text = formatDuration(exoPlayer.currentPosition)
            }
        })

        sbPlayback.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer.seekTo(progress.toLong())
                    tvCurrentTime.text = formatDuration(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}