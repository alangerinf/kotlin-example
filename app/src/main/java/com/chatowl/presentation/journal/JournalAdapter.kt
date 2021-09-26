package com.chatowl.presentation.journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.journal.JournalPreview
import com.chatowl.data.helpers.DateUtils
import com.chatowl.data.helpers.DateUtils.formatTo
import com.chatowl.data.helpers.DateUtils.getDayInt
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.utils.*
import kotlinx.android.synthetic.main.row_entry_item_video.view.*
import kotlinx.android.synthetic.main.row_journal.view.*


class JournalAdapter(
    private val itemClick: (JournalPreview) -> Unit,
    private val itemDelete: (JournalPreview) -> Unit
) : ListAdapter<JournalPreview, JournalAdapter.JournalViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<JournalPreview>() {
            override fun areItemsTheSame(
                oldItem: JournalPreview,
                newItem: JournalPreview
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: JournalPreview,
                newItem: JournalPreview
            ): Boolean {
                return oldItem == newItem
            }
        }

    }

    private val itemClickedAt: (Int) -> Unit = { position ->
        itemClick.invoke(getItem(position))
    }

    private val itemTryToDeletedAt: (Int) -> Unit = { position ->
        itemDelete.invoke(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_journal, parent, false)
        return JournalViewHolder(view, itemClickedAt)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class JournalViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                itemClick.invoke(adapterPosition)
            }
        }

        fun bind(item: JournalPreview) {
            // Entry text
            itemView.row_journal_text.text = item.description

            when {
                item.thumbnailUrl != null -> {
                    GlideApp.with(itemView)
                        .load(item.thumbnailUrl)
                        .into(itemView.row_journal_image_view_thumbnail)
                    itemView.row_journal_image_view_thumbnail.visibility = View.VISIBLE
                    itemView.row_journal_image_view_media_icon.visibility = View.GONE
                    itemView.row_journal_card_view.visibility = View.VISIBLE
                }
                item.iconResourceId != null -> {
                    itemView.row_journal_image_view_media_icon.setImageDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            item.iconResourceId
                        )
                    )
                    itemView.row_journal_image_view_media_icon.visibility = View.VISIBLE
                    itemView.row_journal_image_view_thumbnail.visibility = View.GONE
                    itemView.row_journal_card_view.visibility = View.VISIBLE
                }
                else -> {
                    itemView.row_journal_card_view.visibility = View.GONE
                }
            }

            if (item.isDraft) {
                // Show/hide elements
                itemView.row_journal_text_view_draft.visibility = View.VISIBLE
                itemView.row_journal_text.maxLines = 2
            } else {
                // Show/hide elements
                itemView.row_journal_text_view_draft.visibility = View.GONE
                itemView.row_journal_text.maxLines = 3
            }
            // Entry date's values
            val journalDate = getDateFromServerDateString(item.date)
            itemView.row_journal_day_string.text =
                journalDate.formatTo(DateUtils.DATE_DAY_STRING)
            itemView.row_journal_day_number.text = journalDate.getDayInt().toString()
            itemView.row_journal_month.text = journalDate.formatTo(DateUtils.DATE_MONTH_STRING)
        }
    }

    fun onItemTryToDeleteDeleted(position: Int) {
        notifyDataSetChanged()
        itemTryToDeletedAt(position)
    }
}