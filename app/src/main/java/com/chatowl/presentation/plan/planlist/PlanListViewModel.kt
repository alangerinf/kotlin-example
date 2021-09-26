package com.chatowl.presentation.plan.planlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.data.entities.plan.ProgramPreview
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.SingleLiveEvent


class PlanListViewModel(application: Application, args: PlanListFragmentArgs, private val repository: ChatOwlPlanRepository) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: PlanListFragmentArgs, private val repository: ChatOwlPlanRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlanListViewModel(application, args, repository) as T
        }
    }

    val programList: LiveData<List<ProgramPreview>> get() = _programList
    private val _programList = MutableLiveData<List<ProgramPreview>>()

    init {
        _programList.value = args.planList.toList()
    }

    fun onProgramClicked(program: ProgramPreview) {
        fetchPlan(program.id)
    }

    private fun fetchPlan(planId: Int) {
        _isLoading.value = true
        viewModelScope.fetch( {
            repository.getPlanDetail(planId)
        }, {
            _isLoading.value = false
            _navigate.value = PlanListFragmentDirections.actionPlanListToPlanDetail(it, true)
        }, {
            _isLoading.value = false
            Log.e("PlanListViewModel", it.message?:it.toString())
        })
    }
}
