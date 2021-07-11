package com.goelapplications.igsaver.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goelapplications.igsaver.R
import com.goelapplications.igsaver.models.MediaModel

class SideCarAdapter(private val context: Context, private val modelList: ArrayList<MediaModel>,
                     private val clickListener: ClickListeners)
    : RecyclerView.Adapter<SideCarAdapter.ThumbnailHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.thumbnail, parent, false)
        return ThumbnailHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailHolder, position: Int) {
        val model = modelList[position]
        Glide.with(context).load(model.thumbnailUrl).centerCrop().into(holder.thumbnail)
        holder.previewButton.setOnClickListener { clickListener.preview(model) }
        holder.downloadButton.setOnClickListener { clickListener.download(model) }
    }

    override fun getItemCount(): Int = modelList.size

    inner class ThumbnailHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        val previewButton: ImageButton = itemView.findViewById(R.id.preview_button)
        val downloadButton: ImageButton = itemView.findViewById(R.id.download_button)
    }

    interface ClickListeners {
        fun preview(model: MediaModel)
        fun download(model: MediaModel)
    }
}