package com.chatowl.presentation.settings.contactus

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.welcome.ContactUsMessage
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.disclaimer.DisclaimerFragmentArgs
import com.chatowl.presentation.disclaimer.DisclaimerViewModel
import kotlinx.coroutines.delay
import java.net.UnknownHostException
import java.util.*

class ContactUsViewModel(application: Application) : BaseViewModel(application) {

    val dismiss: LiveData<Boolean> get() = _dismiss
    private val _dismiss = SingleLiveEvent<Boolean>()

    val showForm: LiveData<Boolean> get() = _showForm
    private val _showForm = MutableLiveData<Boolean>()

    val showSuccess: LiveData<Boolean> get() = _showSuccess
    private val _showSuccess = SingleLiveEvent<Boolean>()

    val emailErrorMessage: LiveData<Int> get() = _emailErrorMessage
    private val _emailErrorMessage = SingleLiveEvent<Int>()

    val errorMessage: LiveData<Int> get() = _errorMessage
    private val _errorMessage = SingleLiveEvent<Int>()

    val isSendButtonEnabled: LiveData<Boolean> get() = _isSendButtonEnabled
    private val _isSendButtonEnabled = MutableLiveData(false)

    fun onSendClicked(message: String, email: String) {
        when {
            message.isBlank() -> {
                _errorMessage.value = R.string.message_can_not_be_empty
            }
            email.isNullOrEmpty() -> {
                _emailErrorMessage.value = R.string.email_can_not_be_empty
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _emailErrorMessage.value = R.string.malformed_email_field_error_message
            }
            else -> {
                sendMessage(message, email)
            }
        }
    }

    fun onCancelClicked() {
        _dismiss.value = true
    }

    private fun sendMessage(message: String, email:String) {
        _isLoading.value = true
        _showForm.value = false
        viewModelScope.fetch({
            val contactUsMessage = ContactUsMessage(message, Locale.getDefault().toLanguageTag(), email)
            ChatOwlUserRepository.sendContactUsMessage(contactUsMessage)
        }, {
            _isLoading.value = false
            _showSuccess.value = true
            delayAndDismissDialog()
        }, {
            _isLoading.value = false
            _showForm.value = true
            _errorMessage.value = R.string.unexpected_error

            if (it is UnknownHostException){
                _errorMessage.value =
                    R.string.no_internet_connection
            } else {
                _errorMessage.value =
                    R.string.unexpected_error
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
