package com.chatowl.presentation.chat.flows

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.data.entities.chat.flows.Flow
import com.chatowl.data.repositories.ChatOwlChatTestRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch

class FlowsViewModel(application: Application, private val chatOwlChatTestRepository: ChatOwlChatTestRepository) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val repository: ChatOwlChatTestRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FlowsViewModel(application, repository) as T
        }
    }

    private val flowItems: LiveData<List<Flow>> get() = _flowItems
    private val _flowItems = MutableLiveData<List<Flow>>()

    private val searchFilter: LiveData<String> get() = _searchFilter
    private val _searchFilter = MutableLiveData("")

    val filteredValues = MediatorLiveData<List<Flow>>()

    init {
        filteredValues.addSource(searchFilter) {
            filteredValues.value = filterData()
        }

        filteredValues.addSource(flowItems) {
            filteredValues.value = filterData()
        }

        fetchFlows()
    }

    private fun fetchFlows() {
        _isLoading.value = true
        viewModelScope.fetch({
            chatOwlChatTestRepository.getFlows()
        }, { response ->
            _isLoading.value = false
            _flowItems.value = response
        }, {
            _isLoading.value = false
        })
    }

    private fun filterData(): List<Flow> {
        val unfilteredList = flowItems.value ?: emptyList()
        return unfilteredList.filter { it.name?.contains(searchFilter.value ?: "", true) ?: true }
    }

    fun onFilterSearchChanged(searchString: String) {
        _searchFilter.value = searchString
    }
}
