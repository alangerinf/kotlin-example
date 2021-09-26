package com.chatowl.presentation.toolbox.mediaexercise

import android.app.Application
import android.app.DownloadManager
import android.app.DownloadManager.STATUS_FAILED
import android.app.DownloadManager.STATUS_SUCCESSFUL
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import androidx.work.*
import com.chatowl.R
import com.chatowl.data.entities.toolbox.MediaType
import com.chatowl.data.entities.toolbox.ToolSubtype
import com.chatowl.data.entities.toolbox.ToolSubtype.*
import com.chatowl.data.entities.toolbox.ExerciseInstruction
import com.chatowl.data.entities.toolbox.ToolboxExercise
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.data.entities.toolbox.event.ToolEvent
import com.chatowl.data.entities.toolbox.event.ToolEvent.*
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.data.entities.toolbox.offline.DownloadManagerIdRegistry
import com.chatowl.data.entities.toolbox.offline.OfflineEvent
import com.chatowl.data.entities.toolbox.offline.OfflineEventType.*
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.extensions.secondsToRepeatLimitDescriptiveString
import com.chatowl.presentation.common.extensions.upperCase
import com.chatowl.presentation.common.utils.*
import com.chatowl.presentation.common.utils.PreferenceHelper.Key
import com.chatowl.work.OfflineEventsWorker
import com.chatowl.work.OfflineEventsWorker.Companion.OFFLINE_EVENTS_WORKER_NAME
import com.chatowl.work.OfflineEventsWorker.Companion.WORKER_BACKOFF_DELAY_IN_MINUTES
import kotlinx.coroutines.launch
import java.io.File
import com.chatowl.presentation.common.extensions.*
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.set
import com.instabug.library.logging.InstabugLog
import java.util.*
import java.util.concurrent.TimeUnit


