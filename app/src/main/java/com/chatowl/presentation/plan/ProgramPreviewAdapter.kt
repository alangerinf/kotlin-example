package com.chatowl.presentation.plan

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatowl.R
import com.chatowl.data.entities.plan.ProgramPreview
import kotlinx.android.synthetic.main.row_plan_preview.view.*

class ProgramPreviewAdapter(private val onPlanClick: (plan: ProgramPreview) -> Unit) :
    ListAdapter<ProgramPreview, ProgramPreviewAdapter.ProgramPreviewViewHolder>(
        ProgramPreviewDiffCallback()
    ) {

    private val itemClickedAt: (Int) -> Unit = { position ->
        onPlanClick.invoke(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramPreviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ProgramPreviewViewHolder(
            inflater.inflate(
                R.layout.row_plan_preview,
                parent,
                false
            ),
            itemClickedAt
        )
    }

    override fun onBindViewHolder(holder: ProgramPreviewViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ProgramPreviewViewHolder(itemView: View, private val programClick: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                programClick.invoke(adapterPosition)
            }
        }

        fun bind(item: ProgramPreview) {
            itemView.row_plan_preview_text_view_title.text = item.name
            itemView.row_plan_preview_text_view_tagline.text = item.tagline
            Glide.with(itemView.context)
                .load(item.thumbnailUrl)
                .into(itemView.row_plan_preview_image_view)
        }
    }
}

class ProgramPreviewDiffCallback : DiffUtil.ItemCallback<ProgramPreview>() {
    override fun areItemsTheSame(oldItem: ProgramPreview, newItem: ProgramPreview): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ProgramPreview, newItem: ProgramPreview): Boolean {
        return oldItem == newItem
    }
}

