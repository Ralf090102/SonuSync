/*
GitHub: https://github.com/Ralf090102/SonuSync
 */

package com.example.sonusync.data.adapters

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.sonusync.R
import com.example.sonusync.data.model.Music

class MusicAdapter(
    private val musicClickListener: MusicClickListener
) : ListAdapter<Music, MusicAdapter.MusicViewHolder>(MusicDiffCallback()) {

    private var globalMusicList: List<Music> = emptyList()

    interface MusicClickListener {
        fun onMusicClick(music: Music, globalIndex: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = getItem(position)
        val globalIndex = globalMusicList.indexOfFirst { it.id == music.id }

        holder.bind(music, musicClickListener, globalIndex)
    }

    override fun getItemCount(): Int = currentList.size

    fun submitFilteredList(filteredList: List<Music>) {
        submitList(filteredList)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitGlobalList(globalList: List<Music>) {
        this.globalMusicList = globalList
        notifyDataSetChanged()
    }

    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvItemTitle)
        private val artistTextView: TextView = itemView.findViewById(R.id.tvItemArtist)
        private val durationTextView: TextView = itemView.findViewById(R.id.tvItemDuration)
        private val albumCoverImageView: ImageView = itemView.findViewById(R.id.ivItemCover)

        fun bind(music: Music, musicClickListener: MusicClickListener, globalIndex: Int) {
            titleTextView.text = music.title
            artistTextView.text = music.artist
            durationTextView.text = formatDuration(music.duration)
            val albumArt = Uri.parse(music.albumArtUri)

            albumCoverImageView.load(albumArt) {
                placeholder(R.drawable.default_album_cover)
                error(R.drawable.default_album_cover)
            }

            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    musicClickListener.onMusicClick(music, globalIndex)
                }
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
}