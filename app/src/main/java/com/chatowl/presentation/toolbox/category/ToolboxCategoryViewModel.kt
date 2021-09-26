package com.chatowl.presentation.toolbox.category

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.entities.toolbox.ToolSubtype.*
import com.chatowl.data.entities.toolbox.categorydetail.ToolboxCategoryDetail
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.ToolboxViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.*
import com.chatowl.presentation.common.utils.getNextAvailableTime
import com.chatowl.presentation.common.utils.getRemainingDaysInTrial
import com.chatowl.presentation.home.HomeFragmentDirections


class ToolboxCategoryViewModel(
    application: Application,
    args: ToolboxCategoryFragmentArgs,
    private val toolboxRepository: ToolboxRepository
) : ToolboxViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val args: ToolboxCategoryFragmentArgs,
        private val toolboxRepository: ToolboxRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToolboxCategoryViewModel(application, args, toolboxRepository) as T
        }
    }

    val application = getApplication<ChatOwlApplication>()

    private val categoryToolboxData = toolboxRepository.getCategoryToolboxItems(args.category)

    val filteredData = MediatorLiveData<Pair<List<ToolboxItem>, List<ToolboxItem>>>()

    init {

        filteredData.addSource(categoryToolboxData) {
            filteredData.value = filterData()
        }

        filteredData.addSource(searchFilter) {
            filteredData.value = filterData()
        }

        filteredData.addSource(audioFilter) {
            filteredData.value = filterData()
        }

        filteredData.addSource(imageFilter) {
            filteredData.value = filterData()
        }

        filteredData.addSource(quoteFilter) {
            filteredData.value = filterData()
        }

        filteredData.addSource(videoFilter) {
            filteredData.value = filterData()
        }

        filteredData.addSource(lengthShortFilter) {
            filteredData.value = filterData()
        }

        filteredData.addSource(lengthMediumFilter) {
            filteredData.value = filterData()
        }

        filteredData.addSource(lengthLongFilter) {
            filteredData.value = filterData()
        }
    }

    val header = Transformations.map(categoryToolboxData) { category ->
        category.name
    }

    private fun filterData(): Pair<List<ToolboxItem>, List<ToolboxItem>> {
        return categoryToolboxData.value?.let {
            val toolboxToolList =
                filterToolboxItemList(categoryToolboxData.value?.items?.sortedBy { it.id }
                    ?: emptyList())
            val availableToolboxToolList = toolboxToolList.filter { !it.isLocked }
            val upcomingToolboxToolList = toolboxToolList.filter { it.isLocked }
            return Pair(availableToolboxToolList, upcomingToolboxToolList)
        } ?: Pair(emptyList(), emptyList())
    }

    fun onToolClicked(toolboxItem: ToolboxItem) {
        when {
            toolboxItem.isPaywalled -> {
                _navigate.value =
                    ToolboxCategoryFragmentDirections.actionToolboxCategoryToExerciseLocked(
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
                _navigate.value =
                    ToolboxCategoryFragmentDirections.actionToolboxCategoryToExerciseLocked(
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
                    _navigate.value =
                        ToolboxCategoryFragmentDirections.actionToolboxCategoryToExerciseLocked(
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
            toolboxItem.daysUntilActivity > 0 -> {
                val daysText =
                    getRemainingDaysInTrial(toolboxItem.daysUntilActivity, application)
                _navigate.value = ToolboxCategoryFragmentDirections.actionToolboxCategoryToExerciseLocked(
                    ExerciseLockedMessage(
                        application.getString(R.string.locked_exercise_title),
                        application.getString(
                            R.string.exercise_coming_up_message,
                            daysText
                        ),
                        toolboxItem.tool.title,
                        toolboxItem.tool.toolIcon(),
                        false
                    )
                )
            }
            (!toolboxItem.timeLastWatched.isNullOrEmpty()) &&
                    toolboxItem.tool.toolSubtype.equals(ToolType.SESSION.name, true) -> {
                _navigate.value =
                    ToolboxCategoryFragmentDirections.actionToolboxCategoryToChatHistoryFragment(
                        toolboxItem.tool.id,
                        toolboxItem.tool.title
                    )
            }
            else -> {
                fetchTool(toolboxItem)
            }
        }
//        if(toolboxItem.isLocked) {
//            val messageResourceId = if(toolboxItem.tool.toolType.equals(SESSION.name, true)) R.string.locked_session_message
//            else R.string.locked_exercise_message
//            _navigate.value = ToolboxCategoryFragmentDirections.actionToolboxCategoryToMessageDialog(messageResourceId)
//        } else {
//
//        }
    }

    private fun fetchTool(toolboxItem: ToolboxItem) {
        _isLoading.value = true
        viewModelScope.fetch({
            toolboxRepository.getToolboxExercise(toolboxItem.id)
        }, { exercise ->
            navigateToExercise(exercise)
            _isLoading.value = false
        }, {
            _isLoading.value = false
            if (it.isConnectionException()) {
                _navigate.value =
                    ToolboxCategoryFragmentDirections.actionGlobalNoInternetDialog()
            }
        })
    }

    private fun navigateToExercise(exercise: ToolboxExercise) {
        when (exercise.tool.toolSubtype) {
            VIDEO.name.lowerCase(), AUDIO.name.lowerCase() -> {
                _navigate.value =
                    ToolboxCategoryFragmentDirections.actionGlobalMediaExercise(exercise)
                        .setHeader(header.value)
            }
            QUOTE.name.lowerCase() -> {
                _navigate.value =
                    ToolboxCategoryFragmentDirections.actionGlobalQuoteExercise(exercise)
                        .setHeader(header.value)
            }
            IMAGE.name.lowerCase() -> {
                _navigate.value =
                    ToolboxCategoryFragmentDirections.actionGlobalImageExercise(exercise)
                        .setHeader(header.value)
            }
        }
    }
}
