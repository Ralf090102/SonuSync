package com.example.sonusync.data.adapters

import android.net.Uri
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
import com.example.sonusync.data.model.Ensemble

class EnsembleAdapter(
    private val ensembleClickListener: EnsembleClickListener
) : ListAdapter<Ensemble, EnsembleAdapter.EnsembleViewHolder>(DiffCallback()) {

    interface EnsembleClickListener {
        fun onEnsembleClick(ensemble: Ensemble, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnsembleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ensemble, parent, false)
        return EnsembleViewHolder(view)
    }

    override fun onBindViewHolder(holder: EnsembleViewHolder, position: Int) {
        val ensemble = getItem(position)
        holder.bind(ensemble, ensembleClickListener)
    }

    class EnsembleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(ensemble: Ensemble, ensembleClickListener: EnsembleClickListener) {
            itemView.findViewById<TextView>(R.id.tvEnsembleName).text = ensemble.name

            val ivEnsembleCover = itemView.findViewById<ImageView>(R.id.ivEnsembleCover)
            val albumArt = Uri.parse(ensemble.artUri)

            ivEnsembleCover.load(albumArt){
                placeholder(R.drawable.default_album_cover)
                error(R.drawable.default_album_cover)
            }

            itemView.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    ensembleClickListener.onEnsembleClick(ensemble, currentPosition)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Ensemble>() {
        override fun areItemsTheSame(oldItem: Ensemble, newItem: Ensemble): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ensemble, newItem: Ensemble): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name
        }
    }
}