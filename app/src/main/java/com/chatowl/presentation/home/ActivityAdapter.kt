package com.chatowl.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.chatowl.R
import com.chatowl.data.entities.home.ProgramActivity
import com.chatowl.data.entities.home.ProgramActivity.Companion.TYPE_OVERVIEW
import com.chatowl.data.entities.home.ProgramActivity.Companion.TYPE_REGULAR
import com.chatowl.data.entities.toolbox.ToolType
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.extensions.secondsToDescriptiveString
import com.chatowl.presentation.common.utils.getRemainingDaysInTrial
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.row_activity_small.view.*

class ActivityAdapter(private val isActivePlan: Boolean, private val onActivityClick: (activity: ProgramActivity) -> Unit) :
    ListAdapter<ProgramActivity, ActivityAdapter.ActivityLargeCardViewHolder>(
        HomeActivityDiffCallback()
    ) {

    private val itemClickedAt: (Int) -> Unit = { position ->
        onActivityClick.invoke(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityLargeCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ActivityLargeCardViewHolder(
            when (viewType) {
                TYPE_REGULAR -> {
                    inflater.inflate(
                        R.layout.row_activity_small,
                        parent,
                        false
                    )
                }
                TYPE_OVERVIEW -> {
                    inflater.inflate(
                        R.layout.row_activity_therapy_overview,
                        parent,
                        false
                    )
                }
                else -> throw ClassCastException("Unknown viewType $viewType")
            } as View,
            itemClickedAt
        )
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    override fun onBindViewHolder(holder: ActivityLargeCardViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ActivityLargeCardViewHolder(
        itemView: View,
        val itemClick: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: ProgramActivity) {
            when {
                item.type == TYPE_REGULAR -> {
                    itemView.setOnClickListener { itemClick.invoke(adapterPosition) }
                    itemView.row_activity_small_text_view_title.text = item.tool.title
                    itemView.row_activity_small_text_view_description.text =
                        getDescription(item, itemView.context)
                    if ((item.tool.duration != null) && (isActivePlan)
                        && (item.daysUntilActivity < 0)) {
                        //itemView.row_activity_small_text_view_description.text =
                        item.tool.duration?.secondsToDescriptiveString(itemView.context)
                        itemView.row_activity_small_text_view_description.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(itemView.context, R.drawable.ic_clock),
                            null,
                            null,
                            null
                        )
                    } else {
                        //itemView.row_activity_small_text_view_description.text = item.tool.description
                        itemView.row_activity_small_text_view_description.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            null,
                            null
                        )
                    }

                    when {
                        //TODO Mandatory label is not displayed anymore, this code still commented just in case
                        /* item.isMandatory -> {
                            //itemView.row_activity_small_text_tag.text = itemView.context.getString(R.string.mandatory)
                            itemView.row_activity_small_text_view_description.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                null,
                                null
                            )
                            itemView.row_activity_small_text_tag.visibility = View.GONE
                        }*/
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
                        item.isOngoing -> {
                            itemView.row_activity_small_text_tag.text =
                                itemView.context.getString(R.string.tag_ongoing)
                            itemView.row_activity_small_text_view_description.setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                null,
                                null,
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

                    val iconDrawable = getIconDrawableResourceId(item)

                    if (!isActivePlan){
                        itemView.row_activity_small_image_view_icon.visibility = View.GONE
                    }

                    iconDrawable?.let {
                        itemView.row_activity_small_image_view_icon.setImageDrawable(
                            ContextCompat.getDrawable(itemView.context, it)
                        )
                    }
                }
                item.type == TYPE_OVERVIEW -> {
                    itemView.setOnClickListener { itemClick.invoke(adapterPosition) }
                }
            }
        }

        private fun getIconDrawableResourceId(item: ProgramActivity): Int?{
            return when {
                item.isCompleted -> R.drawable.ic_check_activity_completed
                item.isLocked -> {
                    if (!isActivePlan) {
                        return null
                    } else {
                        return R.drawable.ic_locked
                    }
                }
                else -> item.tool.toolboxIcon()
            }
        }

        private fun getDescription( item: ProgramActivity, context: Context):String {
            when {
                (item.daysUntilActivity >= 0) -> {
                    return getRemainingDaysInTrial(item.daysUntilActivity, context)
                }
                (!isActivePlan) -> {
                    return item.tool.description?:""
                }
                else -> {
                    return item.tool.duration?.secondsToDescriptiveString(context)?:""
                }
            }
        }

    }
}

class HomeActivityDiffCallback : DiffUtil.ItemCallback<ProgramActivity>() {
    override fun areItemsTheSame(oldItem: ProgramActivity, newItem: ProgramActivity): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ProgramActivity, newItem: ProgramActivity): Boolean {
        return oldItem.id == newItem.id &&
                oldItem.tool.thumbnailUrl == newItem.tool.thumbnailUrl &&
                oldItem.isLocked == newItem.isLocked &&
                oldItem.isCompleted == newItem.isCompleted
    }
}

