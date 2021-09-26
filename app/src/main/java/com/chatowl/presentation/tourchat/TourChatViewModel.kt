package com.chatowl.presentation.tourchat

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.data.api.chat.*
import com.chatowl.data.api.chat.apptour.AppTourSocketManager
import com.chatowl.data.entities.chat.*
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.getStringTimestampFromDate
import com.instabug.library.logging.InstabugLog


class TourChatViewModel(application: Application,
                        private val socketManager: IFloatingChatSocketManager,
                        private val tourChatFlowId: Int
) : BaseViewModel(application), LifecycleObserver {

    fun  isAppTour():Boolean = tourChatFlowId == APP_TOUR_FLOW_ID

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val socketManager: IFloatingChatSocketManager,
        private val tourChatFlowId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TourChatViewModel(application, socketManager, tourChatFlowId) as T
        }
    }

    val collapse: LiveData<Boolean> get() = _collapse
    private val _collapse = MutableLiveData<Boolean>()

    val action: LiveData<ChatAction> get() = _action
    private val _action = SingleLiveEvent<ChatAction>()

    val navigateThroughTabView: LiveData<ChatActionType> get() = _navigateThroughTabView
    private val _navigateThroughTabView = SingleLiveEvent<ChatActionType>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        if (!isAppTour()) {
            socketManager.connect()
            socketManager.subscribe()
            Log.d("alan", "onCreate: tourId $tourChatFlowId")
            socketManager.openChat(tourChatFlowId)
        } else {
            Log.d("alan", "es app tour")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        //TODO ojo con esto.... esta mal
        //TODO usar bien el polimorfismo
        if (isAppTour()){
            (socketManager as AppTourSocketManager).beginAppTour()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        socketManager.disconnect()
        socketManager.unsubscribe()
    }

    val messageList = socketManager.getMessages()

    val endChat = Transformations.map(messageList) { messageList ->
        messageList.firstOrNull {
            it.messageType.equals(ChatMessageType.CHAT_MARKER.name, true) &&
                    it.messageData?.markerType.equals(ChatMarkerType.END.name, true)
        } != null
    }

    val choices: LiveData<Pair<BotChoice?, BotChoice?>?> = Transformations.map(messageList) { list ->
        if(list.isNotEmpty()) {
            val choices = list.last().question?.botChoices
            val first = choices?.firstOrNull()
            val second = choices?.takeIf { it.size >= 2 }?.get(1)
            if (first == null && second == null) null else Pair(first, second)
        } else {
            null
        }
    }

    fun onChoiceClicked(botChoice: BotChoice) {
        val botStepId = messageList.value?.lastOrNull()?.botStepId ?: -1
        val order = 0
        val id = ChatMessageSender.USER.name.lowerCase() + "|" + botStepId + "|" + order
        val timestamp = getStringTimestampFromDate(serverTimeZone = true)
        val metadata = messageList.value?.last()?.question?.metadata ?: ""

        val answerChatItem = ChatMessage(
            id = id,
            botStepId = botStepId,
            botChoiceId = botChoice.id,
            messageType = ChatMessageType.TEXT.name.lowerCase(),
            metadata = metadata,
            sender = ChatMessageSender.USER.name.lowerCase(),
            sent = false,
            text = botChoice.label,
            timestamp = timestamp
        )

        socketManager.sendMessage(answerChatItem.asSocketMessage())

        socketManager.appendMessage(answerChatItem)

        botChoice.action?.let {
            it.name?.let { actionName ->
                Log.e("SocketManager", "ACTION:"+actionName)
                try {
                    val action = ChatActionType.valueOf(actionName.toUpperCase())
                    action.data = botChoice.action.data
                    _navigateThroughTabView.value = action
                } catch (e: IllegalArgumentException) {
                    Log.e("SocketManager", "ACTION: action wasnt recognized $actionName")
                    _action.value = it
                }
            }
        }?: run {
            Log.e("SocketManager", "no action")
        }
    }

    fun collapseRequested(collapse: Boolean){
        _collapse.value = collapse
    }

    fun onViewDestroyed(){
        if (isAppTour()){
            socketManager.resetChat()
        }
    }
}
