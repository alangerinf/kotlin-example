package com.chatowl.presentation.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import com.chatowl.presentation.common.utils.SingleLiveEvent

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    open val navigate: LiveData<NavDirections> get() = _navigate
    protected val _navigate = SingleLiveEvent<NavDirections>()

    open val systemMessage: LiveData<String> get() = _systemMessage
    protected val _systemMessage = SingleLiveEvent<String>()

    open val isLoading: LiveData<Boolean> get() = _isLoading
    protected val _isLoading = MutableLiveData(false)

    open val openChat_flowId: LiveData<Int> get() = _openChat_flowId
    open val _openChat_flowId = SingleLiveEvent<Int>()
}