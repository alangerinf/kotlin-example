package com.chatowl.presentation.chat.property

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.chat.values.PropertyValue
import com.chatowl.data.entities.chat.values.PropertyValueType
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.data.repositories.ChatOwlChatTestRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.SingleLiveEvent

class PropertyViewModel(application: Application, val args: PropertyFragmentArgs) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: PropertyFragmentArgs) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PropertyViewModel(application, args) as T
        }
    }

    private val repository = ChatOwlChatTestRepository

    private val propertyValue: LiveData<PropertyValue> get() = _propertyValue
    private val _propertyValue = MutableLiveData<PropertyValue>()

    val success: LiveData<Boolean> get() = _success
    private val _success = SingleLiveEvent<Boolean>()

    init {
        _propertyValue.value = args.property
    }

    val valueString = Transformations.map(propertyValue) { property ->
        if (property.type == PropertyValueType.STRING.apiReference) {
            "\"${property.value}\""
        } else {
            property.value
        }
    }

    fun onSendValueClicked(string: String) {
        propertyValue.value?.let { propertyValue ->
            _isLoading.value = true
            viewModelScope.fetch({
                repository.setPropertyValue(propertyValue.id, string)
            }, {
                _success.value = true
                _isLoading.value = false
            }, {
                _success.value = false
                _isLoading.value = false
            })
        }
    }

}
