package com.chatowl.presentation.toolbox.plans

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.entities.toolbox.ToolboxCategoryType.PROGRAM
import com.chatowl.data.entities.toolbox.categorydetail.ToolboxCategoryDetail
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroup
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroupItem
import com.chatowl.data.entities.toolbox.home.ToolboxProgram
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.ToolboxViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.*
import com.chatowl.presentation.common.utils.getTextForPausedAt
import com.chatowl.presentation.common.utils.getNextAvailableTime
import com.chatowl.presentation.toolbox.host.ToolboxHostFragmentDirections


class ToolboxPlansViewModel(
    application: Application,
    private val toolboxRepository: ToolboxRepository
) : ToolboxViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val toolboxRepository: ToolboxRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToolboxPlansViewModel(application, toolboxRepository) as T
        }
    }

    val application = getApplication<ChatOwlApplication>()
    private val programs = toolboxRepository.getPrograms()

    val isLoadingPlanOrCategory: LiveData<Boolean> get() = _isLoadingPlanOrCategory
    private val _isLoadingPlanOrCategory = MutableLiveData(false)

    val filteredGroups = MediatorLiveData<List<ToolboxHomeGroup>>()

    init {
        filteredGroups.addSource(programs) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(searchFilter) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(audioFilter) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(imageFilter) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(quoteFilter) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(videoFilter) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(lengthShortFilter) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(lengthMediumFilter) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(lengthLongFilter) {
            filteredGroups.value = filterGroups()
        }
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    private fun onResume() {
//        fetchToolboxData()
//    }
//
//    private fun fetchToolboxData() {
//        _isLoading.value = true
//        viewModelScope.fetch({
//            toolboxRepository.refreshHome()
//        }, {
//            _isLoading.value = false
//        }, {
//            it.printStackTrace()
//            _isLoading.value = false
//        })
//    }

    private fun filterGroups(): List<ToolboxHomeGroup> {
        val groups = mutableListOf<ToolboxHomeGroup>()
        // Categories
        programs.value?.forEach { program ->
            val filteredProgramExercises = filterToolboxItemList(program.exercises)
            if (filteredProgramExercises.isNotEmpty()) {
                var programExercises =
                    mutableListOf<ToolboxHomeGroupItem>(*filteredProgramExercises.map {
                        ToolboxHomeGroupItem.ToolboxHomeToolboxItem(
                            toolboxItem = it,
                            parentName = program.name
                        )
                    }.toTypedArray())
                if (programExercises.size > 4) {
                    programExercises = programExercises.take(4).toMutableList()
                    programExercises.add(
                        ToolboxHomeGroupItem.ToolboxHomeShowMore()
                    )
                }
                groups.add(
                    ToolboxHomeGroup(
                        id = program.id,
                        title = application.getString(R.string.my_therapy_exercises),
                        subtitle = getSubtitle(program),
                        groupName = program.name,
                        groupType = PROGRAM,
                        items = programExercises,
                        pausedAt = program.pausedAt
                    )
                )
            }
        }
        return groups.sortedWith(compareBy(
            { it.pausedAt }))
    }

    private fun getSubtitle(program: ToolboxProgram):String{
        when{
            program.pausedAt.isNullOrEmpty() -> {
                return ChatOwlApplication.get().resources.getText(R.string.your_current_therapy_plan).toString()
            }
            else -> {
                return getTextForPausedAt(
                    ChatOwlApplication.get(), program.pausedAt!!)
            }
        }
    }


    fun onGroupClicked(toolboxHomeGroup: ToolboxHomeGroup) {
        when (toolboxHomeGroup.groupType) {
            PROGRAM -> {
                _navigate.value =
                    ToolboxPlansFragmentDirections.actionToolboxPlansToToolboxCategory(
                        ToolboxCategoryDetail(
                            id = toolboxHomeGroup.id,
                            name = toolboxHomeGroup.groupName,
                            type = toolboxHomeGroup.groupType.name
                        )
                    )
            }
            else -> {

            }
        }
    }

    fun onGroupItemClicked(
        toolboxHomeGroup: ToolboxHomeGroup,
        toolboxHomeGroupItem: ToolboxHomeGroupItem
    ) {
        val toolboxItem =
            (toolboxHomeGroupItem as ToolboxHomeGroupItem.ToolboxHomeToolboxItem).toolboxItem
        when {
            toolboxItem.isPaywalled -> {
                _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToExerciseLocked(
                    ExerciseLockedMessage(
                        application.getString(R.string.locked_exercise_title),
                        application.getString(R.string.paywalled_exercise_body),
                        toolboxItem.tool.title,
                        toolboxItem.tool.toolIcon(),
                        true
                    )
                )
            }
            !toolboxItem.withinRepeatLimit() -> {
                val repeatLimitInSeconds = toolboxItem.tool.repeatLimit ?: 0
                val repeatLimitMessage = repeatLimitInSeconds
                    .secondsToRepeatLimitDescriptiveString(application)
                val timeLeftDescription = getNextAvailableTime(
                    application,
                    repeatLimitInSeconds, toolboxItem.timeLastWatched
                        ?: ""
                )
                val message = application.getString(
                    R.string.limited_exercise_body,
                    repeatLimitMessage,
                    timeLeftDescription
                )
                _navigate.value = ToolboxPlansFragmentDirections.actionToolboxPlansToExerciseLocked(
                    ExerciseLockedMessage(
                        application.getString(R.string.limited_exercise_title),
                        message,
                        toolboxItem.tool.title,
                        toolboxItem.tool.toolIcon(),
                        false
                    )
                )
            }
            toolboxItem.pendingTool != null -> {
                toolboxItem.pendingTool?.let { pendingItem ->
                    _navigate.value = ToolboxPlansFragmentDirections.actionToolboxPlansToExerciseLocked(
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
            (toolboxItem.timeLastWatched != null) &&
                    toolboxItem.tool.toolSubtype.equals(ToolType.SESSION.name, true) -> {
                _navigate.value = ToolboxPlansFragmentDirections.actionToolboxPlansToChatHistoryFragment(toolboxItem.tool.id, toolboxItem.tool.title)
            }
            else -> {
                fetchTool(toolboxItem, toolboxHomeGroup.groupName)
            }
        }
    }

    private fun fetchTool(toolboxItem: ToolboxItem, header: String) {
        _isLoading.value = true
        viewModelScope.fetch({
            toolboxRepository.getToolboxExercise(toolboxItem.id)
        }, { exercise ->
            navigateToExercise(exercise, header)
            _isLoading.value = false
        }, {
            _isLoading.value = false
            if(it.isConnectionException()) {
                _navigate.value = ToolboxPlansFragmentDirections.actionGlobalNoInternetDialog()
            }
        })
    }

    private fun navigateToExercise(exercise: ToolboxExercise, header: String) {
        when (exercise.tool.toolSubtype) {
            ToolSubtype.VIDEO.name.lowerCase(), ToolSubtype.AUDIO.name.lowerCase() -> {
                _navigate.value =
                    ToolboxPlansFragmentDirections.actionGlobalMediaExercise(exercise)
                        .setHeader(header)
            }
            ToolSubtype.QUOTE.name.lowerCase() -> {
                _navigate.value =
                    ToolboxPlansFragmentDirections.actionGlobalQuoteExercise(exercise)
                        .setHeader(header)
            }
            ToolSubtype.IMAGE.name.lowerCase() -> {
                _navigate.value =
                    ToolboxPlansFragmentDirections.actionGlobalImageExercise(exercise)
                        .setHeader(header)
            }
        }
    }

    val isLoadingAndEmpty = isLoading.combineWith(filteredGroups) { isLoading, groups ->
        isLoading == true && groups.isNullOrEmpty()
    }
}
