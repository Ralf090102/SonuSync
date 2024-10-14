package com.example.sonusync.data.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sonusync.R
import com.example.sonusync.data.model.Music
import com.example.sonusync.data.repository.MusicRepository
import javax.inject.Inject


class MusicAdapter @Inject constructor(
    private var musics: List<Music> = listOf()
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val song = musics[position]
        holder.bind(song)
    }

    override fun getItemCount() = musics.size

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(music: Music) {
            // Bind song data to UI components
        }
    }

    fun setMusicList(musicList: List<Music>) {
        this.musics = musicList
        notifyDataSetChanged()
    }
}