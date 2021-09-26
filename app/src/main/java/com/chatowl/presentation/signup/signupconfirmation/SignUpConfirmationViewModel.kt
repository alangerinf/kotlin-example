package com.chatowl.presentation.signup.signupconfirmation

import android.app.Application
import com.chatowl.presentation.common.BaseViewModel


class SignUpConfirmationViewModel(application: Application) : BaseViewModel(application) {

    init {

    }

    fun onCorrectAnimationEnd() {
        _navigate.value = SignUpConfirmationFragmentDirections.actionSignupConfirmationToSecureModePickerFragment()
    }

    fun onContactSupportClicked() {
        _navigate.value = SignUpConfirmationFragmentDirections.actionGlobalContactUsDialog()
    }
}
