package com.chatowl.data.api.chat

import android.app.Application
import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.chatowl.BuildConfig
import com.chatowl.data.database.dao.ChatDao
import com.chatowl.data.entities.apptour.AppTourData
import com.chatowl.data.entities.chat.*
import com.chatowl.data.entities.chat.socketerror.SocketError
import com.chatowl.data.entities.chat.socketerror.SocketErrorCode
import com.chatowl.data.entities.chat.survey.Survey
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.extensions.checkConnection
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.get
import com.chatowl.presentation.common.utils.set
import com.chatowl.presentation.main.MainActivity
import com.chatowl.service.ChatOwlFirebaseMessagingService
import com.instabug.library.logging.InstabugLog
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.function.IntToLongFunction
import kotlin.concurrent.thread

interface ISocketManager {
    fun subscribe()
    fun unsubscribe()
    fun sendMessage(chatAnswer: ChatAnswer)

    fun getSocketMessageLiveData(): LiveData<String>
    fun getSurveyLiveData(): LiveData<String>
    fun getIsTypingLiveData(): LiveData<Boolean>

    fun pause()
    fun disconnect()
    fun openChat(toolId: Int?)
    fun connect()
    fun resetChat()

    fun getMessages(): LiveData<List<ChatMessage>>
}

class SocketManager private constructor(
    private val application: Application,
    private val chatDao: ChatDao,
    private val userPool: CognitoUserPool
) : ISocketManager{

    private var retryCount = 0

    private val socketMessage: LiveData<String> get() = _socketMessage
    private val _socketMessage = SingleLiveEvent<String>()

    private val survey: LiveData<String> get() = _survey
    private val _survey = SingleLiveEvent<String>()

    private val isTyping: LiveData<Boolean> get() = _isTyping
    private val _isTyping = MutableLiveData<Boolean>()

    // TOUR CHAT
    //val tourChatMessages: LiveData<List<ChatMessage>> get() = _tourChatMessages
    //private val _tourChatMessages = MutableLiveData<List<ChatMessage>>()

    private val listMyData = Types.newParameterizedType(
        List::class.java,
        ChatMessage::class.java
    )

    override fun getMessages(): LiveData<List<ChatMessage>> {
        return MutableLiveData<List<ChatMessage>>()
    }

    override fun getSocketMessageLiveData(): LiveData<String> {
        return socketMessage
    }

    override fun getSurveyLiveData(): LiveData<String> {
        return survey
    }

    override fun getIsTypingLiveData(): LiveData<Boolean> {
        return  isTyping
    }

    private val chatItemsMoshiAdapter: JsonAdapter<List<ChatMessage>> = Moshi.Builder().build()
        .adapter(listMyData)

    private val openChatMoshiAdapter: JsonAdapter<OpenChat> =
        Moshi.Builder().build().adapter(OpenChat::class.java)

    private val openTourChatMoshiAdapter: JsonAdapter<OpenTourChat> =
        Moshi.Builder().build().adapter(OpenTourChat::class.java)

    private val chatAnswerMoshiAdapter: JsonAdapter<ChatAnswer> =
        Moshi.Builder().build().adapter(ChatAnswer::class.java)

    private val chatAnswerAckMoshiAdapter: JsonAdapter<ChatAnswerAck> =
        Moshi.Builder().build().adapter(ChatAnswerAck::class.java)

    private val socketErrorMoshiAdapter: JsonAdapter<SocketError> =
        Moshi.Builder().build().adapter(SocketError::class.java)

    private val socketSurveyMoshiAdapter: JsonAdapter<Survey> =
        Moshi.Builder().build().adapter(Survey::class.java)

    // TODO lateinit?
    var socket: Socket? = null
    private var localPersistence: ChatDao? = null

    companion object {

        @Volatile
        private var INSTANCE: SocketManager? = null

        fun getInstance(
            application: Application,
            chatDao: ChatDao,
            userPool: CognitoUserPool
        ): SocketManager {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = SocketManager(application, chatDao, userPool)
                    INSTANCE = instance
                }
                return instance
            }
        }

        fun resetInstance() {
            INSTANCE = null
        }

    }

    init {
        localPersistence = chatDao
        initSocket()
    }

    private fun initSocket() {
        try {
            val accessToken = PreferenceHelper.getChatOwlPreferences()
                .get(PreferenceHelper.Key.ACCESS_TOKEN, "") ?: ""
            val opts = IO.Options.builder()
                .setAuth(mapOf("token" to accessToken))
                .build()
            socket = IO.socket(BuildConfig.CHAT_URL, opts)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * - - - MAIN CHAT - - -
     */

    override fun connect() {
        Log.d("SocketManager", "connect")
        InstabugLog.d("SocketManager -> connect")
        if (socket?.connected() == true) return
        socket?.connect()
    }

    override fun openChat(toolId: Int?) {
        Log.d("SocketManager", "openChat $toolId")
        InstabugLog.d("SocketManager -> openChat $toolId")
        val openChatSessionObject = if (toolId != null) {
            JSONObject(openChatMoshiAdapter.toJson(OpenChat(toolId = toolId)))
        } else {
            null
        }
        socket?.emit(EVENT_CLIENT_OPEN_CHAT, openChatSessionObject)
    }

    override fun subscribe() {
        Log.d("SocketManager", "subscribe")
        InstabugLog.d("SocketManager -> subscribe")

        socket?.on(EVENT_CLIENT_CONNECT) {
            Log.d("SocketManager", "onConnect")
            InstabugLog.d("SocketManager -> onConnect")
            retryCount = 0
            val unsentMessages = localPersistence?.getUnsentMessages()
            unsentMessages?.forEach { persistedMessage ->
                sendMessage(persistedMessage.asSocketMessage())
            }
        }

        socket?.on(EVENT_CLIENT_OPEN_CHAT) {
            Log.d("SocketManager", "onHistoryReceived: " + it[0].toString())
            InstabugLog.d("SocketManager -> onHistoryReceived: " + it[0].toString())
            val chatHistory = chatItemsMoshiAdapter.fromJson(it[0].toString())
            chatHistory?.let {
                localPersistence?.persistMessages(chatHistory)
            }
        }

        socket?.on(EVENT_CLIENT_ANSWER) { answer ->
            Log.d("SocketManager", "onMessageReceived: " + answer[0].toString())
            InstabugLog.d("SocketManager -> onMessageReceived: " + answer[0].toString())
            val messages = chatItemsMoshiAdapter.fromJson(answer[0].toString())
            messages?.let {
                localPersistence?.let {
                    it.persistMessages(messages)
                }
            }
        }

        socket?.on(EVENT_BOT_TYPING_START) {
            Log.d("SocketManager", "typing")
            InstabugLog.d("SocketManager -> typing")
            _isTyping.postValue(true)
        }

        socket?.on(EVENT_BOT_TYPING_END) {
            Log.d("SocketManager", "not typing")
            InstabugLog.d("SocketManager -> not typing")
            _isTyping.postValue(false)
        }

        socket?.on(EVENT_CLIENT_SURVEY) { answer ->
            Log.d("SocketManager", "onSurveyReceived: " + answer[0].toString())
            InstabugLog.d("SocketManager -> onSurveyReceived: " + answer[0].toString())
            val survey = socketSurveyMoshiAdapter.fromJson(answer[0].toString())
            survey?.let {
                _survey.postValue(it.token)
            }
        }

        socket?.on(EVENT_CLIENT_DISCONNECT) {
            Log.d("SocketManager", "onDisconnect")
            InstabugLog.d("SocketManager -> onDisconnect")
            retryCount = 0
            _isTyping.postValue(false)
        }

        socket?.on(EVENT_CLIENT_CONNECT_ERROR, onEventConnectError)

        socket?.on(EVENT_SERVER_ERROR, onEventServerError)

        socket?.on(EVENT_ERROR, onError)
    }

    private val onEventConnectError = Emitter.Listener {
        Log.d("SocketManager", "onEventConnectError: " + it[0].toString())
        InstabugLog.d("SocketManager -> onEventConnectError: " + it[0].toString())
        if (retryCount == 0 && application.checkConnection()) {
            retryCount++
            onTokenExpired {
                subscribe()
                openChat(null)
            }
        }
    }

    private val onEventServerError = Emitter.Listener {
        Log.d("SocketManager", "onEventServerError: " + it[0].toString())
        InstabugLog.d("SocketManager -> onEventServerError: " + it[0].toString())
        val socketError = socketErrorMoshiAdapter.fromJson(it[0].toString())
        when (socketError?.errorCode) {
            SocketErrorCode.TOKEN_INVALID.name -> {
                onTokenExpired {
                    subscribe()
                    openChat(null)
                }
            }
        }
    }

    private val onError = Emitter.Listener {
        Log.d("SocketManager", "onError: " + it[0].toString())
        InstabugLog.d("SocketManager -> onError: " + it[0].toString())
    }

    override fun disconnect() {
        Log.d("SocketManager", "disconnect")
        InstabugLog.d("SocketManager -> disconnect")
        socket?.disconnect()
    }

    override fun unsubscribe() {
        Log.d("SocketManager", "unsubscribe")
        InstabugLog.d("SocketManager -> unsubscribe")
        socket?.off(EVENT_CLIENT_CONNECT)
        socket?.off(EVENT_CLIENT_OPEN_CHAT)
        socket?.off(EVENT_CLIENT_ANSWER)
        socket?.off(EVENT_BOT_TYPING_START)
        socket?.off(EVENT_BOT_TYPING_END)
        socket?.off(EVENT_CLIENT_SURVEY)
        socket?.off(EVENT_CLIENT_DISCONNECT)
        socket?.off(EVENT_CLIENT_CONNECT_ERROR)
        socket?.off(EVENT_SERVER_ERROR)
        socket?.off(EVENT_ERROR)
    }

    override fun sendMessage(chatAnswer: ChatAnswer) {
        Log.d("SocketManager", "sendMessage: $chatAnswer")
        InstabugLog.d("SocketManager -> sendMessage: $chatAnswer")
        socket?.emit(
            EVENT_CLIENT_ANSWER,
            JSONObject(chatAnswerMoshiAdapter.toJson(chatAnswer)),
            Ack { ackData ->
                val ack = chatAnswerAckMoshiAdapter.fromJson(ackData[0].toString())
                Log.d("SocketManager", "sendMessage ack: " + ack.toString())
                InstabugLog.d("SocketManager -> sendMessage ack: ${ackData[0]}")
                ack?.let {
                    if(it.status.equals(ChatAnswerAckStatus.SUCCESS.name, true)) {
                        Log.d("SocketManager", "sendMessage ack success")
                        it.chatItem?.let { item ->
                            val chatMessage = item.apply {
                                sent = true
                            }
                            Log.d("ChatViewModel", "message inserted: $chatMessage")
                            localPersistence?.updateMessage(chatMessage)
                        }
                    } else {
                        Log.d("SocketManager", "sendMessage ack fail")
                        it.clientAnswer?.let { answer ->
                            localPersistence?.deleteMessageById(answer.inferMessageId())
                        }
                    }
                }
            })
    }

    override fun pause() {
        Log.d("SocketManager", "pause")
        InstabugLog.d("SocketManager -> pause")
        socket?.emit(EVENT_CLIENT_PAUSED)
    }

    override fun resetChat() {
        InstabugLog.d("SocketManager -> resetChat")
        socket?.emit(EVENT_APP_MANAGEMENT, JSONObject().put("resetChat", true),
            Ack { ackData ->
                val ack = chatAnswerAckMoshiAdapter.fromJson(ackData[0].toString())
                Log.d("SocketManager", "resetChat ack: " + ack.toString())
                InstabugLog.d("SocketManager -> resetChat ack: " + ack.toString())
                socket?.emit(EVENT_CLIENT_OPEN_CHAT)
            })
    }

    /**
     * - - - TOUR CHAT - - -
     */
/*
    fun subscribeTourChat() {
        Log.d("SocketManager", "subscribeTourChat")
        InstabugLog.d("SocketManager -> subscribeTourChat")

        socket?.on(EVENT_CLIENT_CONNECT) {
            Log.d("SocketManager", "onConnectTourChat")
            InstabugLog.d("SocketManager -> onConnectTourChat")
            retryCount = 0
        }

        socket?.on(EVENT_OPEN_TOUR_CHAT) {
            Log.d("SocketManager", "onTourChatHistoryReceived: " + it[0].toString())
            InstabugLog.d("SocketManager -> onTourChatHistoryReceived: " + it[0].toString())
            val chatHistory = chatItemsMoshiAdapter.fromJson(it[0].toString())
            chatHistory?.let { messageList ->
                val updatedMessages = mutableListOf<ChatMessage>().apply{
                    addAll(messageList)
                }
                _tourChatMessages.postValue(updatedMessages)
            }
        }

        socket?.on(EVENT_TOUR_CHAT_ANSWER) { answer ->
            Log.d("SocketManager", "onTourChatMessageReceived: " + answer[0].toString())
            InstabugLog.d("SocketManager -> onTourChatMessageReceived: " + answer[0].toString())
            val messages = chatItemsMoshiAdapter.fromJson(answer[0].toString())
            messages?.let { messageList ->
                val updatedMessages = tourChatMessages.value?.toMutableList() ?: mutableListOf()
                updatedMessages.addAll(messageList)
                _tourChatMessages.postValue(updatedMessages)
            }
        }

        socket?.on(EVENT_CLIENT_CONNECT_ERROR) {
            Log.d("SocketManager", "onTourChatEventConnectError: " + it[0].toString())
            InstabugLog.d("SocketManager -> onTourChatEventConnectError: " + it[0].toString())
            if (retryCount == 0 && application.checkConnection()) {
                retryCount++
                onTokenExpired {
                    subscribeTourChat()
                    openChat(null)
                }
            }
        }

        socket?.on(EVENT_SERVER_ERROR) {
            Log.d("SocketManager", "onTourChatEventServerError: " + it[0].toString())
            InstabugLog.d("SocketManager -> onTourChatEventServerError: " + it[0].toString())
            val socketError = socketErrorMoshiAdapter.fromJson(it[0].toString())
            when (socketError?.errorCode) {
                SocketErrorCode.TOKEN_INVALID.name -> {
                    onTokenExpired {
                        subscribeTourChat()
                        openChat(null)
                    }
                }
            }
        }

        socket?.on(EVENT_ERROR) {
            Log.d("SocketManager", "onTourChatError: " + it[0].toString())
            InstabugLog.d("SocketManager -> onTourChatError: " + it[0].toString())
        }
    }
*/
 /*   fun openTourChat(toolId: Int) {
        Log.d("SocketManager", "openTourChat $toolId")
        InstabugLog.d("SocketManager -> openTourChat $toolId")
        val openChatSessionObject = JSONObject(openTourChatMoshiAdapter.toJson(OpenTourChat(tourChat = toolId)))
        socket?.emit(EVENT_OPEN_TOUR_CHAT, openChatSessionObject)
    }

    fun sendTourChatMessage(chatAnswer: ChatAnswer) {
        Log.d("SocketManager", "sendTourChatMessage: $chatAnswer")
        InstabugLog.d("SocketManager -> sendTourChatMessage: $chatAnswer")
        socket?.emit(
            EVENT_TOUR_CHAT_ANSWER,
            JSONObject(chatAnswerMoshiAdapter.toJson(chatAnswer)),
            Ack { ackData ->
                val ack = chatAnswerAckMoshiAdapter.fromJson(ackData[0].toString())
                Log.d("SocketManager", "sendTourChatMessage ack: " + ack.toString())
                InstabugLog.d("SocketManager -> sendTourChatMessage ack: ${ackData[0]}")
            })
    }

    fun appendTourChatMessage(chatMessage: ChatMessage) {
        Log.d("SocketManager", "appendTourChatMessage: $chatMessage")
        InstabugLog.d("SocketManager -> appendTourChatMessage: $chatMessage")
        val updatedMessages = tourChatMessages.value?.toMutableList() ?: mutableListOf()
        updatedMessages.add(chatMessage)
        _tourChatMessages.postValue(updatedMessages)
    }

    fun unsubscribeTourChat() {
        _tourChatMessages.value = emptyList()
        Log.d("SocketManager", "unsubscribeTourChat")
        InstabugLog.d("SocketManager -> unsubscribeTourChat")
        socket?.off(EVENT_CLIENT_CONNECT)
        socket?.off(EVENT_OPEN_TOUR_CHAT)
        socket?.off(EVENT_TOUR_CHAT_ANSWER)
        socket?.off(EVENT_CLIENT_CONNECT_ERROR)
        socket?.off(EVENT_SERVER_ERROR)
        socket?.off(EVENT_ERROR)
    }*/
    /**
     * - - - TOUR CHAT - - -
     */


    /**
     * - - - REFRESH TOKEN - - -
     */

    private fun onTokenExpired(onAuthenticationSuccess: () -> Unit) {
        InstabugLog.d("SocketManager -> onTokenExpired")
        userPool.currentUser.getSessionInBackground(CognitoAuthenticationHandler(
            onAuthenticationSuccess = onAuthenticationSuccess,
            onAuthenticationFail = {
                retryCount = 0
                val intent = Intent(application, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                application.startActivity(intent)
            }
        ))
    }

    inner class CognitoAuthenticationHandler(
        private val onAuthenticationSuccess: () -> Unit,
        private val onAuthenticationFail: () -> Unit
    ) : AuthenticationHandler {
        override fun onSuccess(
            userSession: CognitoUserSession?,
            newDevice: CognitoDevice?
        ) {
            InstabugLog.d("SocketManager -> CognitoAuthenticationHandler: onSuccess")
            // Save new token
            val newToken = userSession?.accessToken?.jwtToken
            PreferenceHelper.getChatOwlPreferences()
                .set(PreferenceHelper.Key.ACCESS_TOKEN, newToken)
            // Disconnect socket
            disconnect()
            // Re-initialize socket connection
            initSocket()
            connect()
            // Invoke any method passed in
            onAuthenticationSuccess.invoke()
        }

        override fun getAuthenticationDetails(
            authenticationContinuation: AuthenticationContinuation?,
            userId: String?
        ) {
            InstabugLog.d("SocketManager -> CognitoAuthenticationHandler: getAuthenticationDetails")
            onAuthenticationFail.invoke()
        }

        override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
            InstabugLog.d("SocketManager -> CognitoAuthenticationHandler: getMFACode")
            onAuthenticationFail.invoke()
        }

        override fun authenticationChallenge(continuation: ChallengeContinuation?) {
            InstabugLog.d("SocketManager -> CognitoAuthenticationHandler: authenticationChallenge")
            onAuthenticationFail.invoke()
        }

        override fun onFailure(exception: java.lang.Exception?) {
            InstabugLog.d("SocketManager -> CognitoAuthenticationHandler: onFailure")
            onAuthenticationFail.invoke()
        }

    }

}