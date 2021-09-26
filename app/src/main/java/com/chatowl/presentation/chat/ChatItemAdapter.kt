package com.chatowl.presentation.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.chat.ChatMessageSender
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.utils.MeasureUtils
import com.chatowl.presentation.common.utils.getTimeFromServerDate
import kotlinx.android.synthetic.main.row_activity_large.view.*
import kotlinx.android.synthetic.main.row_chat_date_header.view.*
import kotlinx.android.synthetic.main.row_chat_image_received.view.*
import kotlinx.android.synthetic.main.row_chat_image_received.view.row_chat_image_image_view
import kotlinx.android.synthetic.main.row_chat_image_received.view.row_chat_image_text_view_time
import kotlinx.android.synthetic.main.row_chat_image_sent.view.*
import kotlinx.android.synthetic.main.row_chat_marker.view.*
import kotlinx.android.synthetic.main.row_chat_text_received.view.*
import kotlinx.android.synthetic.main.row_chat_tool.view.*
import kotlinx.android.synthetic.main.row_chat_video.view.*
import java.lang.UnsupportedOperationException

const val ITEM_VIEW_TYPE_TEXT_SENT = 0
const val ITEM_VIEW_TYPE_TEXT_RECEIVED = 1

const val ITEM_VIEW_TYPE_IMAGE_SENT = 2
const val ITEM_VIEW_TYPE_IMAGE_RECEIVED = 3

const val ITEM_VIEW_TYPE_VIDEO_SENT = 4
const val ITEM_VIEW_TYPE_VIDEO_RECEIVED = 5

const val ITEM_VIEW_TYPE_TOOL_SENT = 6
const val ITEM_VIEW_TYPE_TOOL_RECEIVED = 7

const val ITEM_VIEW_TYPE_MARKER = 8

const val ITEM_VIEW_TYPE_QUESTION = 9

const val ITEM_VIEW_TYPE_DATE_HEADER = -1

