package com.chatowl.presentation.splash

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.SingleLiveEvent
import kotlinx.coroutines.delay


class SplashViewModel(application: Application) : BaseViewModel(application) {

    val splashDone: LiveData<Boolean> get() = _splashDone
    private val _splashDone = SingleLiveEvent<Boolean>()

    init {
        showSplash()
    }

    private fun showSplash() {
        viewModelScope.fetch({
            delay(1500)
        }, {
            _splashDone.value = true
        }, {})
    }
}