class MediaExerciseViewModel(
    application: Application,
    val args: MediaExerciseFragmentArgs,
    private val toolboxRepository: ToolboxRepository
) : BaseViewModel(application), LifecycleObserver {
    val TAG = this::class.java.simpleName
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val args: MediaExerciseFragmentArgs,
        private val toolboxRepository: ToolboxRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaExerciseViewModel(application, args, toolboxRepository) as T
        }
    }

    val application = getApplication<ChatOwlApplication>()

    val exercise: LiveData<ToolboxExercise> get() = _exercise
    private val _exercise = MutableLiveData<ToolboxExercise>()

    val done: LiveData<Boolean> get() = _done
    private val _done = MutableLiveData<Boolean>()

    val showRateExercise: LiveData<Pair<Int, Boolean>> get() = _showRateExercise
    private val _showRateExercise = MutableLiveData(Pair(-1,false))

    val fullscreenPlayer: LiveData<FullscreenPlayerData> get() = _fullscreenPlayer
    private val _fullscreenPlayer = SingleLiveEvent<FullscreenPlayerData>()

    val isChatSource: LiveData<Boolean> get() = _isChatSource
    private val _isChatSource = MutableLiveData<Boolean>()

    val header: LiveData<String> get() = _header
    private val _header = MutableLiveData<String>()

    private var firsLifecycle = true

    private lateinit var downloadManager: DownloadManager
    private lateinit var toolMediaFile: File

    val isDownloaded: LiveData<Boolean> get() = _isDownloaded
    private val _isDownloaded = MutableLiveData<Boolean>()

    val isDownloading: LiveData<Boolean> get() = _isDownloading
    private val _isDownloading = MutableLiveData<Boolean>()

    init {
        _exercise.value = args.exercise
        _isChatSource.value = args.isChatSource
        _header.value = args.header
        initDownloadState()
    }

    private fun initDownloadState() {
        // Init download manager
        downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        exercise.value?.tool?.let { tool ->
            // Set destination path for possible download
            toolMediaFile = File(
                application.getExternalFilesDir(File.pathSeparator + "Downloads" + File.pathSeparator),
                tool.id.toString() + MediaType.valueOf(tool.mediaType!!.upperCase()).fileExtension
            )
            // Check if the file exists
            _isDownloaded.value = toolMediaFile.exists()
            // Get list of last known ongoing downloads
            val currentRegistry = PreferenceHelper.getChatOwlPreferences()
                .get(Key.DOWNLOAD_MANAGER_ID_REGISTRY, DownloadManagerIdRegistry())
                ?: DownloadManagerIdRegistry()
            // Check if the current tool id has an ongoing download
            _isDownloading.value = currentRegistry.toolDownloadIds[tool.id]?.let { downloadId ->
                val status = getDownloadStatus(downloadId)
                if (status != STATUS_FAILED && status != STATUS_SUCCESSFUL) {
                    true
                } else {
                    currentRegistry.toolDownloadIds.remove(tool.id)
                    PreferenceHelper.getChatOwlPreferences()
                        .set(Key.DOWNLOAD_MANAGER_ID_REGISTRY, currentRegistry)
                    false
                }
            } ?: false
        }
        setChatToolStarted()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        if (firsLifecycle) {
            firsLifecycle = false
        } else {
            exercise.value?.let { exercise ->
                fetchExercise(exercise.id, false) { fetchedExercise ->
                    _exercise.value = fetchedExercise
                }
            }
        }
    }

    private fun fetchExercise(
        toolboxItemId: Int,
        showLoader: Boolean = true,
        onSuccess: (ToolboxExercise) -> Unit
    ) {
        _isLoading.value = showLoader
        viewModelScope.fetch({
            toolboxRepository.getToolboxExercise(toolboxItemId)
        }, { exercise ->
            _isLoading.value = false
            onSuccess.invoke(exercise)
        }, {
            _isLoading.value = false
        })
    }

    val previewIconResourceId = Transformations.map(exercise) { exercise ->
        when {
            ToolSubtype.valueOf(exercise.tool.toolSubtype.upperCase()) == AUDIO -> R.drawable.ic_audio_circle
            else -> R.drawable.ic_play_circle
        }
    }

    val isFavorite = Transformations.map(exercise) { exercise ->
        exercise.tool.isFavorite
    }

    val status = Transformations.map(exercise) {
        Triple(it.isPaywalled, it.isLocked, !it.calculatedWithinRepeatLimit())
    }

    val statistics = Transformations.map(exercise) {
        Triple(it.tool.toolSubtype.capitalize(Locale.getDefault()), it.tool.duration, it.watchCount)
    }

    private val watchPosition = Transformations.map(exercise) { exercise ->
        if (!exercise.calculatedWithinRepeatLimit()) {
            0
        } else {
            InstabugLog.d("MediaExerciseFragment -> watchPosition: ${exercise.watchPosition}")
            exercise.watchPosition
        }
    }

    val progressBarInformation =
        watchPosition.combineWithNotNull(statistics) { watchPosition, statistics ->
            Pair(statistics.second ?: 0, watchPosition.toInt())
        }

    val title = Transformations.map(exercise) { exercise ->
        exercise.tool.title
    }

    val description = Transformations.map(exercise) {
        when {
            it.isPaywalled -> {
                application.getString(R.string.exercise_paywalled_message)
            }
            it.isLocked -> {
                application.getString(R.string.exercise_locked_message)
            }
            !it.calculatedWithinRepeatLimit() -> {
                val repeatLimitInSeconds = it.tool.repeatLimit ?: 0
                val repeatLimitMessage =
                    repeatLimitInSeconds.secondsToRepeatLimitDescriptiveString(application)
                val timeLeftDescription = getNextAvailableTime(
                    application,
                    repeatLimitInSeconds, it.timeLastWatched
                        ?: ""
                )
                val descriptionSpan = SpannableString(
                    application.getString(
                        R.string.format_repeat_limit_left_message,
                        repeatLimitMessage,
                        timeLeftDescription
                    )
                )
                descriptionSpan.setSpan(
                    StyleSpan(Typeface.BOLD),
                    descriptionSpan.indexOf(timeLeftDescription),
                    descriptionSpan.indexOf(timeLeftDescription) + timeLeftDescription.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                descriptionSpan
            }
            else -> {
                it.tool.description
            }
        }
    }

    val backgroundUrl = Transformations.map(exercise) { exercise ->
        exercise.tool.thumbnailUrl
    }

    val mediaTypeResourceId: LiveData<Int> = Transformations.map(exercise) { exercise ->
        exercise.tool.mediaType?.let { mediaType ->
            MediaType.valueOf(mediaType.upperCase()).typeDrawableResourceId
        }
    }

    val instructions: LiveData<Triple<Boolean, Boolean, Boolean>> =
        Transformations.map(exercise) { exercise ->
            Triple(
                exercise.tool.instructions?.contains(ExerciseInstruction.COMFORTABLE.name.lowerCase()) == true,
                exercise.tool.instructions?.contains(ExerciseInstruction.DIM_LIGHTS.name.lowerCase()) == true,
                exercise.tool.instructions?.contains(ExerciseInstruction.HEADPHONES.name.lowerCase()) == true
            )
        }

    val pendingExercise: LiveData<ToolboxItem> = Transformations.map(exercise) { exercise ->
        exercise.pendingItem
    }

//    val relatedToolList: LiveData<List<ToolboxItem>> = Transformations.map(exercise) { exercise ->
//        exercise.suggestedItems
//    }

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
        }, {}, {
            it.printStackTrace()
            val eventType = if (add) EXERCISE_SAVE_FAVORITE else EXERCISE_REMOVE_FAVORITE
            scheduleEvent(OfflineEvent(type = eventType.name.lowerCase(), toolId = toolId))
        })
    }