class ChatItemAdapter(
    private val context: Context,
    private val onChatItemClick: (item: ChatMessage) -> Unit
) : ListAdapter<ChatAdapterItem, RecyclerView.ViewHolder>(HomeActivityDiffCallback()) {

    abstract class BindableChatItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: ChatMessage, showTimeStamp: Boolean)

        protected fun handleTimestamp(
            timeStampTextView: TextView,
            timestamp: String,
            showTimeStamp: Boolean
        ) {
            if (showTimeStamp) {
                timeStampTextView.text = getTimeFromServerDate(timestamp)
                timeStampTextView.visibility = View.VISIBLE
            } else {
                timeStampTextView.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_TEXT_SENT ->
                ChatTextViewHolder(inflater.inflate(R.layout.row_chat_text_sent, parent, false))
            ITEM_VIEW_TYPE_TEXT_RECEIVED ->
                ChatTextViewHolder(inflater.inflate(R.layout.row_chat_text_received, parent, false))
            ITEM_VIEW_TYPE_IMAGE_SENT -> {
                val itemView = inflater.inflate(R.layout.row_chat_image_sent, parent, false)
                val height = MeasureUtils.getScreenHeightPercentage(context, 0.4)
                val constraintSet = ConstraintSet()
                constraintSet.clone(itemView.row_chat_image_sent_layout_main)
                constraintSet.constrainMaxHeight(R.id.row_chat_image_sent_card_view, height)
                constraintSet.applyTo(itemView.row_chat_image_sent_layout_main)
                ChatImageViewHolder(itemView)
            }
            ITEM_VIEW_TYPE_IMAGE_RECEIVED -> {
                val itemView = inflater.inflate(R.layout.row_chat_image_received, parent, false)
                val height = MeasureUtils.getScreenHeightPercentage(context, 0.4)
                val constraintSet = ConstraintSet()
                constraintSet.clone(itemView.row_chat_image_received_layout_main)
                constraintSet.constrainMaxHeight(R.id.row_chat_image_received_card_view, height)
                constraintSet.applyTo(itemView.row_chat_image_received_layout_main)
                ChatImageViewHolder(itemView)
            }
            ITEM_VIEW_TYPE_VIDEO_SENT ->
                throw UnsupportedOperationException("ViewHolder unsupported for view type $ITEM_VIEW_TYPE_VIDEO_SENT")
            ITEM_VIEW_TYPE_VIDEO_RECEIVED ->
                ChatVideoViewHolder(inflater.inflate(R.layout.row_chat_video, parent, false))
            ITEM_VIEW_TYPE_TOOL_SENT ->
                throw UnsupportedOperationException("ViewHolder unsupported for view type $ITEM_VIEW_TYPE_TOOL_SENT")
            ITEM_VIEW_TYPE_TOOL_RECEIVED ->
                ChatToolboxItemViewHolder(inflater.inflate(R.layout.row_chat_tool, parent, false))
            ITEM_VIEW_TYPE_MARKER ->
                ChatMarkerViewHolder(inflater.inflate(R.layout.row_chat_marker, parent, false))
            ITEM_VIEW_TYPE_QUESTION ->
                QuestionViewHolder(inflater.inflate(R.layout.row_chat_question, parent, false))
            ITEM_VIEW_TYPE_DATE_HEADER ->
                ChatDateHeaderViewHolder(
                    inflater.inflate(
                        R.layout.row_chat_date_header,
                        parent,
                        false
                    )
                )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ChatDateHeaderViewHolder -> {
                val headerItem = item as ChatAdapterItem.DateHeaderAdapterItem
                holder.bind(headerItem.label)
            }
            is BindableChatItemViewHolder -> {
                val chatTextItem = item as ChatAdapterItem.ChatHistoryAdapterItem
                holder.bind(chatTextItem.chatMessage, chatTextItem.showTimestamp)
            }
        }
    }

    inner class ChatDateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String) {
            itemView.row_chat_date_header_text_view.text = item
        }
    }

    inner class ChatTextViewHolder(itemView: View) : BindableChatItemViewHolder(itemView) {
        override fun bind(item: ChatMessage, showTimeStamp: Boolean) {
            itemView.row_chat_text_text_view_body.text = item.text
            if(item.sender.equals(ChatMessageSender.USER.name, true) && !item.sent) {
                itemView.row_chat_text_text_view_time.text = ""
                itemView.row_chat_text_text_view_time.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_clock_outline),
                    null,
                    null,
                    null
                )
            } else {
                itemView.row_chat_text_text_view_time.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
                )
                handleTimestamp(
                    itemView.row_chat_text_text_view_time,
                    item.timestamp,
                    showTimeStamp
                )
            }
        }
    }

    inner class ChatImageViewHolder(itemView: View) : BindableChatItemViewHolder(itemView) {
        override fun bind(item: ChatMessage, showTimeStamp: Boolean) {
            itemView.setOnClickListener {
                onChatItemClick.invoke(item)
            }
            GlideApp.with(itemView)
                .load(item.mediaUrl)
                .centerCrop()
                .into(itemView.row_chat_image_image_view)
            handleTimestamp(itemView.row_chat_image_text_view_time, item.timestamp, showTimeStamp)
        }
    }

    inner class ChatVideoViewHolder(itemView: View) : BindableChatItemViewHolder(itemView) {
        override fun bind(item: ChatMessage, showTimeStamp: Boolean) {
            itemView.setOnClickListener {
                onChatItemClick.invoke(item)
            }
            GlideApp.with(itemView)
                .load(item.thumbnailUrl)
                .into(itemView.row_chat_video_image_view_background)
            handleTimestamp(itemView.row_chat_video_text_view_time, item.timestamp, showTimeStamp)
        }
    }

    inner class ChatMarkerViewHolder(itemView: View) : BindableChatItemViewHolder(itemView) {
        override fun bind(item: ChatMessage, showTimeStamp: Boolean) {
            itemView.setOnClickListener {
                onChatItemClick.invoke(item)
            }
            itemView.row_chat_marker_text_view_title.text = item.text
            itemView.row_chat_marker_text_view_description.text = item.subText
            GlideApp.with(itemView)
                .load(item.thumbnailUrl)
                .into(itemView.row_chat_marker_image_view)
        }
    }

    inner class QuestionViewHolder(itemView: View) : BindableChatItemViewHolder(itemView) {
        override fun bind(item: ChatMessage, showTimeStamp: Boolean) {}
    }

    inner class ChatToolboxItemViewHolder(itemView: View) : BindableChatItemViewHolder(itemView) {
        override fun bind(item: ChatMessage, showTimeStamp: Boolean) {
            itemView.setOnClickListener {
                onChatItemClick.invoke(item)
            }
            val tool = item.tool
            itemView.row_activity_large_text_view_title.text = tool?.title
            GlideApp.with(itemView)
                .load(item.thumbnailUrl)
                .into(itemView.row_activity_large_image_view)
            handleTimestamp(itemView.row_chat_tool_text_view_time, item.timestamp, showTimeStamp)
        }
    }
}

class HomeActivityDiffCallback : DiffUtil.ItemCallback<ChatAdapterItem>() {
    override fun areItemsTheSame(
        oldAdapterItem: ChatAdapterItem,
        newAdapterItem: ChatAdapterItem
    ): Boolean {
        return oldAdapterItem.id == newAdapterItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldAdapterItem: ChatAdapterItem,
        newAdapterItem: ChatAdapterItem
    ): Boolean {
        return oldAdapterItem == newAdapterItem
    }
}


sealed class ChatAdapterItem {

    data class ChatHistoryAdapterItem(val chatMessage: ChatMessage, val showTimestamp: Boolean) : ChatAdapterItem() {
        override val id = chatMessage.id
        override val type = chatMessage.getChatItemType()
    }

    data class DateHeaderAdapterItem(val label: String) : ChatAdapterItem() {
        override val id = label
        override val type = ITEM_VIEW_TYPE_DATE_HEADER
    }

    abstract val id: String
    abstract val type: Int
}

