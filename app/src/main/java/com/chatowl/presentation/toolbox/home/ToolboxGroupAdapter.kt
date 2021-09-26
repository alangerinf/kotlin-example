package com.chatowl.presentation.toolbox.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.toolbox.ToolboxCategoryType
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroup
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroupItem
import kotlinx.android.synthetic.main.row_toolbox_group.view.*


class ToolboxGroupAdapter(
    private val groupClick: (item: ToolboxHomeGroup) -> Unit,
    private val itemClick: (group: ToolboxHomeGroup, item: ToolboxHomeGroupItem) -> Unit
) :
    ListAdapter<ToolboxHomeGroup, ToolboxGroupAdapter.CategoryViewHolder>(DIFF_CALLBACK) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ToolboxHomeGroup>() {
            override fun areItemsTheSame(
                oldItem: ToolboxHomeGroup,
                newItem: ToolboxHomeGroup
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ToolboxHomeGroup,
                newItem: ToolboxHomeGroup
            ): Boolean {
                return oldItem == newItem
            }
        }

    }

    // For OnItemTouchListener
    private var xDistance = 0f
    private var yDistance = 0f
    private var lastX = 0f
    private var lastY = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_toolbox_group, parent, false)
        return CategoryViewHolder(view, groupClick, itemClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)
    }

    inner class CategoryViewHolder(
        itemView: View,
        private val groupClick: (item: ToolboxHomeGroup) -> Unit,
        private val itemClick: (group: ToolboxHomeGroup, item: ToolboxHomeGroupItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        init {
        }

        fun bind(group: ToolboxHomeGroup) {
            when{
                group.groupType == ToolboxCategoryType.PROGRAM -> {
                    itemView.fragment_toolbox_group_text_view_title.text = group.title
                }
                else -> {
                    itemView.fragment_toolbox_group_text_view_title.text = group.groupName
                }
            }

            when{
                group.subtitle.isNotEmpty() -> {
                    itemView.fragment_toolbox_group_text_view_subtitle.text = group.subtitle
                    itemView.fragment_toolbox_group_text_view_subtitle.visibility = View.VISIBLE
                }
                else -> {
                    itemView.fragment_toolbox_group_text_view_subtitle.visibility = View.GONE
                }
            }
            itemView.fragment_toolbox_group_text_view_title.setOnClickListener {
                groupClick.invoke(group)
            }
            val groupItemAdapter = ToolboxHomeGroupItemAdapter { groupItem ->
                if (groupItem.id == -1) {
                    groupClick.invoke(group)
                } else {
                    itemClick.invoke(group, groupItem)
                }
            }
            itemView.fragment_toolbox_group_recycler_view.adapter = groupItemAdapter
            groupItemAdapter.submitList(group.items)
        }
    }

}