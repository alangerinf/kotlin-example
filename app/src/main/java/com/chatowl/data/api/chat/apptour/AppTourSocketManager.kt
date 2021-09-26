package com.chatowl.data.api.chat.apptour

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.chatowl.data.api.chat.IFloatingChatSocketManager
import com.chatowl.data.database.dao.ChatDao
import com.chatowl.data.entities.apptour.AppTourData
import com.chatowl.data.entities.chat.ChatAnswer
import com.chatowl.data.entities.chat.ChatMessage
import com.instabug.library.logging.InstabugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppTourSocketManager private constructor() : IFloatingChatSocketManager {

    private var appTourData: AppTourData? = null
    private var waitingForAppTourAnswer = false
    private var keepAlive = false
    private var updatedMessages: MutableList<ChatMessage> = mutableListOf<ChatMessage>()


    // APP TOUR CHAT
    val appTourMessages: LiveData<List<ChatMessage>> get() = _appTourMessages
    private val _appTourMessages = MutableLiveData<List<ChatMessage>>()

    companion object {
        @Volatile
        private var INSTANCE: AppTourSocketManager? = null

        fun getInstance(
        ): AppTourSocketManager {
            synchronized(this) {
                var instance = AppTourSocketManager.INSTANCE
                if (instance == null) {
                    instance = AppTourSocketManager()
                    AppTourSocketManager.INSTANCE = instance
                }
                return instance
            }
        }

        fun getInstance(
            appTourData: AppTourData?
        ): AppTourSocketManager {
            synchronized(this) {
                var instance = AppTourSocketManager.INSTANCE
                if (instance == null) {
                    instance = AppTourSocketManager()
                    AppTourSocketManager.INSTANCE = instance
                    instance.appTourData = appTourData
                }
                return instance

            }
        }
    }

    override fun getMessages(): LiveData<List<ChatMessage>> {
        return appTourMessages
    }

    fun connectToAppTour() {
        Log.d("SocketManager", "connect to app tour")
        InstabugLog.d("SocketManager -> connect to app tour")
    }

    /**
     * - - - App Tour - - -
     */
    override fun subscribe() {
        Log.d("SocketManager", "filling all AppTour messages")
        InstabugLog.d("filling all AppTour messages")
    }

    fun beginAppTour() {
        Log.d("SocketManager", "onAppTourMessageReceived: begin apptour")
        InstabugLog.d("SocketManager -> onAppTourMessageReceived: begin apptour")
        appTourData?.let {
            keepAlive = true
            receiveAppTourMessage(it.tour)
        }
    }

    fun receiveAppTourMessage(messages: List<ChatMessage>) {
        Log.d("SocketManager", "onAppTourMessageReceived: " + messages[0].toString())
        InstabugLog.d("SocketManager -> onTourChatMessageReceived: " + messages[0].toString())

        messages?.let { messageList ->

            CoroutineScope(Dispatchers.IO).launch {
                processMessagesOneAtATime(messageList)
            }

        }
    }

    private fun processMessagesOneAtATime(messages: List<ChatMessage>) {
        var message: ChatMessage? = null
        var millisec: Long? = null
        var processedMessages = 0
        while (keepAlive) {
            if (!keepAlive) {
                break;
            }
            if (!waitingForAppTourAnswer) {
                message = messages.get(processedMessages)
                millisec = ((message.text?.length ?: 1) * 10).toLong()
                updatedMessages.add(message)
                _appTourMessages.postValue(updatedMessages)
                if (message.question?.botChoices != null) {
                    waitingForAppTourAnswer = true
                }
                processedMessages += 1
                Log.d(
                    "SocketManager",
                    "processedMessages: $processedMessages, total: ${messages.size}"
                )
                if (messages.size == processedMessages) {
                    keepAlive = false
                }

                Thread.sleep(millisec)

            } else {
                Thread.sleep(1000)
            }
        }
    }

    override fun appendMessage(chatMessage: ChatMessage) {
        updatedMessages.add(chatMessage)
        _appTourMessages.postValue(updatedMessages)
        waitingForAppTourAnswer = false
    }

    override fun sendMessage(chatAnswer: ChatAnswer) {
        Log.d("SocketManager", "sendAppTourChatMessage: $chatAnswer")
        InstabugLog.d("SocketManager -> sendAppTourChatMessage: $chatAnswer")
        //
    }


    override fun getSocketMessageLiveData(): LiveData<String> {
        Log.d("SocketManager", "OpenChat")
        InstabugLog.d("SocketManager -> OpenChat")
        return MutableLiveData<String>()
    }

    override fun getSurveyLiveData(): LiveData<String> {
        Log.d("SocketManager", "getSurvey")
        InstabugLog.d("SocketManager -> getSurvey")
        return MutableLiveData<String>()
    }

    override fun getIsTypingLiveData(): LiveData<Boolean> {
        Log.d("SocketManager", "isTyping")
        InstabugLog.d("SocketManager -> isTyping")
        return MutableLiveData<Boolean>()
    }

    override fun pause() {
        Log.d("SocketManager", "Pause")
        InstabugLog.d("SocketManager -> Pause")

    }

    override fun disconnect() {
        Log.d("SocketManager", "disconnect")
        waitingForAppTourAnswer = false
        keepAlive = false
        InstabugLog.d("SocketManager -> disconnect")
    }

    override fun openChat(toolId: Int?) {
        Log.d("SocketManager", "OpenChat")
        InstabugLog.d("SocketManager -> OpenChat")
    }

    override fun connect() {
        Log.d("SocketManager", "connect")
        InstabugLog.d("SocketManager -> connect")
    }

    override fun resetChat() {
        updatedMessages.clear()
    }

    override fun unsubscribe() {
        keepAlive = false
        waitingForAppTourAnswer = false
    }

}