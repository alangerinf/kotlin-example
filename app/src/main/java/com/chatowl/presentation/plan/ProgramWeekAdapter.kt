package com.chatowl.presentation.plan

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.home.ProgramActivity
import com.chatowl.data.entities.plan.ProgramWeek
import com.chatowl.data.entities.plan.ProgramWeekStatus
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.home.ActivityAdapter
import kotlinx.android.synthetic.main.row_week_active_plan.view.*

class ProgramWeekAdapter(
    private val isActive: Boolean,
    private val onActivityClick: (activity: ProgramActivity) -> Unit
) : ListAdapter<ProgramWeek, ProgramWeekAdapter.ProgramWeekViewHolder>(
    ProgramWeekDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramWeekViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ProgramWeekViewHolder(
            inflater.inflate(
                R.layout.row_week_active_plan,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProgramWeekViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ProgramWeekViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val activityAdapter: ActivityAdapter =
            ActivityAdapter(isActive) { activity -> onActivityClick.invoke(activity) }

        init {
            val visibility = if(isActive) View.VISIBLE else View.GONE
            itemView.row_week_active_plan_connector_top.visibility = visibility
            itemView.row_week_active_plan_view_status_indicator.visibility = visibility
            itemView.row_week_active_plan_connector_bottom.visibility = visibility
            itemView.row_week_active_plan_image_view_indicator_done.visibility = visibility
            itemView.row_week_active_plan_image_view_indicator_current.visibility = visibility

            //itemView.row_week_active_plan_group_active_views.visibility = if(isActive) View.VISIBLE else View.GONE
            itemView.row_week_active_plan_recycler_view.adapter = activityAdapter
        }

        fun bind(item: ProgramWeek) {
            activityAdapter.submitList(item.weekActivities)
            itemView.row_week_active_plan_text_view_title.text =
                itemView.context.getString(R.string.format_week, item.week)
            itemView.row_week_active_plan_text_view_subtitle.text = item.name
            itemView.row_week_active_plan_text_view_description.text = item.description
            GlideApp.with(itemView)
                .load(item.thumbnailUrl)
                .into(itemView.row_week_active_plan_image_view)
            if (item.isExpanded) {
                itemView.row_week_active_plan_image_view_disclosure.setImageDrawable(
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_up)
                )
                itemView.row_week_active_plan_detail.visibility = View.VISIBLE
            } else {
                itemView.row_week_active_plan_detail.visibility = View.GONE
                itemView.row_week_active_plan_image_view_disclosure.setImageDrawable(
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_down)
                )
            }
            itemView.setOnClickListener {
                getItem(adapterPosition).isExpanded = !item.isExpanded
                for (i in adapterPosition..itemCount ) {
                    notifyItemChanged(i)
                }
            }
            if(isActive) {
                when {
                    item.status.equals(ProgramWeekStatus.DONE.name, true) -> {
                        itemView.row_week_active_plan_image_view_indicator_done.visibility =
                            View.VISIBLE
                        itemView.row_week_active_plan_image_view_indicator_current.visibility =
                            View.GONE
                    }
                    item.status.equals(ProgramWeekStatus.IN_PROGRESS.name, true) -> {
                        itemView.row_week_active_plan_image_view_indicator_done.visibility =
                            View.GONE
                        itemView.row_week_active_plan_image_view_indicator_current.visibility =
                            View.VISIBLE
                    }
                    else -> {
                        itemView.row_week_active_plan_image_view_indicator_done.visibility =
                            View.GONE
                        itemView.row_week_active_plan_image_view_indicator_current.visibility =
                            View.GONE
                    }
                }
                itemView.row_week_active_plan_connector_top.visibility =
                    if (adapterPosition == 0) View.GONE else View.VISIBLE
                itemView.row_week_active_plan_connector_bottom.visibility =
                    if (currentList.size == adapterPosition.plus(1)) View.GONE else View.VISIBLE
            }
        }
    }
}

class ProgramWeekDiffCallback : DiffUtil.ItemCallback<ProgramWeek>() {
    override fun areItemsTheSame(oldItem: ProgramWeek, newItem: ProgramWeek): Boolean {
        return oldItem.week == newItem.week
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ProgramWeek, newItem: ProgramWeek): Boolean {
        return oldItem == newItem
    }
}

