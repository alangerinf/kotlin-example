package com.chatowl.presentation.signup

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.chatowl.R
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.isEmailValid


class SignUpViewModel(application: Application) : BaseViewModel(application) {

    private val isAgeCheckboxChecked: LiveData<Boolean> get() = _isAgeCheckboxChecked
    private val _isAgeCheckboxChecked = MutableLiveData(false)

    private val isTermsCheckboxChecked: LiveData<Boolean> get() = _isTermsCheckboxChecked
    private val _isTermsCheckboxChecked = MutableLiveData(false)

    val signUpUser: LiveData<Pair<String, String>> get() = _signUpUser
    private val _signUpUser = SingleLiveEvent<Pair<String, String>>()

    private val nickname: LiveData<String> get() = _nickname
    private val _nickname = MutableLiveData<String>()

    private val email: LiveData<String> get() = _email
    private val _email = MutableLiveData<String>()

    val resourceLink: LiveData<Int> get() = _resourceLink
    private val _resourceLink = MutableLiveData<Int>()

    val nicknameFieldError: LiveData<Int?> get() = _nicknameFieldError
    private val _nicknameFieldError = MutableLiveData<Int?>()

    val emailFieldError: LiveData<Int?> get() = _emailFieldError
    private val _emailFieldError = MutableLiveData<Int?>()

    val isSignUpButtonEnabled = MediatorLiveData<Boolean>()

    init {

        isSignUpButtonEnabled.addSource(nickname) {
            isSignUpButtonEnabled.value = checkCompleteForm()
        }

        isSignUpButtonEnabled.addSource(email) {
            isSignUpButtonEnabled.value = checkCompleteForm()
        }

        isSignUpButtonEnabled.addSource(isAgeCheckboxChecked) {
            isSignUpButtonEnabled.value = checkCompleteForm()
        }

        isSignUpButtonEnabled.addSource(isTermsCheckboxChecked) {
            isSignUpButtonEnabled.value = checkCompleteForm()
        }

    }

    private fun checkCompleteForm(): Boolean {
        return !nickname.value.isNullOrBlank() &&
                !email.value.isNullOrBlank() &&
                isAgeCheckboxChecked.value == true &&
                isTermsCheckboxChecked.value == true
    }

    fun onSignUpClicked(nickname: String, email: String) {
        if(isValidSignUpInformation(nickname, email)) {
            _signUpUser.value = Pair(nickname, email)
        }
    }

    fun onNicknameFieldChanged(nickname: String) {
        _nickname.value = nickname
    }

    fun onEmailFieldChanged(email: String) {
        _email.value = email
    }

    fun onAgeCheckboxChecked(isChecked: Boolean) {
        _isAgeCheckboxChecked.value = isChecked
    }

    fun onTermsCheckboxChecked(isChecked: Boolean) {
        _isTermsCheckboxChecked.value = isChecked
    }

    @SuppressLint("NullSafeMutableLiveData")
    private fun isValidSignUpInformation(nickname: String, email: String): Boolean {
        if (nickname.isBlank()) {
            _nicknameFieldError.value = R.string.empty_nickname_field_error_message
            return false
        } else {
            _nicknameFieldError.value = null
        }

        if (email.isBlank()) {
            _emailFieldError.value = R.string.empty_email_field_error_message_empty
            return false
        } else if (!email.isEmailValid()) {
            _emailFieldError.value = R.string.malformed_email_field_error_message
            return false
        } else {
            _emailFieldError.value = null
        }

        return true
    }

    fun onTermsAndConditionsClicked() {
        _resourceLink.value = R.string.link_terms
    }

    fun onPrivacyPolicyClicked() {
        _resourceLink.value = R.string.link_privacy
    }
}
