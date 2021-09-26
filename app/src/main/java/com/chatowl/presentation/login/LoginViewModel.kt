package com.chatowl.presentation.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chatowl.R
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.isEmailValid


class LoginViewModel(application: Application) : BaseViewModel(application) {

    val emailFieldError: LiveData<Int?> get() = _emailFieldError
    private val _emailFieldError = MutableLiveData<Int?>()

    val loginEmail: LiveData<String> get() = _loginEmail
    private val _loginEmail = SingleLiveEvent<String>()

    init {

    }

    fun onSendCodeClicked(email: String) {
        if(isValidLoginInformation(email)) {
            _loginEmail.value = email
        }
    }

    fun onSignUpClicked() {
        _navigate.value = LoginFragmentDirections.actionLoginToSignup()
    }

    private fun isValidLoginInformation(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                _emailFieldError.value = R.string.empty_email_field_error_message_empty
                false
            }
            !email.isEmailValid() -> {
                _emailFieldError.value = R.string.malformed_email_field_error_message
                false
            }
            else -> {
                _emailFieldError.value = null
                true
            }
        }
    }

    fun onContactSupportClicked() {
        _navigate.value = LoginFragmentDirections.actionGlobalContactUsDialog()
    }
}
