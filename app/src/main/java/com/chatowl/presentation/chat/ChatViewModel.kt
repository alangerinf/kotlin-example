package com.chatowl.presentation.chat

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.api.chat.ISocketManager
import com.chatowl.data.api.chat.SocketManager
import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.database.dao.ChatDao
import com.chatowl.data.entities.toolbox.ToolSubtype
import com.chatowl.data.entities.toolbox.ToolType
import com.chatowl.data.entities.chat.*
import com.chatowl.data.entities.chat.ChatMessageType.*
import com.chatowl.data.entities.toolbox.ToolboxExercise
import com.chatowl.data.entities.toolbox.event.ToolEvent
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.presentation.chat.ChatFragment.Companion.NAVIGATE_TO_JOURNAL
import com.chatowl.presentation.chat.ChatFragment.Companion.NAVIGATE_TO_TOOLBOX
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.extensions.upperCase
import com.chatowl.presentation.common.utils.*
import com.instabug.crash.CrashReporting
import com.instabug.library.logging.InstabugLog
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.Part.Companion.createFormData
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


@ExperimentalStdlibApi
class ChatViewModel(
    application: Application,
    private val chatDao: ChatDao,
    private val socketManager: ISocketManager
) : BaseViewModel(application), LifecycleObserver {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val chatDao: ChatDao,
        private val socketManager: ISocketManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ChatViewModel(application, chatDao, socketManager) as T
        }
    }

    val application = getApplication<ChatOwlApplication?>()

    val messageCharacterCount: LiveData<Int> get() = _messageCharacterCount
    private val _messageCharacterCount = MutableLiveData<Int>()

    val hideKeyboardOnview: LiveData<Boolean> get() = _hideKeyboardOnView
    private val _hideKeyboardOnView = MutableLiveData<Boolean>()

    val fullscreenPlayer: LiveData<FullscreenPlayerData> get() = _fullscreenPlayer
    private val _fullscreenPlayer = SingleLiveEvent<FullscreenPlayerData>()

    val fullScreenChoiceList: LiveData<ArrayList<BotChoice>> get() = _fullScreenChoiceList
    private val _fullScreenChoiceList = SingleLiveEvent<ArrayList<BotChoice>>()

    val answerViewState: LiveData<AnswerViewState> get() = _answerViewState
    private val _answerViewState = MutableLiveData<AnswerViewState>()

    private val seekBarValue: LiveData<Int> get() = _seekBarValue
    private val _seekBarValue = MutableLiveData<Int>()

    val navigateThroughTabView: LiveData<ChatActionType> get() = _navigateThroughTabView
    private val _navigateThroughTabView = SingleLiveEvent<ChatActionType>()

    val bindScrollEnd: LiveData<Boolean> get() = _bindScrollEnd
    private val _bindScrollEnd = MutableLiveData<Boolean>()

    private val chatItems = chatDao.getChatMessagesWithTools()
        .map { roomChatMessageWithToolList ->
            roomChatMessageWithToolList.map { roomChatMessageWithTool ->
                roomChatMessageWithTool.toChatMessage()
            }
        }

    val showTestControls: LiveData<Boolean> get() = _showTestControls
    private val _showTestControls = MutableLiveData<Boolean>()

    val isTyping = socketManager.getIsTypingLiveData()

    val surveyToken = socketManager.getSurveyLiveData()

    val socketMessage = socketManager.getSocketMessageLiveData()

    init {
        _showTestControls.value =
            PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.IS_TESTER, false)
                ?: false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        Log.d("ChatViewModel", "onCreate")
        socketManager.connect()
        socketManager.subscribe()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        Log.d("ChatViewModel", "onStart")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        Log.d("ChatViewModel", "onStop")
        if (navigate.value == null) {
            socketManager.pause()
        }
        resetNavigation()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        Log.d("ChatViewModel", "onDestroy")
        socketManager.disconnect()
        socketManager.unsubscribe()
    }

    private fun resetNavigation() {
        _navigate.value = null
    }

    fun setDummyNavDirection() {
        _navigate.value = ChatFragmentDirections.actionChatNoPauseAction()
    }

    val chatAdapterItems = Transformations.map(chatItems) { historyItems ->
        val items = when {
            historyItems.isEmpty() -> {
                emptyList()
            }
            else -> {
                val sortedMessagesByDate = historyItems.groupBy { getShortFormatDate(it.timestamp) }
                val sortedKeys = sortedMessagesByDate.keys.sorted()
                val messagesWithHeaders = mutableListOf<ChatAdapterItem>()
                for (key in sortedKeys) {
                    messagesWithHeaders.add(
                        ChatAdapterItem.DateHeaderAdapterItem(
                            getChatMessageHeaderDescriptiveDate(application, key)
                        )
                    )
                    val sortedMessagesForDate = sortedMessagesByDate[key] ?: emptyList()
                    messagesWithHeaders.addAll(sortedMessagesForDate.mapIndexed { index, messageForDate ->
                        val showTimestamp = if (index < sortedMessagesForDate.size - 1) {
                            getTimeFromServerDate(sortedMessagesForDate[index + 1].timestamp) != getTimeFromServerDate(
                                messageForDate.timestamp
                            ) ||
                                    historyItems[index + 1].sender != messageForDate.sender
                        } else {
                            true
                        }
                        ChatAdapterItem.ChatHistoryAdapterItem(messageForDate, showTimestamp)
                    })
                }
                messagesWithHeaders.reversed()
            }
        }

        _answerViewState.value = getAnswerViewState(historyItems)

        items
    }

    fun openChat(toolId: Int?) {
        socketManager.openChat(toolId)
    }

    private fun getAnswerViewState(messages: List<ChatMessage>): AnswerViewState {
        val answerViewState = AnswerViewState()
        val lastHistoryItem = messages.lastOrNull()
        val lastHistoryItemQuestion = lastHistoryItem?.question
        InstabugLog.d("getAnswerViewState -> botStepId: ${lastHistoryItem?.botStepId}, sender: ${lastHistoryItem?.sender}")
        if (lastHistoryItem?.sender?.equals(ChatMessageSender.BOT.name, true) == true &&
            lastHistoryItemQuestion != null
        ) {
            val answerType =
                ChatQuestionType.valueOf(lastHistoryItemQuestion.questionType.upperCase())
            answerViewState.botChoices =
                getAnswerBotChoices(lastHistoryItem, lastHistoryItemQuestion)
            InstabugLog.d("getAnswerViewState -> answerType: $answerType, botChoices: ${answerViewState.botChoices?.map { it.label }}")
            when (answerType) {
                ChatQuestionType.TEXT -> {
                    answerViewState.toggleCustomAnswerControls = true
                    answerViewState.textAnswerViewState = TextAnswerViewState(
                        false
                    )
                }
                ChatQuestionType.MULTIPLE_CHOICE -> {
                    answerViewState.textAnswerViewState = TextAnswerViewState(
                        true
                    )
                }
                ChatQuestionType.IMAGE -> {
                    answerViewState.imageAnswerViewState = ImageAnswerViewState(
                        answerViewState.botChoices?.size == 1 && answerViewState.botChoices?.firstOrNull()?.id == -1
                    )
                }
                ChatQuestionType.SLIDER -> {
                    val defaultChoiceStep = when (val defaultChoiceIndex =
                        answerViewState.botChoices?.indexOfFirst { it.isDefault == true }) {
                        -1, null -> {
                            answerViewState.botChoices?.size?.div(2)?.minus(1) ?: 0
                        }
                        else -> defaultChoiceIndex
                    }
                    answerViewState.sliderAnswerViewState = SliderAnswerViewState(
                        lastHistoryItemQuestion.text ?: "",
                        lastHistoryItemQuestion.subText ?: "",
                        false,
                        defaultChoiceStep
                    )
                }
            }
        }
        return answerViewState
    }

    private fun getAnswerBotChoices(
        lastHistoryItem: ChatMessage,
        answerData: Question
    ): List<BotChoice> {
        // Initialize bot choices
        var botChoices = answerData.botChoices ?: emptyList()
        // Check if the last item in the chat is a tool message (sent by the bot)
        if (lastHistoryItem.sender.equals(ChatMessageSender.BOT.name, true) &&
            lastHistoryItem.messageType.equals(TOOL.name, true)
        ) {
            // Get the tool
            lastHistoryItem.tool?.let { tool ->
                // Check that it's not a session
                if (!tool.toolType.equals(ToolType.SESSION.name, true)) {
                    // Get the tool id
                    val currentChatToolId: Int = PreferenceHelper.getChatOwlPreferences()
                        .get(PreferenceHelper.Key.CHAT_TOOL, -1) ?: -1
                    if (currentChatToolId != tool.id) {
                        // If it's not already the one stored, save it and reset its status
                        PreferenceHelper.getChatOwlPreferences()
                            .set(PreferenceHelper.Key.CHAT_TOOL, tool.id)
                        PreferenceHelper.getChatOwlPreferences()
                            .set(PreferenceHelper.Key.CHAT_TOOL_STATUS, "")
                    }
                    // Get the status of the tool
                    val currentChatToolStatus = PreferenceHelper.getChatOwlPreferences()
                        .get(PreferenceHelper.Key.CHAT_TOOL_STATUS, "")
                    // Filter bot choices accordingly
                    when (currentChatToolStatus) {
                        ToolEvent.STARTED.name -> {
                            // Show 'Continue' and 'Back to exercise'
                            botChoices = botChoices.filter { it.value == "0" || it.value == "3" }
                        }
                        ToolEvent.FINISHED.name -> {
                            // Send 'Exercise completed' through the chat back to the bot
                            botChoices.find { it.value == "2" }?.let { exerciseCompletedBotChoice ->
                                onMultipleChoiceItemClicked(exerciseCompletedBotChoice)
                            }
                            // Reset chat tool data
                            PreferenceHelper.getChatOwlPreferences()
                                .set(PreferenceHelper.Key.CHAT_TOOL, -1)
                            PreferenceHelper.getChatOwlPreferences()
                                .set(PreferenceHelper.Key.CHAT_TOOL_STATUS, "")
                            // Show only the 'Continue' option just in case
                            botChoices = botChoices.filter { it.value == "0" }
                        }
                        else -> {
                            // Show 'Continue' and 'Start'
                            botChoices = botChoices.filter { it.value == "0" || it.value == "1" }
                        }
                    }
                }
            }
        }

        return when (ChatQuestionType.valueOf(answerData.questionType.upperCase())) {
            ChatQuestionType.TEXT, ChatQuestionType.MULTIPLE_CHOICE -> {
                if (botChoices.isNotEmpty() && answerData.allowCustomAnswer) {
                    botChoices + BotChoice(label = application.getString(R.string.let_me_type))
                } else {
                    botChoices
                }
            }
            ChatQuestionType.IMAGE -> {
                if (answerData.allowCustomAnswer) {
                    listOf(BotChoice()) + (botChoices)
                } else {
                    botChoices
                }
            }
            else -> {
                botChoices
            }
        }
    }

    fun onMessageChanged(message: String) {
        _messageCharacterCount.value = message.length
    }

    fun onChatItemClicked(chatItem: ChatMessage) {
        InstabugLog.d("onChatItemClicked -> ${chatItem.messageType} with id ${chatItem.id}")
        when (ChatMessageType.valueOf(chatItem.messageType.upperCase())) {
            VIDEO -> {
                _fullscreenPlayer.value = chatItem.toFullscreenPlayerData()
            }
            IMAGE -> {
                _navigate.value =
                    ChatFragmentDirections.actionGlobalImageExercise(chatItem.toImageToolboxExercise())
                        .setIsChatSource(true)
            }
            TOOL -> {
                chatItem.tool?.let { tool ->
                    navigateToExercise(tool.toToolboxExercise())
                }
            }
            else -> {
            }
        }
    }

    // TEXT
    val messageLengthLimitReached = Transformations.map(messageCharacterCount) { count ->
        count >= application.resources.getInteger(R.integer.chat_response_max_length).times(0.9F)
    }

    fun onFreeTextBackClicked() {
        _answerViewState.value = answerViewState.value?.copy(toggleCustomAnswerControls = false)
    }

    fun onFreeTextMessageSend(message: String) {
        val trimmedMessage = message.trim()
        if (trimmedMessage.isNotEmpty()) {
            val botStepId = chatItems.value?.lastOrNull()?.botStepId ?: -1
            val order = 0
            val id = ChatMessageSender.USER.name.lowerCase() + "|" + botStepId + "|" + order
            val metadata = chatItems.value?.last()?.question?.metadata ?: ""
            val timestamp = getStringTimestampFromDate(serverTimeZone = true)

            InstabugLog.d("onFreeTextMessageSend -> botStepId: $botStepId, answer: $trimmedMessage}")

            val answerChatItem = ChatMessage(
                id = id,
                botStepId = botStepId,
                customAnswer = trimmedMessage,
                metadata = metadata,
                messageType = TEXT.name.lowerCase(),
                sent = false,
                sender = ChatMessageSender.USER.name.lowerCase(),
                text = trimmedMessage,
                timestamp = timestamp
            )

            hideKeyboard()
            persistMessage(answerChatItem)
            socketManager.sendMessage(answerChatItem.asSocketMessage())
        }
    }

    // MULTIPLE CHOICE
    @ExperimentalStdlibApi
    fun onMultipleChoiceItemClicked(botChoice: BotChoice) {
        Log.e("SocketManager", "onMultipleChoiceItemClicked $botChoice")
        val lastItem = chatItems.value?.last()
        // Check if the last item is a tool (not session)
        if (lastItem?.messageType.equals(TOOL.name, true) &&
            !lastItem?.tool?.toolType.equals(ToolType.SESSION.name, true) &&
            (botChoice.value == "1" || botChoice.value == "3")
        ) {
            Log.e("SocketManager", "111")
            // Launch the tool
            lastItem?.let { it ->
                onChatItemClicked(it)
            }
        } else {

            Log.e("SocketManager", "221")
            // Proceed as normal
            if (botChoice.id == -1) {
                _answerViewState.value =
                    answerViewState.value?.copy(toggleCustomAnswerControls = true)
                Log.e("SocketManager", "444")
            } else {
                Log.e("SocketManager", "555")
                val botStepId = chatItems.value?.lastOrNull()?.botStepId ?: -1
                val order = 0
                val id = ChatMessageSender.USER.name.lowerCase() + "|" + botStepId + "|" + order
                val metadata = chatItems.value?.last()?.question?.metadata ?: ""
                val timestamp = getStringTimestampFromDate(serverTimeZone = true)

                InstabugLog.d("onMultipleChoiceItemClicked -> botStepId: $botStepId, answer: $botChoice}")

                val answerChatItem = ChatMessage(
                    id = id,
                    botChoiceId = botChoice.id,
                    botStepId = botStepId,
                    messageType = TEXT.name.lowerCase(),
                    metadata = metadata,
                    sender = ChatMessageSender.USER.name.lowerCase(),
                    sent = false,
                    text = botChoice.label,
                    timestamp = timestamp,
                )

                hideKeyboard()
                persistMessage(answerChatItem)
                socketManager.sendMessage(answerChatItem.asSocketMessage())

                botChoice.action?.let {

                    it.name?.let { actionName ->

                        Log.e("SocketManager", "ACTION:"+actionName)
                        try {
                            val action = ChatActionType.valueOf(actionName.toUpperCase())
                            action.data = botChoice.action.data
                            _navigateThroughTabView.value = action
                        } catch (e: IllegalArgumentException) {
                            Log.e("SocketManager", "ACTION: action wasnt recognized $actionName")
                        }
                    }
                }?: run {
                    Log.e("SocketManager", "no action")
                }
            }
        }
    }

    // SLIDER
    fun setSeekBarValue(value: Int) {
        _seekBarValue.value = value
    }

    val seekBarLabel = Transformations.map(seekBarValue) { step ->
        val botChoices = answerViewState.value?.botChoices
        InstabugLog.d(
            "addMultipleChoiceItems -> step: $step, " +
                    "botStepId: ${chatItems.value?.lastOrNull()?.botStepId ?: -1}, " +
                    "botChoices: ${botChoices?.map { it.label }}"
        )
        try {
            botChoices?.get(step)?.label?.capitalize(Locale.getDefault())
        } catch (e: Exception) {
            CrashReporting.reportException(e)
            ""
        }
    }

    fun onSliderAnswerDoneClicked() {
        val botStepId = chatItems.value?.lastOrNull()?.botStepId ?: -1
        val order = 0
        val id = ChatMessageSender.USER.name.lowerCase() + "|" + botStepId + "|" + order
        val metadata = chatItems.value?.last()?.question?.metadata ?: ""
        val timestamp = getStringTimestampFromDate(serverTimeZone = true)

        InstabugLog.d("onSliderAnswerDoneClicked -> botStepId: $botStepId, answer: ${seekBarValue.value}")

        val seekBarValue = seekBarValue.value ?: -1
        val botChoice = answerViewState.value?.botChoices?.get(seekBarValue)

        val answerChatItem = ChatMessage(
            id = id,
            botChoiceId = botChoice?.id ?: -1,
            botStepId = botStepId,
            messageType = TEXT.name.lowerCase(),
            metadata = metadata,
            sender = ChatMessageSender.USER.name.lowerCase(),
            sent = false,
            text = botChoice?.label,
            timestamp = timestamp
        )

        hideKeyboard()
        persistMessage(answerChatItem)
        socketManager.sendMessage(answerChatItem.asSocketMessage())

    }

    // IMAGE
    fun onImageChoiceClicked(botChoice: BotChoice) {
        if (botChoice.id == -1) {
            _answerViewState.value = answerViewState.value?.copy(toggleCustomAnswerControls = true)
        } else {
            val botStepId = chatItems.value?.lastOrNull()?.botStepId ?: -1
            val order = 0
            val id = ChatMessageSender.USER.name.lowerCase() + "|" + botStepId + "|" + order
            val metadata = chatItems.value?.last()?.question?.metadata ?: ""
            val timestamp = getStringTimestampFromDate(serverTimeZone = true)

            InstabugLog.d("onImageChoiceClicked -> botStepId: $botStepId, answer: $botChoice}")

            val answerChatItem = ChatMessage(
                id = id,
                botChoiceId = botChoice.id,
                botStepId = botStepId,
                messageType = IMAGE.name.lowerCase(),
                metadata = metadata,
                sender = ChatMessageSender.USER.name.lowerCase(),
                sent = false,
                thumbnailUrl = botChoice.thumbnailUrl,
                timestamp = timestamp
            )

            hideKeyboard()
            persistMessage(answerChatItem)
            socketManager.sendMessage(answerChatItem.asSocketMessage())

        }
    }

    fun onImageChoiceActionClicked() {
        if (answerViewState.value?.toggleCustomAnswerControls == true) {
            _answerViewState.value = answerViewState.value?.copy(toggleCustomAnswerControls = false)
        } else {
            answerViewState.value?.botChoices?.let { botChoices ->
                _fullScreenChoiceList.value =
                    ArrayList(botChoices.filter { !it.thumbnailUrl.isNullOrEmpty() })
            }
        }
    }

    // CUSTOM IMAGE
    fun onCustomImageSelected(uri: Uri) {
        uploadCustomImage(uri)
    }

    private fun uploadCustomImage(uri: Uri) {
        viewModelScope.fetch({
            val inputStream: InputStream? = application.contentResolver.openInputStream(uri)
            val content = inputStream?.readBytes()
            val requestFile =
                content!!.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, content.size)
            val body = createFormData("mediaFile", uri.lastPathSegment, requestFile)
            ChatOwlAPIClient.uploadFile(body).data
        }, {
            sendCustomImageAnswer(it.first())
        }, {
            it.printStackTrace()
        })
    }

    private fun sendCustomImageAnswer(s3Url: String) {
        val botStepId = chatItems.value?.lastOrNull()?.botStepId ?: -1
        val order = 0
        val id = ChatMessageSender.USER.name.lowerCase() + "|" + botStepId + "|" + order
        val metadata = chatItems.value?.last()?.question?.metadata ?: ""
        val timestamp = getStringTimestampFromDate(serverTimeZone = true)

        InstabugLog.d("sendCustomImageAnswer -> botStepId: $botStepId, answer: $s3Url")

        val answerChatItem = ChatMessage(
            id = id,
            botStepId = botStepId,
            customAnswer = s3Url,
            mediaUrl = s3Url,
            messageType = IMAGE.name.lowerCase(),
            metadata = metadata,
            sender = ChatMessageSender.USER.name.lowerCase(),
            sent = false,
            thumbnailUrl = s3Url,
            timestamp = timestamp
        )

        hideKeyboard()
        persistMessage(answerChatItem)
        socketManager.sendMessage(answerChatItem.asSocketMessage())

    }

    //HIDE KEYBOARD
    private fun hideKeyboard(){
        _hideKeyboardOnView.value = true
    }
    // PERSISTENCE
    private fun persistMessage(message: ChatMessage) {
        viewModelScope.fetch ({
            Log.d("ChatViewModel", "message inserted: $message")
            chatDao.insertMessage(message)
        },{}, {})
    }

    // NAVIGATION
    fun navigateToExercise(exercise: ToolboxExercise) {
        when (exercise.tool.toolSubtype) {
            ToolSubtype.VIDEO.name.lowerCase(), ToolSubtype.AUDIO.name.lowerCase() -> {
                _navigate.value = ChatFragmentDirections.actionGlobalMediaExercise(exercise)
                    .setIsChatSource(true)
            }
            ToolSubtype.QUOTE.name.lowerCase() -> {
                _navigate.value = ChatFragmentDirections.actionGlobalQuoteExercise(exercise)
                    .setIsChatSource(true)
            }
            ToolSubtype.IMAGE.name.lowerCase() -> {
                _navigate.value = ChatFragmentDirections.actionGlobalImageExercise(exercise)
                    .setIsChatSource(true)
            }
            ToolSubtype.SESSION.name.lowerCase() -> {
                socketManager.openChat(exercise.tool.id)
            }
        }
    }

    fun onPlayStopped(result: Long) {

    }

    // TEST TOOLS
    fun resetChat() {
        socketManager.resetChat()
    }

    fun scrollEndReached(){
        _bindScrollEnd.value = true
    }

    fun scrollEndReleased(){
        _bindScrollEnd.value = false
    }
    fun onFlowsClicked() {
        _navigate.value = ChatFragmentDirections.actionChatToFlows()
    }

    fun onValuesClicked() {
        _navigate.value = ChatFragmentDirections.actionChatToValues()
    }
}
