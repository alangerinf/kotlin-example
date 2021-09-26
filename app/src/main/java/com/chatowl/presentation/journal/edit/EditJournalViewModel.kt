package com.chatowl.presentation.journal.edit

import android.app.Application
import android.media.MediaRecorder
import android.net.Uri
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.entities.journal.*
import com.chatowl.data.entities.journal.gallery.GalleryItem
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.data.repositories.JournalRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.combineWith
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.MONTH_DAY_COMMA_YEAR_FORMAT
import com.chatowl.presentation.common.utils.SERVER_FORMAT
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.changeDateFormat
import com.chatowl.presentation.journal.entryitem.EntryAdapterItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.net.URLConnection
import java.nio.file.Files.exists
import kotlin.random.Random

class EditJournalViewModel(
    application: Application,
    private val journalRepository: JournalRepository,
    journalToEdit: Journal?,
    isEdit: Boolean
) : BaseViewModel(application),
    LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val journalRepository: JournalRepository,
        private val journalToEdit: Journal?,
        private val isEdit: Boolean
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditJournalViewModel(application, journalRepository, journalToEdit, isEdit) as T
        }
    }

    companion object {
        const val RECORDING_LIMIT = 120000L
        const val COUNTDOWN_INTERVAL = 1000L
    }

    val journal: LiveData<Journal> get() = _journal
    private val _journal = MutableLiveData<Journal>()

    val isRecording: LiveData<Boolean> get() = _isRecording
    private val _isRecording = MutableLiveData(false)

    val showTooltip: LiveData<Boolean> get() = _showTooltip
    private val _showTooltip = MutableLiveData(false)

    val isUploading: LiveData<Boolean> get() = _isUploading
    private val _isUploading = MutableLiveData<Boolean>()

    val recordingLength: LiveData<Int> get() = _recordingLength
    private val _recordingLength = MutableLiveData<Int>()

    val errorMessage: LiveData<Int> get() = _errorMessage
    private val _errorMessage = SingleLiveEvent<Int>()

    val toggleEdit: LiveData<Boolean> get() = _toggleEdit
    private val _toggleEdit = MutableLiveData<Boolean>()

    private val isKeyboardVisible: LiveData<Boolean> get() = _isKeyboardVisible
    private val _isKeyboardVisible = MutableLiveData(false)

    val fullscreenPlayer: LiveData<FullscreenPlayerData> get() = _fullscreenPlayer
    private val _fullscreenPlayer = SingleLiveEvent<FullscreenPlayerData>()

    val dismiss: LiveData<Boolean> get() = _dismiss
    private val _dismiss = SingleLiveEvent<Boolean>()

    // Helper view content
    val helperText: LiveData<String> get() = _helperText
    private val _helperText = MutableLiveData<String>()

    // Audio recording
    var recorder: MediaRecorder? = null
    var recordFile: File? = null

    private val recordingTimer = object : CountDownTimer(RECORDING_LIMIT, COUNTDOWN_INTERVAL) {

        override fun onTick(millisUntilFinished: Long) {
            _recordingLength.value = (RECORDING_LIMIT - millisUntilFinished)
                .div(1000)
                .coerceIn(0, RECORDING_LIMIT)
                .toInt()
        }

        override fun onFinish() {
            onSendRecordingClick()
        }
    }

    init {
        journalToEdit?.let {
            _journal.value = it
        } ?: run {
            fetchDraft()
        }
        _toggleEdit.value = isEdit
    }

    private fun fetchDraft() {
        viewModelScope.fetch({
            journalRepository.getJournalById(DRAFT_ID)
        }, {
            _journal.value = it
        }, {})
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        onCancelRecordingClick()
    }

    val header = Transformations.map(journal) { journal ->
        changeDateFormat(journal.date, SERVER_FORMAT, MONTH_DAY_COMMA_YEAR_FORMAT)
    }

    private val domainJournalItems = Transformations.map(journal) { journal ->
        journal.components
    }

    val adapterJournalItems = Transformations.map(domainJournalItems) { list ->
        list.asAdapterItemList()
    }

    val showSaveLayout = toggleEdit.combineWith(isKeyboardVisible) { isEdit, isKeyboardVisible ->
        isEdit == true && isKeyboardVisible == false
    }

    fun toggleEditMode() {1
        _toggleEdit.value = toggleEdit.value?.not() ?: true
    }

    fun viewFullscreen(journalItemId: Int) {
        domainJournalItems.value?.find { it.id == journalItemId }?.let { journalItem ->
            when (EntryItemType.valueOf(journalItem.type.toUpperCase())) {
                EntryItemType.IMAGE -> {
                    _navigate.value =
                        EditJournalFragmentDirections.actionJournalEditToImageFullscreen(
                            journalItem.uri ?:journalItem.mediaUrl?: ""
                        )
                }
                EntryItemType.VIDEO -> {
                    _fullscreenPlayer.value = FullscreenPlayerData(
                        mediaUri = Uri.parse(journalItem.uri?:journalItem.mediaUrl?: ""),
                        fixedLandscape = false
                    )
                }
                else -> {

                }
            }
        }
    }

    fun onNewTextEntry() {
        addCurrentHelperText()
    }

    fun appendJournalTextEntry(text: String) {
        appendJournalItem(
            EntryItem(
                content = text,
                type = EntryItemType.TEXT.name.lowerCase()
            )
        )
    }

    private fun addCurrentHelperText() {
        helperText.value?.let { text ->
            if (text.isNotEmpty()) {
                journal.value?.let {
                    if (it.components.isNotEmpty() && it.components.last().type == EntryItemType.TEXT.name.toLowerCase()) {
                        it.components.last().content = it.components.last().content+" "+text
                        _journal.value = it
                    } else {
                        appendJournalTextEntry(text)
                    }
                }?: run {
                    appendJournalTextEntry(text)
                }
                _helperText.value = ""
            }
        }
    }

    fun removeItem(position: Int) {
        val currentJournalItems = journal.value?.components?.toMutableList() ?: mutableListOf()
        currentJournalItems.removeAt(position)
        _journal.value = journal.value?.copy(components = currentJournalItems)
    }
    //aca guarda... ver por le guarda con ID = 0, aunque el 0 es en el EntryItemAdapter
    private fun appendJournalItem(entryItem: EntryItem) {
        val currentJournalItems = journal.value?.components?.toMutableList() ?: mutableListOf()
        entryItem.order = currentJournalItems.size
        //entryItem.id = entryItem.id
        currentJournalItems.add(entryItem)
        _journal.value = journal.value?.copy(components = currentJournalItems)
    }

    fun onTextContentChanged(position: Int, id: Int, newContent: String) {
        val currentJournalItems = journal.value?.components?.toMutableList() ?: mutableListOf()
        val item = currentJournalItems[position]
        item.apply {
            if (newContent != content) {
                content = newContent
                _journal.value = journal.value?.copy(components = currentJournalItems)
            }
        }
    }

    fun onHelperViewTextChanged(text: String) {
        _helperText.value = text
    }

    fun onCameraContentCaptured(sourceUri: Uri) {
        onGalleryContentSelected(
            GalleryItem(
                -1,
                sourceUri.lastPathSegment ?: "${System.currentTimeMillis()}.jpg",
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                "image/jpeg",
                sourceUri
            )
        )
    }

    fun onVideoContentCaptured(sourceUri: Uri) {
        onGalleryContentSelected(
            GalleryItem(
                id =-1,
                displayName = sourceUri.lastPathSegment ?: "${System.currentTimeMillis()}.mp4",
                mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                mimeType = "video/mpeg",
                contentUri = sourceUri
            )
        )
    }

    fun onGalleryContentSelected(galleryItem: GalleryItem) {
        _isUploading.value = true
        Log.d("Gallery", galleryItem.toString())
        viewModelScope.fetch({
            val inputStream: InputStream? =
                getApplication<ChatOwlApplication>().contentResolver.openInputStream(galleryItem.contentUri)
            val content = inputStream?.readBytes()
            val requestFile =
                content!!.toRequestBody(galleryItem.mimeType.toMediaTypeOrNull(), 0, content.size)
            val body =
                MultipartBody.Part.createFormData("mediaFile", galleryItem.displayName, requestFile)
            ChatOwlAPIClient.uploadFile(body).data
        }, {
            appendJournalItem(
                EntryItem(
                    id = Random.nextInt(Int.MAX_VALUE),
                    mediaUrl = it.first(),
                    type = if (galleryItem.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        EntryItemType.VIDEO.name.lowerCase()
                    } else {
                        EntryItemType.IMAGE.name.lowerCase()
                    },
                    uri = galleryItem.contentUri.toString()
                )
            )
            _isUploading.value = false
        }, {
            appendJournalItem(
                EntryItem(
                    id = Random.nextInt(Int.MAX_VALUE),
                    type = if (galleryItem.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        EntryItemType.VIDEO.name.lowerCase()
                    } else {
                        EntryItemType.IMAGE.name.lowerCase()
                    },
                    uri = galleryItem.contentUri.toString()
                )
            )
            _isUploading.value = false
            it.printStackTrace()
        })
    }

    fun startRecording(file: File) {
        Log.d("MediaRecorder", "startRecording")
        recordFile = file
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(FileOutputStream(file).fd)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                _isRecording.value = true
                recordingTimer.start()
            } catch (e: IOException) {
                _isRecording.value = false
                _errorMessage.value = R.string.error
                Log.e("MediaRecorder", e.message.toString())
            }

            start()
        }
    }

    fun onSendRecordingClick() {
        stopRecording()
        sendRecording()
    }

    private fun sendRecording() {
        _isUploading.value = true
        val inputStream: InputStream? =
            getApplication<ChatOwlApplication>().contentResolver.openInputStream(recordFile!!.toUri())
        val content = inputStream?.readBytes()
        val requestFile =
            content!!.toRequestBody("audio/3gpp".toMediaTypeOrNull(), 0, content.size)
        inputStream.close()
        viewModelScope.fetch({
            val body =
                MultipartBody.Part.createFormData("mediaFile", recordFile?.name, requestFile)
            ChatOwlAPIClient.uploadFile(body).data
        }, {
            appendJournalItem(
                EntryItem(
                    mediaUrl = it.first(),
                    type = EntryItemType.AUDIO.name.lowerCase()
                )
            )
            _isUploading.value = false
        }, {
            recordFile?.let {
                if (it.exists())
                appendJournalItem(
                    EntryItem(
                        uri = recordFile!!.toURI().toString(),
                        type = EntryItemType.AUDIO.name.lowerCase()
                    )
                )
                _isUploading.value = false
            }
        })
    }

    fun onCancelRecordingClick() {
        stopRecording()
    }

    private fun stopRecording() {
        Log.d("MediaRecorder", "stopRecording")
        _isRecording.value = false
        recordingTimer.cancel()
        recorder?.apply {
            stop()
            reset()
            release()
        }
    }

    fun onSaveClicked() {
        addCurrentHelperText()
        journal.value?.let { it ->
            it.cleanEmptyComponents()
            saveJournal(it)
        }
    }

    private fun saveJournal(journal: Journal) {
        _isLoading.value = true
        viewModelScope.fetch({
            journalRepository.saveJournal(journal)
        }, {
            _isLoading.value = false
            _dismiss.value = true
        }, {
            _isLoading.value = false
            it.printStackTrace()
        })
    }

    fun onKeyboardStateChanged(isVisible: Boolean) {
        _isKeyboardVisible.value = isVisible
    }

    // RECORD TOOL TIP
    fun onRecordButtonPressed(isPressed: Boolean) {
        _showTooltip.value = isPressed
    }

    // NEW JOURNAL DIALOG EXCLUSIVELY
    fun onNewJournalDialogClosed() {
        addCurrentHelperText()
        journal.value?.let { draft ->
            draft.cleanEmptyComponents()
            if (draft.components.isEmpty()) {
                deleteDraft()
            } else {
                saveDraft(draft)
            }
        }
    }

    private fun saveDraft(draft: Journal) {
        viewModelScope.fetch({
            journalRepository.saveDraft(draft)
        }, {
            _dismiss.value = true
        }, {
            it.printStackTrace()
        })
    }

    private fun deleteDraft() {
        viewModelScope.fetch({
            journalRepository.deleteDraft()
        }, {
            _dismiss.value = true
        }, {
            it.printStackTrace()
        })
    }

}