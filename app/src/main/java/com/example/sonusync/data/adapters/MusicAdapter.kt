package com.example.sonusync.data.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sonusync.R
import com.example.sonusync.data.model.Music


class MusicAdapter(
    private val musicClickListener: MusicClickListener
) : ListAdapter<Music, MusicAdapter.MusicViewHolder>(MusicDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_music, parent, false)
            return MusicViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
            val music = getItem(position)
            holder.bind(music, musicClickListener)
        }

    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val artistTextView: TextView = itemView.findViewById(R.id.tvItemArtist)
        private val durationTextView: TextView = itemView.findViewById(R.id.tvItemDuration)
        private val albumCoverImageView: ImageView = itemView.findViewById(R.id.ivItemCover)

        fun bind(music: Music, musicClickListener: MusicClickListener) {
            titleTextView.text = music.title
            artistTextView.text = music.artist

            durationTextView.text = formatDuration(music.duration)

            Glide.with(itemView.context)
                .load(music.albumArtUri)
                .placeholder(R.drawable.default_album_cover)
                .into(albumCoverImageView)

            itemView.setOnClickListener {
                musicClickListener.onMusicClick(music)
            }
        }

        @SuppressLint("DefaultLocale")
        private fun formatDuration(duration: Long): String {
            val minutes = (duration / 1000) / 60
            val seconds = (duration / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    class MusicDiffCallback : DiffUtil.ItemCallback<Music>() {
        override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem == newItem
        }
    }

    interface MusicClickListener {
        fun onMusicClick(music: Music)
    }
}