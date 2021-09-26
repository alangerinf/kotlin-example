package com.chatowl.presentation.settings.myaccount.editfield

import android.app.Application
import androidx.lifecycle.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler
import com.chatowl.R
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.instabug.library.logging.InstabugLog
import java.lang.Exception
import java.util.*


class EditFieldViewModel(application: Application, private val userPool: CognitoUserPool, args: EditFieldFragmentArgs) : BaseViewModel(application), LifecycleObserver {

    val application = getApplication<ChatOwlApplication>()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val userPool: CognitoUserPool,
        private val args: EditFieldFragmentArgs
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditFieldViewModel(application, userPool, args) as T
        }
    }

    val fieldName: LiveData<String> get() = _fieldName
    private val _fieldName = MutableLiveData<String>()

    val fieldValue: LiveData<String> get() = _fieldValue
    private val _fieldValue = MutableLiveData<String>()

    val fieldError: LiveData<String?> get() = _fieldError
    private val _fieldError = MutableLiveData<String?>()

    val dismiss: LiveData<Boolean> get() = _dismiss
    private val _dismiss = SingleLiveEvent<Boolean>()

    private val detailsHandler: GetDetailsHandler = object : GetDetailsHandler {
        override fun onSuccess(cognitoUserDetails: CognitoUserDetails?) {
            cognitoUserDetails?.let { details ->
                val attributeMap = details.attributes.attributes
                _fieldValue.value = attributeMap[fieldName.value]
            }
        }

        override fun onFailure(exception: Exception?) {
            //TODO display a friendly error message
            //System error, and any other errors are hidded now
            //_systemMessage.value = exception?.localizedMessage
            InstabugLog.d("EditFieldFragment -> getDetails:  ${exception?.message}")
        }
    }

    init {
        _fieldName.value = args.attribute
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        userPool.currentUser.takeIf { it.userId != null }?.getDetailsInBackground(detailsHandler)
    }

    fun onSaveClicked(fieldValue: String) {
        if(fieldValue.isNotEmpty()) {
            _fieldError.value = null
            val attributes = CognitoUserAttributes().apply {
                addAttribute(fieldName.value, fieldValue)
            }
            userPool.currentUser.takeIf { it.userId != null }
                ?.updateAttributesInBackground(attributes, updateAttributesHandler)
        } else {
            _fieldError.value = application.getString(
                R.string.format_empty_field_error_message,
            fieldName.value ?: application.getString(R.string.field_value)
            ).capitalize(Locale.ROOT)
        }
    }

    private val updateAttributesHandler: UpdateAttributesHandler = object : UpdateAttributesHandler {
        override fun onSuccess(attributesVerificationList: MutableList<CognitoUserCodeDeliveryDetails>?) {
            _dismiss.value = true
        }

        override fun onFailure(exception: Exception?) {
            //TODO display a friendly message
            //error are hidded now
            //_systemMessage.postValue(exception?.localizedMessage)
            InstabugLog.d("EditFieldFragment -> updateAttrubute:  ${exception?.message}")

        }
    }

}
