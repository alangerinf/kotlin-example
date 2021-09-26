package com.chatowl.presentation.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.chat.BotChoice
import com.chatowl.presentation.GlideApp
import kotlinx.android.synthetic.main.row_chat_image_choice.view.*

class ImageChoiceAdapter(private val itemClick: (imageBotChoice: BotChoice) -> Unit) :
        ListAdapter<BotChoice, ImageChoiceAdapter.ImageChoiceViewHolder>(DIFF_CALLBACK) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BotChoice>() {
            override fun areItemsTheSame(oldItem: BotChoice, newItem: BotChoice): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: BotChoice, newItem: BotChoice): Boolean {
                return oldItem == newItem
            }
        }

    }

    private val itemClickedAt: (Int) -> Unit = { position ->
        itemClick.invoke(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageChoiceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_chat_image_choice, parent, false)
        return ImageChoiceViewHolder(view, itemClickedAt)
    }

    override fun onBindViewHolder(holder: ImageChoiceViewHolder, position: Int) {
        val tool = getItem(position)
        holder.bind(tool)
    }

    inner class ImageChoiceViewHolder(
            itemView: View,
            private val itemClick: (position: Int) -> Unit
    ) :
            RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                itemClick.invoke(adapterPosition)
            }
        }

        fun bind(imageChoice: BotChoice) {
            if(imageChoice.thumbnailUrl == null) {
                itemView.row_chat_image_choice_image_view_add.visibility = View.VISIBLE
                GlideApp.with(itemView).clear(itemView.row_chat_image_choice_image_view_background)
            } else {
                itemView.row_chat_image_choice_image_view_add.visibility = View.GONE
                GlideApp.with(itemView).load(imageChoice.thumbnailUrl).into(itemView.row_chat_image_choice_image_view_background)
            }
        }
    }

}