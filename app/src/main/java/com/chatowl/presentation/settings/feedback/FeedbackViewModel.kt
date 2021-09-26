package com.chatowl.presentation.settings.feedback

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.settings.apppreferences.FeedbackMessage
import com.chatowl.data.entities.settings.apppreferences.FeedbackType
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.SingleLiveEvent
import kotlinx.coroutines.delay
import java.net.UnknownHostException
import java.util.*

class FeedbackViewModel(application: Application, val args: FeedbackDialogFragmentArgs) : BaseViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: FeedbackDialogFragmentArgs) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FeedbackViewModel(application, args) as T
        }
    }

    val messageType = args.messageType

    val title: LiveData<String> get() = _title
    private val _title = MutableLiveData<String>()

    val dismiss: LiveData<Boolean> get() = _dismiss
    private val _dismiss = SingleLiveEvent<Boolean>()

    val showForm: LiveData<Boolean> get() = _showForm
    private val _showForm = MutableLiveData<Boolean>()

    val showSuccess: LiveData<Boolean> get() = _showSuccess
    private val _showSuccess = SingleLiveEvent<Boolean>()

    val errorMessage: LiveData<Int> get() = _errorMessage
    private val _errorMessage = SingleLiveEvent<Int>()

    val isSendButtonEnabled: LiveData<Boolean> get() = _isSendButtonEnabled
    private val _isSendButtonEnabled = MutableLiveData(false)

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun setTitle(){
        when(messageType){
            FeedbackType.FEEDBACK -> {
                _title.value = getApplication<ChatOwlApplication>().resources.getString(R.string.give_feedback)
            }
            FeedbackType.`CONTACT-US` -> {
                _title.value = getApplication<ChatOwlApplication>().resources.getString(R.string.contact_us)
            }
        }
    }

    fun onSendClicked(message: String) {
        if (message.isNotBlank()) {
            sendMessage(message)
        } else {
            _errorMessage.value = R.string.message_can_not_be_empty
        }
    }

    fun onCancelClicked() {
        _dismiss.value = true
    }

    private fun sendMessage(message: String) {
        _isLoading.value = true
        _showForm.value = false
        viewModelScope.fetch({
            val feedbackMessage = FeedbackMessage(message, Locale.getDefault().toLanguageTag(), messageType.name.toLowerCase())
            ChatOwlUserRepository.sendFeedbackMessage(feedbackMessage)
        }, {
            _isLoading.value = false
            _showSuccess.value = true
            delayAndDismissDialog()
        }, {
            _isLoading.value = false
            _showForm.value = true
            if (it is UnknownHostException){
                _errorMessage.value = R.string.no_internet_connection
            } else {
                _errorMessage.value = R.string.unexpected_error
            }
        })
    }

    private fun delayAndDismissDialog() {
        viewModelScope.fetch({
            delay(4000)
        }, {
            _dismiss.value = true
        }, {})
    }

    fun onMessageTextChanged(message: String) {
        _isSendButtonEnabled.value = message.isNotEmpty()
    }

}
