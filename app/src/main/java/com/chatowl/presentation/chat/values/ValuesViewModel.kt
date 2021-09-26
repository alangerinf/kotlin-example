package com.chatowl.presentation.chat.values

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.data.entities.chat.values.PropertyValue
import com.chatowl.data.repositories.ChatOwlChatTestRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch

class ValuesViewModel(application: Application, private val repository: ChatOwlChatTestRepository) : BaseViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val repository: ChatOwlChatTestRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ValuesViewModel(application, repository) as T
        }
    }

    private val propertyValueItems: LiveData<List<PropertyValue>> get() = _propertyValueItems
    private val _propertyValueItems = MutableLiveData<List<PropertyValue>>()

    private val searchFilter: LiveData<String> get() = _searchFilter
    private val _searchFilter = MutableLiveData("")

    val filteredValues = MediatorLiveData<List<PropertyValue>>()

    init {
        filteredValues.addSource(searchFilter) {
            filteredValues.value = filterData()
        }

        filteredValues.addSource(propertyValueItems) {
            filteredValues.value = filterData()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        fetchPropertyValues()
    }

    private fun fetchPropertyValues() {
        _isLoading.value = true
        viewModelScope.fetch({
            repository.getPropertyValues()
        }, { response ->
            _isLoading.value = false
            _propertyValueItems.value = response
        }, {
            _isLoading.value = false
        })
    }

    private fun filterData(): List<PropertyValue> {
        val unfilteredList = propertyValueItems.value ?: emptyList()
        return unfilteredList.filter { it.name?.contains(searchFilter.value ?: "", true) ?: true }
    }

    fun onFilterSearchChanged(searchString: String) {
        _searchFilter.value = searchString
    }

    fun onItemClick(property: PropertyValue) {
        _navigate.value = ValuesFragmentDirections.actionValuesToProperty(property)
    }

}
