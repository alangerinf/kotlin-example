package com.chatowl.presentation.journal.edit.image

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.presentation.common.BaseViewModel

class FullscreenImageViewModel(application: Application, args: FullscreenImageFragmentArgs) : BaseViewModel(application) {

    val imageUrl: LiveData<String> get() = _imageUrl
    private val _imageUrl = MutableLiveData<String>()

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: FullscreenImageFragmentArgs) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FullscreenImageViewModel(application, args) as T
        }
    }

    init {
        _imageUrl.value = args.imageUrl
    }

}