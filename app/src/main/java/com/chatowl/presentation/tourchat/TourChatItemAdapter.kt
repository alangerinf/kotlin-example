package com.chatowl.presentation.tourchat

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.entities.chat.ChatMessageSender
import com.chatowl.data.entities.chat.ChatMessageType
import com.chatowl.presentation.chat.ChatItemAdapter
import com.chatowl.presentation.common.extensions.upperCase
import kotlinx.android.synthetic.main.row_chat_text_received.view.*

class TourChatItemAdapter :
    ListAdapter<ChatMessage, RecyclerView.ViewHolder>(HomeActivityDiffCallback()) {

    var isAppTour  = false

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (item.messageType.equals(ChatMessageType.QUESTION.name, true)){
            return ChatMessageSender.BOT_QUESTION.ordinal
        }
        else if (!item.messageType.equals(ChatMessageType.CHAT_MARKER.name, true)) {
            return ChatMessageSender.valueOf(getItem(position).sender.upperCase()).ordinal
        } else {
            return ChatMessageSender.BOT_CHAT_MARKER.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ChatMessageSender.BOT.ordinal ->
                TourChatTextViewHolder(
                    inflater.inflate(
                        R.layout.row_tour_chat_text_received,
                        parent,
                        false
                    )
                )
            ChatMessageSender.USER.ordinal ->
                TourChatTextViewHolder(
                    inflater.inflate(
                        R.layout.row_tour_chat_text_sent,
                        parent,
                        false
                    )
                )
            ChatMessageSender.BOT_QUESTION.ordinal ->
                TourChatQuestionViewHolder(
                    inflater.inflate(
                        R.layout.row_chat_question,
                        parent,
                        false
                    )
                )
            ChatMessageSender.BOT_CHAT_MARKER.ordinal ->
                TourChatMarkerViewHolder(
                    inflater.inflate(
                        R.layout.row_tour_chat_marker,
                        parent,
                        false
                    )
                )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    inner class TourChatTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ChatMessage) {
            itemView.row_chat_text_text_view_body.text = item.text
        }
    }

    inner class TourChatQuestionViewHolder(itemView: View) : ChatItemAdapter.BindableChatItemViewHolder(itemView) {
        override fun bind(item: ChatMessage, showTimeStamp: Boolean) {}
    }

    inner class TourChatMarkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ChatMessage) {
            //TODO implement a UI marker when it became usefull
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TourChatTextViewHolder -> {
                holder.bind(getItem(position))
            }
            is TourChatMarkerViewHolder -> {
                //THIS is not duplicated code, actually it doesn't do anything, but it might be different in the future
                holder.bind(getItem(position))
            }
            //TourChatQuestionViewHolder is missing beacuse it should not be shown
        }
    }
}

class HomeActivityDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(
        oldAdapterItem: ChatMessage,
        newAdapterItem: ChatMessage
    ): Boolean {
        return oldAdapterItem.id == newAdapterItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldAdapterItem: ChatMessage,
        newAdapterItem: ChatMessage
    ): Boolean {
        return oldAdapterItem == newAdapterItem
    }
}

