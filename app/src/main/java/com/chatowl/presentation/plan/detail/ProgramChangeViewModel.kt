package com.chatowl.presentation.plan.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.plan.ProgramPreview
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch

class ProgramChangeViewModel(
    application: Application,
    private val planRepository: ChatOwlPlanRepository,
    args: ProgramChangeDialogFragmentArgs
) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val application: Application,
        val repository: ChatOwlPlanRepository,
        val args: ProgramChangeDialogFragmentArgs
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ProgramChangeViewModel(application, repository, args) as T
        }
    }

    var newPlanId: Int

    val versionName: LiveData<String> get() = _versionName
    private val _versionName = MutableLiveData<String>()

    val planPreview: LiveData<ProgramPreview> get() = _planPreview
    val _planPreview = MutableLiveData<ProgramPreview>()

    val planChanged: LiveData<Boolean> get() = _planChanged
    private val _planChanged = MutableLiveData<Boolean>()

    val messageBodyResourceId: LiveData<Int> get() = _messageBodyResourceId
    private val _messageBodyResourceId = MutableLiveData<Int>()

    init {
        newPlanId = args.newPlanId
        _planPreview.value = args.planPreview
        _versionName.value = args.planVersion
    }


    fun changePlanConfirmed() {
        _isLoading.value = true
        viewModelScope.fetch({
            planRepository.changePlanFull(newPlanId)
        }, {
            _isLoading.value = false
            val title =
                getApplication<ChatOwlApplication>().resources.getString(R.string.plan_changed_title)
            val plan = "${planPreview.value!!.name}: ${planPreview.value!!.tagline}"
            val message = getApplication<ChatOwlApplication>().resources.getString(
                R.string.plan_changed_message,
                plan
            )
            _navigate.value =
                ProgramChangeDialogFragmentDirections.actionProgramChangeToMessageDialogV2(
                    title,
                    message
                )
            //_planChanged.value = true
        }, {
            _isLoading.value = false
            Log.e("PlanListViewModel", it.message ?: it.toString())
            //TODO show error
        })
        //  }
    }

}
