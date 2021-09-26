package com.chatowl.presentation.messages.viewmessage

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.data.entities.journal.asAdapterItemList
import com.chatowl.data.entities.messages.Message
import com.chatowl.data.repositories.MessagesRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch

class ViewMessageViewModel(
    application: Application,
    args: ViewMessageFragmentArgs,
    private val messagesRepository: MessagesRepository
) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val args: ViewMessageFragmentArgs,
        private val messagesRepository: MessagesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewMessageViewModel(application, args, messagesRepository) as T
        }
    }

    private val _message = MutableLiveData<Message>()
    val message: LiveData<Message> get() = _message

    init {
        fetchMessageById(args.messageId)
    }

    private fun fetchMessageById(messageId: String) {
        viewModelScope.fetch({
            messagesRepository.getMessageById(messageId)
        }, { message ->
            _message.value = message
            if(message.reply != null && message.unread) {
                setMessageAsRead(messageId)
            }
        }, {
            it.printStackTrace()
        })
    }

    val components = Transformations.map(message) { message ->
        message.components.asAdapterItemList()
    }

    private fun setMessageAsRead(messageId: String) {
        viewModelScope.fetch({
            messagesRepository.setMessageAsRead(messageId)
        }, {}, {})
    }

}