package com.chatowl.presentation.toolbox.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.extensions.secondsToDescriptiveString
import kotlinx.android.synthetic.main.row_activity_large.view.*

class ToolboxItemAdapter(private val itemClick: (toolboxItem: ToolboxItem) -> Unit) :
        ListAdapter<ToolboxItem, ToolboxItemAdapter.ToolViewHolder>(DIFF_CALLBACK) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ToolboxItem>() {
            override fun areItemsTheSame(oldItem: ToolboxItem, newItem: ToolboxItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ToolboxItem, newItem: ToolboxItem): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.tool.thumbnailUrl == newItem.tool.thumbnailUrl &&
                        oldItem.isPaywalled == newItem.isPaywalled &&
                        oldItem.isLocked == newItem.isLocked &&
                        oldItem.timeLastWatched == newItem.timeLastWatched
            }
        }

    }

    private val itemClickedAt: (Int) -> Unit = { position ->
        itemClick.invoke(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_activity_large, parent, false)
        return ToolViewHolder(view, itemClickedAt)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        val tool = getItem(position)
        holder.bind(tool)
    }

    inner class ToolViewHolder(
            itemView: View,
            private val itemClick: (position: Int) -> Unit
    ) :
            RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                itemClick.invoke(adapterPosition)
            }
        }

        fun bind(toolboxItem: ToolboxItem) {
            itemView.row_activity_large_text_view_title.text = toolboxItem.tool.title
            itemView.row_activity_large_image_view_favorite.visibility = if (toolboxItem.tool.isFavorite) View.VISIBLE else View.INVISIBLE
            GlideApp.with(itemView).load(toolboxItem.tool.thumbnailUrl).into(itemView.row_activity_large_image_view)

            if (toolboxItem.tool.duration != null) {
                itemView.row_activity_large_text_view_description.text = toolboxItem.tool.duration?.secondsToDescriptiveString(itemView.context)
                itemView.row_activity_large_text_view_description.visibility = View.VISIBLE
            } else {
                itemView.row_activity_large_text_view_description.visibility = View.GONE
            }

            if (toolboxItem.tool.duration != null) {
                itemView.row_activity_large_text_view_description.text =
                    toolboxItem.tool.duration?.secondsToDescriptiveString(itemView.context)
                itemView.row_activity_large_text_view_description.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_clock),
                    null,
                    null,
                    null
                )
            } else {
                itemView.row_activity_large_text_view_description.text = toolboxItem.tool.description
                itemView.row_activity_large_text_view_description.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
                )
            }

            val iconDrawable = when {
                toolboxItem.isLocked -> R.drawable.ic_locked
                else -> toolboxItem.tool.toolboxIcon()
            }

            itemView.row_activity_large_image_view_icon.setImageDrawable(
                ContextCompat.getDrawable(itemView.context, iconDrawable)
            )
        }
    }

}