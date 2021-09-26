package com.chatowl.presentation.toolbox.home

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroupItem
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.extensions.secondsToDescriptiveString
import kotlinx.android.synthetic.main.row_activity_small.view.*

const val ITEM_VIEW_TYPE_TOOLBOX_ITEM = 0
const val ITEM_VIEW_TYPE_SHOW_MORE = 1

class ToolboxHomeGroupItemAdapter(itemClick: (item: ToolboxHomeGroupItem) -> Unit) :
    ListAdapter<ToolboxHomeGroupItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ToolboxHomeGroupItem>() {
            override fun areItemsTheSame(
                oldItem: ToolboxHomeGroupItem,
                newItem: ToolboxHomeGroupItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ToolboxHomeGroupItem,
                newItem: ToolboxHomeGroupItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    private val itemClickedAt: (Int) -> Unit = { position ->
        itemClick.invoke(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_TOOLBOX_ITEM -> {
                val view = inflater.inflate(R.layout.row_activity_small, parent, false)
                if (currentList.size<=1) {
                    view.layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT
                }
                ToolboxItemViewHolder(view, itemClickedAt)
            }
            ITEM_VIEW_TYPE_SHOW_MORE -> {
                val view = inflater.inflate(R.layout.row_activity_show_more, parent, false)
                ShowMoreViewHolder(view, itemClickedAt)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ToolboxItemViewHolder -> {
                val toolboxItem = item as ToolboxHomeGroupItem.ToolboxHomeToolboxItem
                holder.bind(toolboxItem)
            }
            is ShowMoreViewHolder -> {
                holder.bind()
            }
        }
    }

    inner class ToolboxItemViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener { itemClick.invoke(adapterPosition) }
        }

        fun bind(adapterItem: ToolboxHomeGroupItem.ToolboxHomeToolboxItem) {

            val item = adapterItem.toolboxItem

            itemView.row_activity_small_text_view_title.text = item.tool.title

            itemView.row_activity_small_image_view_favorite.visibility = if(item.tool.isFavorite) View.VISIBLE else View.INVISIBLE

            if (item.tool.duration != null) {
                itemView.row_activity_small_text_view_description.text =
                    item.tool.duration?.secondsToDescriptiveString(itemView.context)
                itemView.row_activity_small_text_view_description.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_clock),
                    null,
                    null,
                    null
                )
            } else {
                itemView.row_activity_small_text_view_description.text = item.tool.description
                itemView.row_activity_small_text_view_description.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
                )
            }

            when {
                item.isNew -> {
                    itemView.row_activity_small_text_tag.text =
                        itemView.context.getString(R.string.tag_new)
                    itemView.row_activity_small_text_tag.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(itemView.context, R.drawable.ic_star),
                        null
                    )
                    itemView.row_activity_small_text_tag.visibility = View.VISIBLE
                }
                else -> {
                    itemView.row_activity_small_text_tag.visibility = View.GONE
                }
            }

            GlideApp.with(itemView)
                .load(item.tool.thumbnailUrl)
                .into(itemView.row_activity_small_image_view)
            itemView.row_activity_small_image_view.colorFilter =
                ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1f) })

            val iconDrawable = when {
                item.isLocked -> R.drawable.ic_locked
                else -> item.tool.toolboxIcon()
            }

            itemView.row_activity_small_image_view_icon.setImageDrawable(
                ContextCompat.getDrawable(itemView.context, iconDrawable)
            )

        }
    }

    inner class ShowMoreViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

        fun bind() {
            itemView.setOnClickListener { itemClick.invoke(adapterPosition) }
            itemView
        }
    }

}