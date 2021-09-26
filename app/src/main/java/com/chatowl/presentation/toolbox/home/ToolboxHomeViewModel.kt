package com.chatowl.presentation.toolbox.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.entities.toolbox.ToolboxCategoryType.*
import com.chatowl.data.entities.toolbox.categorydetail.ToolboxCategoryDetail
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroup
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroupItem
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.ToolboxViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.*
import com.chatowl.presentation.common.utils.getNextAvailableTime
import com.chatowl.presentation.common.utils.getRemainingDaysInTrial
import com.chatowl.presentation.home.HomeFragmentDirections
import com.chatowl.presentation.toolbox.host.ToolboxHostFragmentDirections


class ToolboxHomeViewModel(
    application: Application,
    private val toolboxRepository: ToolboxRepository
) : ToolboxViewModel(application), LifecycleObserver {

    private val TAG = this::class.java.simpleName

    companion object {
        private const val FAVORITE_GROUP_ID = -2
        private const val PROGRAM_GROUP_ID = -1

        private const val GROUP_MINIMUM = 0
        private const val GROUP_MAXIMUM = 4
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val toolboxRepository: ToolboxRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToolboxHomeViewModel(application, toolboxRepository) as T
        }
    }

    val isLoadingPlanOrCategory: LiveData<Boolean> get() = _isLoadingPlanOrCategory
    private val _isLoadingPlanOrCategory = MutableLiveData(false)

    val application = getApplication<ChatOwlApplication>()

    private val programs = toolboxRepository.getPrograms()

    private val categories = toolboxRepository.getCategories()

    val filteredGroups = MediatorLiveData<List<ToolboxHomeGroup>>()

    init {
        filteredGroups.addSource(programs) {
            filteredGroups.value = filterGroups()
        }

        filteredGroups.addSource(categories) {
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        fetchToolboxData()
    }

    private fun fetchToolboxData() {
        _isLoading.value = true
        viewModelScope.fetch({
            toolboxRepository.refreshHome()
        }, {
            _isLoading.value = false
        }, {
            Log.e(TAG, it.toString())
            it.printStackTrace()
            _isLoading.value = false
        })
    }

    private fun filterGroups(): List<ToolboxHomeGroup> {

        val groups = mutableListOf<ToolboxHomeGroup>()

        // Favorites group
        val allFavoriteExercises = mutableListOf<ToolboxItem>()
        // Categories
        categories.value?.forEach { category ->
            allFavoriteExercises.addAll(category.exercises
                .filter { it.tool.isFavorite })
            if (category.exercises.size > GROUP_MINIMUM) {
                val filteredCategoryExercises = filterToolboxItemList(category.exercises)
                if (filteredCategoryExercises.isNotEmpty()) {
                    var categoryExercises =
                        mutableListOf<ToolboxHomeGroupItem>(*filteredCategoryExercises.map {
                            ToolboxHomeGroupItem.ToolboxHomeToolboxItem(
                                toolboxItem = it,
                                parentName = category.name
                            )
                        }.toTypedArray())
                    if (categoryExercises.size > GROUP_MAXIMUM) {
                        categoryExercises = categoryExercises.take(GROUP_MAXIMUM).toMutableList()
                        categoryExercises.add(
                            ToolboxHomeGroupItem.ToolboxHomeShowMore()
                        )
                    }
                    groups.add(
                        ToolboxHomeGroup(
                            id = category.id,
                            title = category.name,
                            groupName = category.name,
                            groupType = CATEGORY,
                            items = categoryExercises
                        )
                    )
                }
            }
        }
        // Programs exercises
        val allProgramExercises = mutableListOf<ToolboxItem>()
        programs.value?.forEach { program ->
            allFavoriteExercises.addAll(program.exercises
                .filter { it.tool.isFavorite })
            allProgramExercises.addAll(program.exercises)
        }

        val filteredProgramExercises = filterToolboxItemList(allProgramExercises)
        if (filteredProgramExercises.isNotEmpty()) {
            var programExercises =
                mutableListOf<ToolboxHomeGroupItem>(*filteredProgramExercises.map {
                    ToolboxHomeGroupItem.ToolboxHomeToolboxItem(
                        toolboxItem = it,
                        parentName = application.getString(R.string.my_therapy_exercises)
                    )
                }.toTypedArray())

            val parentName = programs.value?.let {
                if (it.size > 1) application.getString(R.string.my_therapy_exercises) else it.first().name
            } ?: application.getString(R.string.my_therapy_exercises)

            if (programExercises.size > GROUP_MAXIMUM) {
                programExercises = programExercises.take(GROUP_MAXIMUM).toMutableList()
                programExercises.add(
                    ToolboxHomeGroupItem.ToolboxHomeShowMore()
                )
            }
            groups.add(
                0,
                ToolboxHomeGroup(
                    id = PROGRAM_GROUP_ID,
                    title = application.getString(R.string.my_therapy_exercises),
                    groupName = parentName,
                    groupType = PROGRAM,
                    items = programExercises
                )
            )
        }

        val filteredFavorites = filterToolboxItemList(allFavoriteExercises.distinctBy { it.id })
        if (filteredFavorites.isNotEmpty()) {
            var favoriteExercises = mutableListOf<ToolboxHomeGroupItem>(*filteredFavorites.map {
                ToolboxHomeGroupItem.ToolboxHomeToolboxItem(
                    toolboxItem = it,
                    parentName = application.getString(R.string.favorites)
                )
            }.toTypedArray())
            if (favoriteExercises.size > GROUP_MAXIMUM) {
                favoriteExercises = favoriteExercises.take(GROUP_MAXIMUM).toMutableList()
                favoriteExercises.add(
                    ToolboxHomeGroupItem.ToolboxHomeShowMore()
                )
            }
            groups.add(
                0,
                ToolboxHomeGroup(
                    id = FAVORITE_GROUP_ID,
                    title = application.getString(R.string.favorites),
                    groupName = application.getString(R.string.favorites),
                    groupType = FAVORITES,
                    items = favoriteExercises
                )
            )
        }

        return groups
    }

    fun onGroupClicked(toolboxHomeGroup: ToolboxHomeGroup) {
        when (toolboxHomeGroup.groupType) {
            PROGRAM -> {
                programs.value?.let { programs ->
                    if (programs.size > 1) {
                        _navigate.value =
                            ToolboxHostFragmentDirections.actionToolboxHostToToolboxPlans()
                    } else {
                        _navigate.value =
                            ToolboxHostFragmentDirections.actionToolboxHostToToolboxCategory(
                                ToolboxCategoryDetail(
                                    id = programs.first().id,
                                    name = toolboxHomeGroup.title,
                                    type = toolboxHomeGroup.groupType.name
                                )
                            )
                    }
                }
            }
            CATEGORY -> {
                _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToToolboxCategory(
                    ToolboxCategoryDetail(
                        id = toolboxHomeGroup.id,
                        name = toolboxHomeGroup.groupName,
                        type = toolboxHomeGroup.groupType.name
                    )
                )
            }
            FAVORITES -> {
                _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToToolboxCategory(
                    ToolboxCategoryDetail(
                        id = toolboxHomeGroup.id,
                        name = toolboxHomeGroup.groupName,
                        type = toolboxHomeGroup.groupType.name
                    )
                )
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
                _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToExerciseLocked(
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
                        ToolboxHostFragmentDirections.actionToolboxHostToExerciseLocked(
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
                _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToExerciseLocked(
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
            (toolboxItem.timeLastWatched != null) &&
            toolboxItem.tool.toolSubtype.equals(ToolType.SESSION.name, true) -> {
                _navigate.value = ToolboxHostFragmentDirections.actionToolboxHostToChatHistoryFragment(toolboxItem.tool.id, toolboxItem.tool.title)
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
                _navigate.value = ToolboxHostFragmentDirections.actionGlobalNoInternetDialog()
            }
        })
    }

    private fun navigateToExercise(exercise: ToolboxExercise, header: String) {
        when (exercise.tool.toolSubtype) {
            ToolSubtype.VIDEO.name.lowerCase(), ToolSubtype.AUDIO.name.lowerCase() -> {
                _navigate.value =
                    ToolboxHostFragmentDirections.actionGlobalMediaExercise(exercise)
                        .setHeader(header)
            }
            ToolSubtype.QUOTE.name.lowerCase() -> {
                _navigate.value =
                    ToolboxHostFragmentDirections.actionGlobalQuoteExercise(exercise)
                        .setHeader(header)
            }
            ToolSubtype.IMAGE.name.lowerCase() -> {
                _navigate.value =
                    ToolboxHostFragmentDirections.actionGlobalImageExercise(exercise)
                        .setHeader(header)
            }
        }
    }

    val isLoadingAndEmpty = isLoading.combineWith(filteredGroups) { isLoading, groups ->
        isLoading == true && groups.isNullOrEmpty()
    }
}
