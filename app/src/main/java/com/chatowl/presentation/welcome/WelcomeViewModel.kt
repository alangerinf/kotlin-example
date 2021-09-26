package com.chatowl.presentation.welcome

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch


class WelcomeViewModel(application: Application, private val repository: ChatOwlUserRepository) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val repository: ChatOwlUserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WelcomeViewModel(application, repository) as T
        }
    }

    init {

    }

    fun onSoundsGreatClicked() {
        // TODO dismiss Fragment and trigger welcome tour chat flow
    }

    fun onNoThanksClicked() {
        // TODO this could be done with GlobalScope so you wouldn't have to wait for a response
        viewModelScope.fetch({
            repository.updateShowWelcome(false)
        }, {

        }, {

        })
    }
}
