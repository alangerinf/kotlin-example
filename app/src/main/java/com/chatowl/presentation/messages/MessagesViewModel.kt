package com.chatowl.presentation.messages

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.data.entities.journal.DRAFT_ID
import com.chatowl.data.entities.messages.MessagePreview
import com.chatowl.data.entities.messages.asMessagePreviewList
import com.chatowl.data.repositories.MessagesRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.toolbox.host.ToolboxHostFragmentDirections


class MessagesViewModel(
    application: Application,
    private val messagesRepository: MessagesRepository
) : BaseViewModel(application), LifecycleObserver {

    companion object {
        const val MESSAGE_PAGE_SIZE = 10
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val messagesRepository: MessagesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MessagesViewModel(application, messagesRepository) as T
        }
    }

    val isPageLoading: LiveData<Boolean> get() = _isPageLoading
    private val _isPageLoading = MutableLiveData(false)

    private val messages = messagesRepository.getMessagesLiveData()
    private var currentTotal = 0

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        fetchMessages()
    }

    val adapterItems = Transformations.map(messages) { messages ->
        messages.asMessagePreviewList().sortedWith(
            compareBy<MessagePreview> { !it.isDraft }.thenByDescending { it.date })
    }

    private fun fetchMessages(pageNumber: Int = 0) {
        if (pageNumber == 0) {
            _isLoading.value = true
            _isPageLoading.value = false
        } else {
            _isLoading.value = false
            _isPageLoading.value = true
        }
        viewModelScope.fetch({
            messagesRepository.getMessages(pageNumber)
        }, { totalMessages ->
            currentTotal = totalMessages
            _isLoading.value = false
            _isPageLoading.value = false
        }, {
            _isLoading.value = false
            _isPageLoading.value = false
            it.printStackTrace()
        })
    }

    fun onMessageClicked(messagePreview: MessagePreview) {
        if (messagePreview.id == DRAFT_ID) {
            _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToAddMessage()
        } else {
            _navigate.value =
                ToolboxHostFragmentDirections.actionToolboxHostToViewMessage(messagePreview.id)
        }
    }

    fun onCreateMessageClicked() {
        _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToAddMessage()
    }

    fun onScrollEnded() {
        messages.value?.let { messages ->
            val messagesSize = messages.size
            if (isLoading.value == false && messagesSize < currentTotal) {
                val nextPage = messagesSize.div(MESSAGE_PAGE_SIZE)
                fetchMessages(pageNumber = nextPage)
            }
        }
    }

}