package com.chatowl.presentation.settings.myaccount

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.chatowl.data.api.chat.SocketManager
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeleteAccountViewModel(
    application: Application,
    private val userPool: CognitoUserPool, private val repository: ChatOwlUserRepository
) : BaseViewModel(application) {

    val canLogout: LiveData<Boolean> get() = _canLogout
    private val _canLogout = SingleLiveEvent<Boolean>()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val userPool: CognitoUserPool,
        private val repository: ChatOwlUserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DeleteAccountViewModel(application, userPool, repository) as T
        }
    }


    init {

    }

    fun onDeleteClicked() {
        viewModelScope.fetch({
            repository.deleteAccount()
        }, {
            if (it) {
                _canLogout.value = true
            }
            //TODO handle else
        }, {
            //TODO handle exception
        })
    }

    fun onLogout() {
        //erase FirebaseId
        viewModelScope.fetch({
            ChatOwlUserRepository.clearFirebaseToken()
        }, {
            continueOnLogout()
        }, {
            continueOnLogout()
        })
    }

    private fun continueOnLogout() {
        // Cognito sign out
        userPool.currentUser.signOut()
        // Clear preferences
        PreferenceHelper.clearSharedPreferences()
        //state.set(MainGraphViewModel.ATTRIBUTE_EMAIL, null)
        // Clear chat persistence and nullify socket
        SocketManager.resetInstance()
        // Clear all database tables
        viewModelScope.launch(Dispatchers.IO) {
            ChatOwlDatabase.getInstance(getApplication()).clearAllTables()
        }
        // Navigate to deleted dialog (also clean the backstack)
        _navigate.value =
            DeleteAccountDialogFragmentDirections.actionDeleteAccountToDeletedAccountdialog()
    }

}
