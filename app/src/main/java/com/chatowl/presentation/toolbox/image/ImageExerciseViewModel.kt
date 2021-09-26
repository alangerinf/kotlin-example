package com.chatowl.presentation.toolbox.image

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.chatowl.data.entities.toolbox.ToolSubtype
import com.chatowl.data.entities.toolbox.ToolboxExercise
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.data.entities.toolbox.event.ToolEvent
import com.chatowl.data.entities.toolbox.offline.OfflineEvent
import com.chatowl.data.entities.toolbox.offline.OfflineEventType
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.work.OfflineEventsWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.chatowl.presentation.common.utils.set


class ImageExerciseViewModel(application: Application, args: ImageExerciseFragmentArgs, private val toolboxRepository: ToolboxRepository) : BaseViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val args: ImageExerciseFragmentArgs, private val toolboxRepository: ToolboxRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ImageExerciseViewModel(application, args, toolboxRepository) as T
        }
    }

    val exercise: LiveData<ToolboxExercise> get() = _exercise
    private val _exercise = MutableLiveData<ToolboxExercise>()

    val isChatSource: LiveData<Boolean> get() = _isChatSource
    private val _isChatSource = MutableLiveData<Boolean>()

    val resetAnimation: LiveData<Boolean> get() = _resetAnimation
    private val _resetAnimation = SingleLiveEvent<Boolean>()

    val header: LiveData<String> get() = _header
    private val _header = MutableLiveData<String>()

    init {
        _exercise.value = args.exercise
        _isChatSource.value = args.isChatSource
        _header.value = args.header
        postStartEvent(args.exercise.id)
        setChatToolFinished()
    }

    private fun postStartEvent(toolboxItemId: Int) {
        viewModelScope.fetch({
            toolboxRepository.postEvent(toolboxItemId, ToolEvent.STARTED, null)
        }, {}, {})
    }

    val isFavorite = Transformations.map(exercise) { exercise ->
        exercise.tool.isFavorite
    }

//    val suggestedExercise = Transformations.map(exercise) { exercise ->
//        exercise.suggestedItems?.firstOrNull()
//    }

//    fun onSuggestedExerciseClicked() {
//        suggestedExercise.value?.let { suggestedExercise ->
//            fetchExercise(suggestedExercise.tool.id)
//        }
//    }

    private fun fetchExercise(toolboxItem: ToolboxItem) {
        _isLoading.value = true
        viewModelScope.fetch({
            toolboxRepository.getToolboxExercise(toolboxItem.id)
        }, { exercise ->
            _isLoading.value = false
            navigateToExercise(exercise)
        }, {
            _isLoading.value = false
        })
    }

    private fun navigateToExercise(exercise: ToolboxExercise) {
        when (exercise.tool.toolSubtype) {
            ToolSubtype.VIDEO.name.lowerCase(), ToolSubtype.AUDIO.name.lowerCase() -> {
                val action =
                    ImageExerciseFragmentDirections.actionGlobalMediaExercise(exercise)
                        .setHeader(header.value)
                _navigate.value = action
            }
            ToolSubtype.QUOTE.name.lowerCase() -> {
                _navigate.value =
                    ImageExerciseFragmentDirections.actionGlobalQuoteExercise(exercise)
                        .setHeader(header.value)
            }
            ToolSubtype.IMAGE.name.lowerCase() -> {
                _exercise.value = exercise
                _resetAnimation.value = true
            }
        }
    }

    fun onFavoriteClicked(saveFavorite: Boolean) {
        exercise.value?.let { exercise ->
            _exercise.value = exercise.copy(
                tool = exercise.tool.copy(isFavorite = isFavorite.value?.not() ?: false)
            )
            saveAsFavorite(saveFavorite, exercise.tool.id)
        }
    }

    private fun saveAsFavorite(add: Boolean, toolId: Int) {
        viewModelScope.fetch({
            toolboxRepository.setAsFavorite(add, toolId)
        }, {
            Log.d("Success", "success")
        }, {
            it.printStackTrace()
            val eventTypeString = if (add) OfflineEventType.EXERCISE_SAVE_FAVORITE else OfflineEventType.EXERCISE_REMOVE_FAVORITE
            scheduleEvent(OfflineEvent(type = eventTypeString.name.lowerCase(), toolId = toolId))
        })
    }

    private fun scheduleEvent(offlineEvent: OfflineEvent) {
        viewModelScope.launch {
            toolboxRepository.insertOfflineEvent(offlineEvent)
            scheduleOfflineEventsWorker()
        }
    }

    private fun scheduleOfflineEventsWorker() {
        val workManager = WorkManager.getInstance(getApplication())

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val offlineEventsWorkRequest = OneTimeWorkRequestBuilder<OfflineEventsWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OfflineEventsWorker.WORKER_BACKOFF_DELAY_IN_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        workManager.enqueueUniqueWork(
            OfflineEventsWorker.OFFLINE_EVENTS_WORKER_NAME,
            ExistingWorkPolicy.KEEP,
            offlineEventsWorkRequest
        )
    }

    private fun setChatToolFinished() {
        if(isChatSource.value == true) {
            PreferenceHelper.getChatOwlPreferences().set(
                PreferenceHelper.Key.CHAT_TOOL_STATUS,
                ToolEvent.FINISHED.name
            )
        }
    }
}
