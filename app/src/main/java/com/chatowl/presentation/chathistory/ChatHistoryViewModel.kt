package com.chatowl.presentation.chathistory

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.repositories.ChatOwlChatRepository
import com.chatowl.presentation.chat.ChatAdapterItem
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.getChatMessageHeaderDescriptiveDate
import com.chatowl.presentation.common.utils.getShortFormatDate
import com.chatowl.presentation.common.utils.getTimeFromServerDate

class ChatHistoryViewModel(
    application: Application,
    val args: ChatHistoryFragmentArgs,
    private val chatRepository: ChatOwlChatRepository
) : BaseViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val args: ChatHistoryFragmentArgs,
        private val chatRepository: ChatOwlChatRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ChatHistoryViewModel(application, args, chatRepository) as T
        }
    }

    private val TAG = "ChatHistoryViewModel"

    val messages: LiveData<List<ChatMessage>> get() = _messages
    private val _messages = MutableLiveData<List<ChatMessage>>()

    val title:LiveData<String> get() = _title
    private val _title = MutableLiveData<String>()

    fun onViewCreated() {
        val sessionId = args.chatSessionId
        viewModelScope.fetch({
            chatRepository.getChatHistory(sessionId)
        }, {
            _messages.value = it
        }, {
            it.message?.let { Log.e(TAG, it) }
        })
        _title.value = args.title

    }

    val chatAdapterItems = Transformations.map(messages) { historyItems ->
        val items = when {
            historyItems.isEmpty() -> {
                emptyList()
            }
            else -> {
                val sortedMessagesByDate = historyItems.groupBy { getShortFormatDate(it.timestamp) }
                val sortedKeys = sortedMessagesByDate.keys.sorted()
                val messagesWithHeaders = mutableListOf<ChatAdapterItem>()
                for (key in sortedKeys) {
                    messagesWithHeaders.add(
                        ChatAdapterItem.DateHeaderAdapterItem(
                            getChatMessageHeaderDescriptiveDate(application, key)
                        )
                    )
                    val sortedMessagesForDate = sortedMessagesByDate[key] ?: emptyList()
                    messagesWithHeaders.addAll(sortedMessagesForDate.mapIndexed { index, messageForDate ->
                        val showTimestamp = if (index < sortedMessagesForDate.size - 1) {
                            getTimeFromServerDate(sortedMessagesForDate[index + 1].timestamp) != getTimeFromServerDate(
                                messageForDate.timestamp
                            ) ||
                                    historyItems[index + 1].sender != messageForDate.sender
                        } else {
                            true
                        }
                        ChatAdapterItem.ChatHistoryAdapterItem(messageForDate, showTimestamp)
                    })
                }
                messagesWithHeaders
                //messagesWithHeaders.reversed()
            }
        }

        items
    }
}