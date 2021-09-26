package com.chatowl.presentation.login.loginconfirmation

import android.app.Application
import com.chatowl.presentation.common.BaseViewModel


class LoginConfirmationViewModel(application: Application) : BaseViewModel(application) {

    init {

    }

    fun onCorrectAnimationEnd() {
        _navigate.value = LoginConfirmationFragmentDirections.actionLoginConfirmationToSecureModePickerFragment()
    }

    fun onContactSupportClicked() {
        _navigate.value = LoginConfirmationFragmentDirections.actionGlobalContactUsDialog()
    }

}
