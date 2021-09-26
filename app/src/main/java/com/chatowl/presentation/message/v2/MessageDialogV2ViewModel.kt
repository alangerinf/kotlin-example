package com.chatowl.presentation.message.v2

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chatowl.presentation.common.BaseViewModel

class MessageDialogV2ViewModel(application: Application, args: MessageDialogFragmentV2Args) :
    BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val application: Application,
        val args: MessageDialogFragmentV2Args
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MessageDialogV2ViewModel(application, args) as T
        }
    }

    val messageTitleString: LiveData<String> get() = _messageTitleString
    private val _messageTitleString = MutableLiveData<String>()

    val messageBodyString: LiveData<String> get() = _messageBodyString
    private val _messageBodyString = MutableLiveData<String>()

    val showIcon: LiveData<Boolean> get() = _showIcon
    private val _showIcon = MutableLiveData<Boolean>()

    val iconResourceId: LiveData<Int> get() = _iconResourceId
    private val _iconResourceId = MutableLiveData<Int>()


    init {
        _messageTitleString.value = args.title
        _messageBodyString.value = args.message
        _showIcon.value = args.showIcon
        _iconResourceId.value = args.iconResourceId
    }

}
