package com.chatowl.presentation.messages.newone

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
import com.chatowl.data.entities.journal.EntryItem
import com.chatowl.data.entities.journal.EntryItemType
import com.chatowl.data.entities.journal.asAdapterItemList
import com.chatowl.data.entities.journal.gallery.GalleryItem
import com.chatowl.data.entities.messages.Message
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.data.repositories.MessagesRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.SingleLiveEvent
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*

class NewMessageViewModel(
    application: Application,
    private val messagesRepository: MessagesRepository,
    journalId: String
) : BaseViewModel(application),
    LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val messagesRepository: MessagesRepository,
        private val journalId: String
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NewMessageViewModel(application, messagesRepository, journalId) as T
        }
    }

    companion object {
        const val RECORDING_LIMIT = 120000L
        const val COUNTDOWN_INTERVAL = 1000L
    }

    val message: LiveData<Message> get() = _message
    private val _message = MutableLiveData<Message>()

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

    val isKeyboardVisible: LiveData<Boolean> get() = _isKeyboardVisible
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
        fetchJournal(journalId)
    }

    private fun fetchJournal(messageId: String) {
        viewModelScope.fetch({
            messagesRepository.getMessageById(messageId)
        }, {
            _message.value = it
        }, {
            it.printStackTrace()
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        onCancelRecordingClick()
    }

    private val domainMessageItems = Transformations.map(message) { message ->
        message.components
    }

    val adapterMessageItems = Transformations.map(domainMessageItems) { list ->
        list.asAdapterItemList()
    }

    fun viewFullscreen(journalItemId: Int) {
        domainMessageItems.value?.find { it.id == journalItemId }?.let { journalItem ->
            when (EntryItemType.valueOf(journalItem.type)) {
                EntryItemType.VIDEO -> {
                    _fullscreenPlayer.value = FullscreenPlayerData(
                        mediaUri = Uri.parse(journalItem.mediaUrl),
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

    private fun addCurrentHelperText() {
        helperText.value?.let { text ->
            if (text.isNotEmpty()) {
                appendJournalItem(
                    EntryItem(
                        content = text,
                        type = EntryItemType.TEXT.name.lowerCase()
                    )
                )
                _helperText.value = ""
            }
        }
    }

    fun removeItem(position: Int) {
        val currentJournalItems = message.value?.components?.toMutableList() ?: mutableListOf()
        currentJournalItems.removeAt(position)
        _message.value = message.value?.copy(components = currentJournalItems)
    }

    private fun appendJournalItem(entryItem: EntryItem) {
        val currentJournalItems = message.value?.components?.toMutableList() ?: mutableListOf()
        entryItem.order = currentJournalItems.size
        currentJournalItems.add(entryItem)
        _message.value = message.value?.copy(components = currentJournalItems)
    }

    fun onTextContentChanged(id: Int, newContent: String) {
        val currentJournalItems = message.value?.components?.toMutableList() ?: mutableListOf()
        val item = currentJournalItems.find { it.id == id }
        item?.let { journalItem ->
            if (newContent != item.content) {
                journalItem.content = newContent
                val index = currentJournalItems.indexOfFirst { it.order == item.order }
                currentJournalItems[index] = item
                _message.value = message.value?.copy(components = currentJournalItems)
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
                sourceUri.lastPathSegment ?: "Camera content.jpg",
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                "image/jpeg",
                sourceUri
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
                    mediaUrl = it.first(),
                    type = if (galleryItem.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        EntryItemType.VIDEO.name.lowerCase()
                    } else {
                        EntryItemType.IMAGE.name.lowerCase()
                    }
                )
            )
            clearFile()
            _isUploading.value = false
        }, {
            _isUploading.value = false
            _errorMessage.value = R.string.error
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
                clearFile()
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
        viewModelScope.fetch({
            val inputStream: InputStream? =
                getApplication<ChatOwlApplication>().contentResolver.openInputStream(recordFile!!.toUri())
            val content = inputStream?.readBytes()
            val requestFile =
                content!!.toRequestBody("audio/3gpp".toMediaTypeOrNull(), 0, content.size)
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
            clearFile()
            _isUploading.value = false
        }, {
            _isUploading.value = false
            _errorMessage.value = R.string.error
            it.printStackTrace()
        })
    }

    fun onCancelRecordingClick() {
        stopRecording()
        clearFile()
    }

    private fun stopRecording() {
        Log.d("MediaRecorder", "stopRecording")
        _isRecording.value = false
        recordingTimer.cancel()
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    private fun clearFile() {
        recordFile?.delete()
        recordFile = null
    }

    fun onSendClicked() {
        addCurrentHelperText()
        message.value?.let { it ->
            it.cleanEmptyComponents()
            sendMessage(it)
        }
    }

    private fun sendMessage(message: Message) {
        viewModelScope.fetch({
            messagesRepository.sendMessage(message)
        }, {
            _dismiss.value = true
        }, {
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

    fun onNewMessageDialogClosed() {
        addCurrentHelperText()
        message.value?.let { draft ->
            draft.cleanEmptyComponents()
            if(draft.components.isEmpty()) {
                deleteDraft()
            } else {
                saveDraft(draft)
            }
        }
    }

    private fun saveDraft(draft: Message) {
        viewModelScope.fetch({
            messagesRepository.saveDraft(draft)
        }, {
            _dismiss.value = true
        }, {
            it.printStackTrace()
        })
    }

    private fun deleteDraft() {
        viewModelScope.fetch({
            messagesRepository.deleteDraft()
        }, {
            _dismiss.value = true
        }, {
            it.printStackTrace()
        })
    }

}