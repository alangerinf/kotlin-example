package com.chatowl.presentation.disclaimer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chatowl.presentation.common.BaseViewModel


class DisclaimerViewModel(application: Application, args: DisclaimerFragmentArgs) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: DisclaimerFragmentArgs) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DisclaimerViewModel(application, args) as T
        }
    }

    var toLogin: Boolean = args.toLogin

    fun onCrisisButtonClicked() {
        _navigate.value = DisclaimerFragmentDirections.actionDisclaimerToCrisisSupport()
    }

    fun onIUnderstandClicked() {
        if (toLogin) {
            _navigate.value = DisclaimerFragmentDirections.actionDisclaimerToLogin()
        } else {
            _navigate.value = DisclaimerFragmentDirections.actionDisclaimerToSignUp()
        }
    }
}
