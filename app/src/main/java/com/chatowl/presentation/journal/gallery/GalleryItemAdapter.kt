package com.chatowl.presentation.journal.gallery

import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.journal.gallery.GalleryItem
import com.chatowl.presentation.GlideApp
import kotlinx.android.synthetic.main.row_gallery.view.*

class GalleryItemAdapter(private val itemClick: (galleryItem: GalleryItem) -> Unit) :
    ListAdapter<GalleryItem, GalleryItemAdapter.GalleryItemViewHolder>(DIFF_CALLBACK) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GalleryItem>() {
            override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
                return oldItem == newItem
            }
        }

    }

    private val itemClickedAt: (Int) -> Unit = { position ->
        itemClick.invoke(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_gallery, parent, false)
        return GalleryItemViewHolder(view, itemClickedAt)
    }

    override fun onBindViewHolder(holder: GalleryItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class GalleryItemViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                itemClick.invoke(adapterPosition)
            }
        }

        fun bind(item: GalleryItem) {
            itemView.row_gallery_image_view_video_icon.visibility =
                if (item.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) View.VISIBLE else View.GONE

            GlideApp.with(itemView.context)
                .load(item.contentUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(itemView.row_gallery_image_view_video_thumbnail)
        }
    }

}