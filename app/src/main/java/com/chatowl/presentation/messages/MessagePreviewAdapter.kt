package com.chatowl.presentation.messages

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.messages.MessagePreview
import com.chatowl.presentation.common.utils.ABBREVIATED_MONTH_DAY_YEAR_HOUR_MINUTES_AM_PM
import com.chatowl.presentation.common.utils.SERVER_FORMAT
import com.chatowl.presentation.common.utils.changeDateFormat
import kotlinx.android.synthetic.main.row_message_preview.view.*


class MessagePreviewAdapter(private val itemClick: (MessagePreview) -> Unit) :
    ListAdapter<MessagePreview, MessagePreviewAdapter.MessagePreviewViewHolder>(
        DIFF_CALLBACK
    ) {

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessagePreview>() {
            override fun areItemsTheSame(
                oldItem: MessagePreview,
                newItem: MessagePreview
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MessagePreview,
                newItem: MessagePreview
            ): Boolean {
                return oldItem == newItem
            }
        }

    }

    private val itemClickedAt: (Int) -> Unit = { position ->
        itemClick.invoke(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePreviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_message_preview, parent, false)
        return MessagePreviewViewHolder(view, itemClickedAt)
    }

    override fun onBindViewHolder(holder: MessagePreviewViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry)
    }

    inner class MessagePreviewViewHolder(
        itemView: View,
        private val itemClick: (position: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                itemClick.invoke(adapterPosition)
            }
        }

        fun bind(item: MessagePreview) {
            itemView.row_message_preview_text_view_content_preview.text = item.preview
            itemView.row_message_preview_badge.visibility =
                if (item.showBadge) View.VISIBLE else View.INVISIBLE

            if (item.isDraft) {
                itemView.row_journal_text_view_draft.visibility = View.VISIBLE
                itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.blue2_10
                    )
                )
                itemView.row_message_preview_text_view_draft_date.text = changeDateFormat(
                    item.date,
                    SERVER_FORMAT,
                    ABBREVIATED_MONTH_DAY_YEAR_HOUR_MINUTES_AM_PM
                )
                itemView.row_message_preview_text_view_draft_date.visibility = View.VISIBLE
                itemView.row_message_preview_text_view_date.visibility = View.INVISIBLE
                // reply
                itemView.row_message_preview_text_view_reply_info.visibility = View.GONE
                itemView.row_message_preview_text_view_reply_pending.visibility = View.GONE
            } else {
                itemView.row_journal_text_view_draft.visibility = View.GONE
                itemView.setBackgroundColor(Color.WHITE)
                itemView.row_message_preview_text_view_date.text = changeDateFormat(
                    item.date,
                    SERVER_FORMAT,
                    ABBREVIATED_MONTH_DAY_YEAR_HOUR_MINUTES_AM_PM
                )
                itemView.row_message_preview_text_view_draft_date.visibility = View.INVISIBLE
                itemView.row_message_preview_text_view_date.visibility = View.VISIBLE
                // reply
                if (item.reply != null) {
                    itemView.row_message_preview_text_view_reply_info.text =
                        itemView.context.getString(
                            R.string.format_string_long_space_string,
                            changeDateFormat(
                                item.reply.date,
                                SERVER_FORMAT,
                                ABBREVIATED_MONTH_DAY_YEAR_HOUR_MINUTES_AM_PM
                            ),
                            itemView.context.getString(R.string.therapy_team)
                        )
                    itemView.row_message_preview_text_view_reply_info.visibility = View.VISIBLE
                    itemView.row_message_preview_text_view_reply_pending.visibility = View.GONE
                } else {
                    itemView.row_message_preview_text_view_reply_info.visibility = View.GONE
                    itemView.row_message_preview_text_view_reply_pending.visibility = View.VISIBLE
                }
            }
        }

    }
}