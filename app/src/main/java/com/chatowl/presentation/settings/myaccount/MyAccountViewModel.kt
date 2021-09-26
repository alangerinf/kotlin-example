package com.chatowl.presentation.settings.myaccount

import android.app.Application
import androidx.lifecycle.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler
import com.chatowl.R
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.get
import com.chatowl.presentation.main.MainGraphViewModel.Companion.ATTRIBUTE_EMAIL
import com.chatowl.presentation.main.MainGraphViewModel.Companion.ATTRIBUTE_NICKNAME
import java.net.UnknownHostException


class MyAccountViewModel(
    application: Application,
    private val repository: ChatOwlUserRepository,
    private val homeRepository: ChatOwlHomeRepository,
    private val userPool: CognitoUserPool
) : BaseViewModel(application), LifecycleObserver {

    private val application = getApplication<ChatOwlApplication>()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val repository: ChatOwlUserRepository,
        private val homeRepository: ChatOwlHomeRepository,
        private val userPool: CognitoUserPool
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MyAccountViewModel(application, repository, homeRepository, userPool) as T
        }
    }

    val email: LiveData<String> get() = _email
    private val _email = MutableLiveData<String>()

    val nickname: LiveData<String> get() = _nickname
    private val _nickname = MutableLiveData<String>()

    private val detailsHandler: GetDetailsHandler = object : GetDetailsHandler {
        override fun onSuccess(cognitoUserDetails: CognitoUserDetails?) {
            cognitoUserDetails?.let { details ->
                val attributeMap = details.attributes.attributes
                _email.value = attributeMap[ATTRIBUTE_EMAIL]
                _nickname.value = attributeMap[ATTRIBUTE_NICKNAME]
            }
        }

        override fun onFailure(exception: Exception?) {
            //TODO show  a frendly message,
            //system message is now hidded
            //_systemMessage.value = exception?.localizedMessage
            loadStoredClientInfo()
        }
    }

    private fun loadStoredClientInfo(){
        viewModelScope.fetch({
            homeRepository.getClientInfoFromDB()
        }, {
            _nickname.value = it.nickname?:""
        }, {
            _systemMessage.value = application.getString(R.string.error)
        })
        _email.value = PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.USER_EMAIL, "") ?: ""
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        userPool.currentUser.takeIf { it.userId != null }?.getDetailsInBackground(detailsHandler)
    }

    fun downloadDataClicked() {
        viewModelScope.fetch({
            repository.downloadData()
        }, {
            _systemMessage.value = application.getString(R.string.success)
        }, {
            if (it is UnknownHostException) {
                _navigate.value =
                    MyAccountFragmentDirections.actionGlobalNoInternetDialogFragment()
            } else {
                _navigate.value =
                    MyAccountFragmentDirections.actionGlobalMessageDialog(R.string.unexpected_error)
            }

        })
    }

    fun deleteAccountClicked() {
        _navigate.value =
            MyAccountFragmentDirections.actionAppPreferencesToDeleteAccountDialog()
    }

    fun onAttributeEdit(attribute: String, maxSize: Int, isMultiline: Boolean) {
        _navigate.value = MyAccountFragmentDirections
            .actionMyAccountToEditField(attribute)
            .setMaxSize(maxSize)
            .setIsMultiline(isMultiline)
    }

}