//    fun onSuggestedToolClicked(item: ToolboxItem) {
//        fetchExercise(item.tool.id) { exercise -> navigateToExercise(exercise) }
//    }

    fun onMandatoryToolClicked() {
        exercise.value?.pendingItem?.let { pendingExercise ->
            fetchExercise(pendingExercise.id) { exercise -> navigateToExercise(exercise) }
        }
    }

    fun onDownloadClicked() {
        exercise.value?.tool?.let { tool ->
            if (toolMediaFile.exists()) {
                val deleteSuccess = toolMediaFile.delete()
                _isDownloaded.value = !deleteSuccess //change the downloaded status image
                if (deleteSuccess) {
                    _isDownloading.value = false
                }
            } else {
                if (_isDownloading.value == false) {
                    startDownload(tool.id)
                    _isDownloading.value = true
                }
            }
        }
    }

    private fun navigateToExercise(exercise: ToolboxExercise) {
        when (exercise.tool.toolSubtype) {
            VIDEO.name.lowerCase(), AUDIO.name.lowerCase() -> {
                _done.value = false
                _exercise.value = exercise
            }
            QUOTE.name.lowerCase() -> {
                _navigate.value =
                    MediaExerciseFragmentDirections.actionGlobalQuoteExercise(exercise)
                        .setHeader(header.value)
            }
            IMAGE.name.lowerCase() -> {
                _navigate.value =
                    MediaExerciseFragmentDirections.actionGlobalImageExercise(exercise)
                        .setHeader(header.value)
            }
        }
    }

    fun onPlayClicked() {
        status.value?.let { status ->
            if (!status.first && !status.second && !status.third) {
                exercise.value?.let { exercise ->
                    postEvent(
                        if (exercise.watchPosition == 0L) STARTED else RESUMED
                    )

                    val playerUri =
                        if (toolMediaFile.exists()) Uri.fromFile(toolMediaFile) else Uri.parse(
                            exercise.tool.mediaLandscapeUrl ?: ""
                        )

                    _fullscreenPlayer.value = FullscreenPlayerData(
                        exercise.tool.title,
                        exercise.tool.imageLandscapeUrl ?: "",
                        playerUri,
                        exercise.watchPosition
                    )
                    // reset lifecycle flag so when the player is closed, the exercise is not
                    // re-fetched before the update event is sent to the backend
                    firsLifecycle = true
                }
            }
        }
    }


    fun checkIfShouldRate() {
        viewModelScope.fetch({
            toolboxRepository.checkFeedbackToolAvailability(args.exercise.toolId)
        },{
            Log.i(TAG, Pair(args.exercise.toolId, it).toString())
          _showRateExercise.value = Pair(args.exercise.toolId, it)
        },{
            Log.e(TAG, it.toString())
        })
    }

    fun onPlayStopped(position: Long) {
        if (position == Long.MAX_VALUE) {
            _done.value = true
            checkIfShouldRate()
            _exercise.value = exercise.value?.copy(watchPosition = 0)
            setChatToolFinished()
            postEvent(FINISHED, 0)
        } else {
            postEvent(PAUSED, position)
        }
    }

    private fun postEvent(toolEvent: ToolEvent, position: Long? = null) {
        viewModelScope.fetch({
            toolboxRepository.postEvent(
                exercise.value?.id ?: -1,
                toolEvent,
                position
            )
        }, {
            exercise.value?.let { exercise ->
                fetchExercise(exercise.id, false) { fetchedExercise ->
                    _exercise.value = fetchedExercise
                }
            }
        }, {
            persistExerciseEvent(toolEvent, position)
        })
    }

    private fun persistExerciseEvent(
        toolEvent: ToolEvent,
        position: Long? = null
    ) {
        viewModelScope.fetch({
            exercise.value?.let { exercise ->
                when (toolEvent) {
                    STARTED -> {
                        // Schedule worker
                        scheduleEvent(
                            OfflineEvent(
                                type = EXERCISE_STARTED.name.lowerCase(),
                                toolId = exercise.tool.id,
                                payload = null
                            )
                        )
                    }
                    PAUSED -> {
                        // Persist event in local database
                        toolboxRepository.updateExerciseWatchPosition(exercise.id, position)
                        // Schedule worker
                        scheduleEvent(
                            OfflineEvent(
                                type = EXERCISE_PAUSED.name.lowerCase(),
                                toolId = exercise.tool.id,
                                payload = position?.toString()
                            )
                        )
                    }
                    RESUMED -> {
                        // Schedule worker
                        scheduleEvent(
                            OfflineEvent(
                                type = RESUMED.name.lowerCase(),
                                toolId = exercise.tool.id,
                                payload = null
                            )
                        )
                    }
                    FINISHED -> {
                        // Persist event in local database
                        toolboxRepository.updateExerciseWatchPosition(exercise.id, position)
                        // Calculate and persist new within repeat limit information
                        exercise.tool.repeatLimit?.let { repeatLimit ->
                            // Now is the new time last watched
                            val calendar = Calendar.getInstance()
                            val now = calendar.time
                            val newTimeLastWatchedTimestamp = getStringTimestampFromDate(date = now)
                            // Add the repeat limit field to current moment
                            calendar.add(Calendar.SECOND, repeatLimit)
                            val nextAvailableTime = calendar.time
                            // Check the new status, should be always false
                            val newWithinRepeatLimit = nextAvailableTime.before(now)
                            // Update status in local persistence
                            toolboxRepository.persistRepeatInformation(
                                exercise.id,
                                newTimeLastWatchedTimestamp,
                                newWithinRepeatLimit
                            )
                        }
                        // Schedule worker
                        scheduleEvent(
                            OfflineEvent(
                                type = EXERCISE_FINISHED.name.lowerCase(),
                                toolId = exercise.tool.id,
                                payload = null
                            )
                        )
                    }
                }
            }
        }, {
            // Reload
            exercise.value?.let { exercise ->
                fetchExercise(exercise.id, false) { fetchedExercise ->
                    _exercise.value = fetchedExercise
                }
            }
        }, {
            it.printStackTrace()
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
                WORKER_BACKOFF_DELAY_IN_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        workManager.enqueueUniqueWork(
            OFFLINE_EVENTS_WORKER_NAME,
            ExistingWorkPolicy.KEEP,
            offlineEventsWorkRequest
        )
    }

    private fun startDownload(toolId: Int) {
        val uri: Uri = Uri.parse(exercise.value?.tool?.mediaLandscapeUrl)

        val request = DownloadManager.Request(uri)
            .setTitle(exercise.value?.tool?.title)
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setDestinationUri(toolMediaFile.toUri())

        // Get list of last known ongoing downloads
        val currentRegistry = PreferenceHelper.getChatOwlPreferences()
            .get(Key.DOWNLOAD_MANAGER_ID_REGISTRY, DownloadManagerIdRegistry())
            ?: DownloadManagerIdRegistry()
        // Add download id for current tool
        currentRegistry.toolDownloadIds[toolId] = downloadManager.enqueue(request)
        PreferenceHelper.getChatOwlPreferences()
            .set(Key.DOWNLOAD_MANAGER_ID_REGISTRY, currentRegistry)
    }

    fun broadcastReceived(downloadId: Long) {
        exercise.value?.tool?.let { tool ->
            // Get list of last known ongoing downloads
            val currentRegistry = PreferenceHelper.getChatOwlPreferences()
                .get(Key.DOWNLOAD_MANAGER_ID_REGISTRY, DownloadManagerIdRegistry())
                ?: DownloadManagerIdRegistry()
            // Check if the completed download belongs to this tool in particular
            if (currentRegistry.toolDownloadIds[tool.id] == downloadId) {
                _isDownloading.value = false
                _isDownloaded.value = toolMediaFile.exists()
            }
            // Either way, remove the download id from the registry
            currentRegistry.toolDownloadIds.remove(tool.id)
            PreferenceHelper.getChatOwlPreferences()
                .set(Key.DOWNLOAD_MANAGER_ID_REGISTRY, currentRegistry)
        }
    }

    private fun getDownloadStatus(id: Long): Int? {
        try {
            val query = DownloadManager.Query().setFilterById(id)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                cursor.close()
                return status
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun setChatToolStarted() {
        if (isChatSource.value == true) {
            PreferenceHelper.getChatOwlPreferences().set(
                Key.CHAT_TOOL_STATUS,
                STARTED.name
            )
        }
    }

    private fun setChatToolFinished() {
        if (isChatSource.value == true) {
            PreferenceHelper.getChatOwlPreferences().set(
                Key.CHAT_TOOL_STATUS,
                FINISHED.name
            )
        }
    }
}
