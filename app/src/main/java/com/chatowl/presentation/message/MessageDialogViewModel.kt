package com.chatowl.presentation.message

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.presentation.common.BaseViewModel

class MessageDialogViewModel(application: Application, args: MessageDialogFragmentArgs) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val application: Application,
        val args: MessageDialogFragmentArgs
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MessageDialogViewModel(application, args) as T
        }
    }

    val messageBodyResourceId: LiveData<Int> get() = _messageBodyResourceId
    private val _messageBodyResourceId = MutableLiveData<Int>()

    val showIcon: LiveData<Boolean> get() = _showIcon
    private val _showIcon = MutableLiveData<Boolean>()

    init {
        _messageBodyResourceId.value = args.message
        _showIcon.value = args.showIcon
    }

}
