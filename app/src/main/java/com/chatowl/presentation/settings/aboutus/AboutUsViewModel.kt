package com.chatowl.presentation.settings.aboutus

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.chatowl.R
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.SingleLiveEvent


class AboutUsViewModel(application: Application) : BaseViewModel(application) {

    val TAG = AboutUsViewModel::class.java.simpleName

    val fullScreenNavigate: LiveData<NavDirections> get() = _fullScreenNavigate
    private val _fullScreenNavigate = SingleLiveEvent<NavDirections>()

    val resourcelink: LiveData<Int> get() = _resourceLink
    private val _resourceLink = MutableLiveData<Int>()

    fun onTermsAndConditionsClicked() {
        _resourceLink.value = R.string.link_terms
    }

    fun onPrivacyPolicyClicked() {
        _resourceLink.value = R.string.link_privacy

    }

    private val _isLegalEmailSentSuccessfully = SingleLiveEvent<Boolean>()
    val isLegalEmailSent: LiveData<Boolean>
        get() = _isLegalEmailSentSuccessfully

    fun onEmailThisClicked() {
        viewModelScope.fetch({
            _isLegalEmailSentSuccessfully.postValue(false)
            ChatOwlUserRepository.sendLegalEmailToMe()
        }, {
            _isLegalEmailSentSuccessfully.value = it
           Log.d(TAG, "was sent successfully")
        }, {
            Log.e(TAG, "wasn't sent successfully, $it")
        })
    }
}
