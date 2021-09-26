package com.chatowl.presentation.plan.detail

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.plan.ProgramDetail
import com.chatowl.data.entities.plan.ProgramVersion
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.combineWithNotNull
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.findNavController
import com.chatowl.presentation.common.utils.SingleLiveEvent
import kotlinx.coroutines.coroutineScope


class PlanDetailViewModel(application: Application, private val planRepository: ChatOwlPlanRepository, args: PlanDetailFragmentArgs) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: PlanDetailFragmentArgs) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val planRepository = ChatOwlPlanRepository
            return PlanDetailViewModel(application, planRepository, args) as T
        }
    }

    val programDetail: LiveData<ProgramDetail> get() = _programDetail
    private val _programDetail = MutableLiveData<ProgramDetail>()

    val assessmentDone: LiveData<Boolean> get() = _assessmentDone
    private val _assessmentDone = MutableLiveData<Boolean>()

    val programVersion: LiveData<ProgramVersion> get() = _programVersion
    private val _programVersion = MutableLiveData(ProgramVersion.FULL)

    val deepNavigation: LiveData<String> get() = _deepNavigation
    private val _deepNavigation = SingleLiveEvent<String>()

    init {
        _programDetail.value = args.programDetail
        _assessmentDone.value = args.assessmentDone
    }

    fun onReadMoreClicked() {
        programDetail.value?.let { programDetail ->
            _navigate.value = PlanDetailFragmentDirections.actionPlanDetailToPlanDescription(programDetail.getFullProgramDescription())
        }
    }

    fun changeToPlanClicked() {
        if (assessmentDone.value?: true){
            showPlanChangeDialog()
        } else {
            goToAssessment()
        }

    }

    private fun showPlanChangeDialog(){
        programDetail.value?.let { programDetail ->
            val programId = if (_programVersion.value == ProgramVersion.FULL){
                programDetail.fullId
            } else {
                programDetail.slimId
            }
            _navigate.value = PlanDetailFragmentDirections.actionPlanDetailToPlanChangeDialog(
                programVersion.value!!.name,
                programId,
                programDetail.getProgramPreview()

            )
        }
    }

    private fun goToAssessment(){
        val toolId = programDetail.value?.assessmentTool?.id?: null
        val stringUri = "com.chatowl://tabview?tab=${R.id.nav_chat}&param=$toolId"
        _deepNavigation.value = stringUri
    }

    val programWeeks = programDetail.combineWithNotNull(programVersion) { programDetail, programVersion ->
        if(programVersion == ProgramVersion.FULL) {
            programDetail.full
        } else {
            programDetail.slim
        }
    }

    fun setVersionType(programVersion: ProgramVersion) {
        _programVersion.value = programVersion
    }
}
