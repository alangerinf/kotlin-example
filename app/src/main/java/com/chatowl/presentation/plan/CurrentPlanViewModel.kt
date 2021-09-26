package com.chatowl.presentation.plan

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.home.ProgramActivity
import com.chatowl.data.entities.plan.ProgramHome
import com.chatowl.data.entities.plan.ProgramPreview
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.helpers.DateUtils.formatTo
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.*
import com.chatowl.presentation.common.utils.SERVER_FORMAT
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.getNextAvailableTime
import com.chatowl.presentation.common.utils.getRemainingDaysInTrial
import java.util.*

class CurrentPlanViewModel(
    application: Application,
    private val planRepository: ChatOwlPlanRepository,
    private val activitiesRepository: ChatOwlHomeRepository
) : BaseViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val planRepository: ChatOwlPlanRepository,
        private val activitiesRepository: ChatOwlHomeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CurrentPlanViewModel(application, planRepository, activitiesRepository) as T
        }
    }

    private val application = getApplication<ChatOwlApplication>()

    private val planHome: LiveData<ProgramHome> get() = _planHome
    private val _planHome = MutableLiveData<ProgramHome>()

    val startChatSessionId: LiveData<Int> get() = _startChatSessionId
    private val _startChatSessionId = SingleLiveEvent<Int>()

    val startChatFlow: LiveData<Integer> get() = _startChatFlow
    private val _startChatFlow = SingleLiveEvent<Integer>()

    val popFromBackStack: LiveData<Boolean> get() = _popFromBackStack
    private val _popFromBackStack = SingleLiveEvent<Boolean>()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        //
        fetchPlans()
    }
    
    private fun fetchPlans() {
        _isLoading.value = true
        viewModelScope.fetch({
            planRepository.getPlanHome()
        }, {
            _isLoading.value = false
            when {
                //TODO I can ask for assessmentDone
                (it.activePlan != null) -> {
                    _planHome.value = it
                }
            }
            _planHome.value = it
        }, {
            _isLoading.value = false
        })
    }

    val currentProgram = Transformations.map(planHome) {
        it.activePlan
    }

    val availablePrograms = Transformations.map(planHome) {
        it.availablePlansDto
    }

    fun onReadMoreClicked() {
        currentProgram.value?.let { program ->
            _navigate.value =
                CurrentPlanFragmentDirections.actionCurrentPlanToPlanDescription(program.getFullProgramDescription())
        }
    }

    fun onChangePlanClicked() {
        planHome.value?.availablePlansDto?.let { plans ->
            _navigate.value =
                CurrentPlanFragmentDirections.actionCurrentPlanToPlanList(plans.toTypedArray())
        }
    }

    fun changePlanSelectedFromDialog(flow: Integer) {
        _startChatFlow.value = flow
    }

    fun onProgramClicked(program: ProgramPreview) {
        fetchPlan(program.id)
    }

    private fun fetchPlan(planId: Int) {
        _isLoading.value = true
        viewModelScope.fetch({
            planRepository.getPlanDetail(planId)
        }, { programDetail ->
            _isLoading.value = false
            val assessmentDone = currentProgram.value != null
            _navigate.value = CurrentPlanFragmentDirections.actionCurrentPlanToPlanDetail(
                programDetail,
                assessmentDone
            )
        }, {
            _isLoading.value = false
        })
    }

    fun onActivityClicked(activity: ProgramActivity) {
        if (isLoading.value == false) {
            when {
                !activity.isFree -> {
                    _navigate.value =
                        CurrentPlanFragmentDirections.actionPlanToExerciseLockedDialog(
                            ExerciseLockedMessage(
                                application.getString(R.string.locked_exercise_title),
                                application.getString(R.string.paywalled_exercise_body),
                                activity.tool.title,
                                activity.tool.toolIcon(),
                                true
                            )
                        )
                }
                activity.pendingTool != null -> {
                    activity.pendingTool?.let { pendingItem ->
                        _navigate.value =
                            CurrentPlanFragmentDirections.actionPlanToExerciseLockedDialog(
                                ExerciseLockedMessage(
                                    application.getString(R.string.locked_exercise_title),
                                    application.getString(R.string.locked_exercise_body),
                                    pendingItem.toolName,
                                    ToolSubtype.valueOf(pendingItem.toolSubtype.upperCase()).drawableId,
                                    false
                                )
                            )
                    }
                }
                activity.daysUntilActivity > 0 -> {
                    val daysText =
                        getRemainingDaysInTrial(activity.daysUntilActivity, application)
                    _navigate.value =
                        CurrentPlanFragmentDirections.actionPlanToExerciseLockedDialog(
                            ExerciseLockedMessage(
                                application.getString(R.string.locked_exercise_title),
                                application.getString(
                                    R.string.exercise_coming_up_message,
                                    daysText
                                ),
                                activity.tool.title,
                                activity.tool.toolIcon(),
                                false
                            )
                        )
                }
                activity.tool.toolType == ToolSubtype.SESSION.name.lowerCase() -> {
                    checkAvailabilityAndContinue(activity)
                }
                else -> {
                    fetchActivityExercise(activity.id, application.getString(R.string.therapy_plan))
                }
            }
        }
    }

    fun checkAvailabilityAndContinue(activity: ProgramActivity) {
        _isLoading.value = true
        viewModelScope.fetch({
            activitiesRepository.getToolAvailability(activity.tool.id)
        }, { toolAvailability ->
            _isLoading.value = false
            val canPlayStatus = ToolAvailability.getCanPlayStatus(toolAvailability)
            when {
                canPlayStatus == CanPlayStatus.CAN_PLAY -> {
                    _startChatSessionId.value = activity.tool.id
                }
                canPlayStatus == CanPlayStatus.CANNOT_PLAY -> {
                    _navigate.value = CurrentPlanFragmentDirections.actionPlanToChatHistoryFragment(
                        activity.tool.id,
                        activity.tool.title
                    )
                }
                canPlayStatus == CanPlayStatus.HAVE_TO_WAIT -> {
                    showHaveToWaitMessage(activity)
                }
            }
        }, {
            //Log.e(TAG, it.toString())
            _isLoading.value = false
            if (it.isConnectionException()) {
                _navigate.value = CurrentPlanFragmentDirections.actionPlanToNoInternetDialog()
            }
        })
    }

    private fun showHaveToWaitMessage(activity: ProgramActivity) {
        val repeatLimitInSeconds = activity.tool.repeatLimit ?: 0
        val repeatLimitMessage = repeatLimitInSeconds
            .secondsToRepeatLimitDescriptiveString(application)

        val calendar = Calendar.getInstance()
        val now = calendar.time
        val stringDate = now.formatTo(SERVER_FORMAT)

        val timeLeftDescription = getNextAvailableTime(
            application,
            repeatLimitInSeconds, stringDate
        )

        val message = application.getString(
            R.string.limited_exercise_body,
            repeatLimitMessage,
            timeLeftDescription
        )
        _navigate.value = CurrentPlanFragmentDirections.actionPlanToExerciseLockedDialog(
            ExerciseLockedMessage(
                application.getString(R.string.limited_exercise_title),
                message,
                activity.tool.title,
                ToolSubtype.valueOf(activity.tool.toolSubtype.upperCase()).drawableId,
                false
            )
        )
    }

    private fun fetchActivityExercise(activityId: Int, title: String) {
        _isLoading.value = true
        viewModelScope.fetch({
            activitiesRepository.getActivityExercise(activityId)
        }, { exercise ->
            navigateToExercise(exercise, title)
            _isLoading.value = false
        }, {
            _isLoading.value = false
            if (it.isConnectionException()) {
                _navigate.value = CurrentPlanFragmentDirections.actionPlanToNoInternetDialog()
            }
        })
    }

    private fun navigateToExercise(exercise: ToolboxExercise, title: String?) {
        when (exercise.tool.toolSubtype) {
            ToolSubtype.VIDEO.name.lowerCase(), ToolSubtype.AUDIO.name.lowerCase() -> {
                _navigate.value = CurrentPlanFragmentDirections.actionPlanToMediaExercise(exercise)
                    .setHeader(title)
            }
            ToolSubtype.QUOTE.name.lowerCase() -> {
                _navigate.value = CurrentPlanFragmentDirections.actionPlanToQuoteExercise(exercise)
                    .setHeader(title)
            }
            ToolSubtype.IMAGE.name.lowerCase() -> {
                _navigate.value = CurrentPlanFragmentDirections.actionPlanToImageExercise(exercise)
                    .setHeader(title)
            }
        }
    }

    fun onPlanVersionClicked() {
        currentProgram.value?.let {
            _navigate.value = CurrentPlanFragmentDirections.actionPlanToVersionChangeDialog(
                it.id,
                it.version,
                availablePrograms.value?.toTypedArray()
                    ?: emptyList<ProgramPreview>().toTypedArray()
            )
        }
    }
}
