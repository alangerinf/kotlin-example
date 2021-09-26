package com.chatowl.presentation.plan

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.plan.ProgramDetail
import com.chatowl.data.entities.plan.ProgramPreview
import com.chatowl.data.entities.plan.ProgramVersion
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import java.util.*

class PlanVersionChangeViewModel(application: Application, private val planRepository: ChatOwlPlanRepository, args: PlanVersionChangeDialogFragmentArgs) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val application: Application,
        val repository: ChatOwlPlanRepository,
        val args: PlanVersionChangeDialogFragmentArgs
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlanVersionChangeViewModel(application, repository, args) as T
        }
    }

    val versionName: LiveData<String> get() = _versionName
    private val _versionName = MutableLiveData<String>()

    val messageBodyResourceId: LiveData<Int> get() = _messageBodyResourceId
    private val _messageBodyResourceId = MutableLiveData<Int>()

    val buttonTextResourceId: LiveData<Int> get() = _buttonTextResourceId
    private val _buttonTextResourceId = MutableLiveData<Int>()

    private val programList = args.planList
    val planVersion = ProgramVersion.valueOf(args.planVersion.toUpperCase())

    val currentProgramId = args.programId

    init {
        _versionName.value = args.planVersion.toLowerCase(Locale.getDefault())
            .capitalize(Locale.getDefault())
        when(planVersion) {
            ProgramVersion.FULL ->{
                _messageBodyResourceId.value = R.string.change_to_slim_body
                _buttonTextResourceId.value = R.string.try_slim_version
            }
            ProgramVersion.SLIM -> {
                _messageBodyResourceId.value = R.string.change_to_full_body
                _buttonTextResourceId.value = R.string.try_full_version
            }

        }

    }

    fun changeVersion() {
        _isLoading.value = true
        viewModelScope.fetch({
            planRepository.getPlanDetail(currentProgramId)
        }, {
            _isLoading.value = false
            changeVersion(it)
        }, {
            _isLoading.value = false
            //TODO display error
        })
    }
    fun changeVersion(programDetail: ProgramDetail) {
        val newPlanId = when(planVersion) {
            ProgramVersion.FULL ->{
                programDetail.slimId
            }
            ProgramVersion.SLIM -> {
                programDetail.fullId
            }

        }
        _isLoading.value = true
        viewModelScope.fetch({

            planRepository.changePlanFull(newPlanId)
        }, {
            _isLoading.value = false
            _navigate.value = PlanVersionChangeDialogFragmentDirections.actionVersionChangeDialogToCurrentPlanFragment()
        }, {
            _isLoading.value = false
            //TODO show error message
        })
    }

}
