package com.chatowl.data.api.chat

import android.app.Application
import android.content.Intent
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
import com.chatowl.data.entities.chat.*
import com.chatowl.data.entities.chat.socketerror.SocketError
import com.chatowl.data.entities.chat.socketerror.SocketErrorCode
import com.chatowl.data.entities.chat.survey.Survey
import com.chatowl.presentation.common.extensions.checkConnection
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.get
import com.chatowl.presentation.common.utils.set
import com.chatowl.presentation.main.MainActivity
import com.instabug.library.logging.InstabugLog
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

interface IFloatingChatSocketManager: ISocketManager{
    fun appendMessage(chatMessage: ChatMessage)
}

class TourChatSocketManager private constructor(
    private val application: Application,
    private val chatDao: ChatDao?,
    private val userPool: CognitoUserPool
) : IFloatingChatSocketManager {
    private var retryCount = 0

    private val socketMessage: LiveData<String> get() = _socketMessage
    private val _socketMessage = SingleLiveEvent<String>()

    private val survey: LiveData<String> get() = _survey
    private val _survey = SingleLiveEvent<String>()

    private val isTyping: LiveData<Boolean> get() = _isTyping
    private val _isTyping = MutableLiveData<Boolean>()

    // TOUR CHAT
    val tourChatMessages: LiveData<List<ChatMessage>> get() = _tourChatMessages
    private val _tourChatMessages = MutableLiveData<List<ChatMessage>>()

    private val listMyData = Types.newParameterizedType(
        List::class.java,
        ChatMessage::class.java
    )

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
        private var INSTANCE: TourChatSocketManager? = null

        fun getInstance(
            application: Application,
            chatDao: ChatDao?,
            userPool: CognitoUserPool
        ): TourChatSocketManager {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = TourChatSocketManager(application, chatDao, userPool)
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

    val TAG = this::class.java.simpleName
    private fun initSocket() {
        try {
            val accessToken = PreferenceHelper.getChatOwlPreferences()
                .get(PreferenceHelper.Key.ACCESS_TOKEN, "") ?: ""
            val opts = IO.Options.builder()
                .setAuth(mapOf("token" to accessToken))
                .build()
            socket = IO.socket(BuildConfig.CHAT_URL, opts)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            throw RuntimeException(e)
        }
    }

    override fun connect() {
        Log.d("SocketManager", "connect")
        InstabugLog.d("SocketManager -> connect")
        if (socket?.connected() == true) return
        socket?.connect()
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

    override fun getMessages(): LiveData<List<ChatMessage>> {
        return tourChatMessages
    }

    /**
     * - - - TOUR CHAT - - -
     */

    override fun subscribe() {
        Log.d("SocketManager", "subscribeTourChat")
        InstabugLog.d("SocketManager -> subscribeTourChat")

        socket?.on(EVENT_CLIENT_CONNECT) {
            Log.d("SocketManager", "onConnectTourChat")
            InstabugLog.d("SocketManager -> onConnectTourChat")
            retryCount = 0
        }

        socket?.on(EVENT_OPEN_TOUR_CHAT) {
            Log.d("SocketManager", "onTourChatHistoryReceived: " + it.first().toString())
            InstabugLog.d("SocketManager -> onTourChatHistoryReceived: " + it.first().toString())
            val chatHistory = chatItemsMoshiAdapter.fromJson(it.first().toString())
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
                    subscribe()
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
                        subscribe()
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

    var lastToolId:Int? = null

    override fun openChat(toolId: Int?) {
        toolId?.let {
            lastToolId = it
        }
        Log.d("SocketManager", "openTourChatsssssssss $toolId")
        InstabugLog.d("SocketManager -> openTourChat $toolId")
        //TODO ojo con esto, toolId tiene doble coso "!!"
        lastToolId?.let {
            Log.d("SocketManager openChat", "$it")
            val openChatSessionObject = JSONObject(openTourChatMoshiAdapter.toJson(OpenTourChat(tourChat = it)))
            socket?.emit(EVENT_OPEN_TOUR_CHAT, openChatSessionObject)
        }
    }

    override fun sendMessage(chatAnswer: ChatAnswer) {
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

    override fun appendMessage(chatMessage: ChatMessage) {
        Log.d("SocketManager", "appendTourChatMessage: $chatMessage")
        InstabugLog.d("SocketManager -> appendTourChatMessage: $chatMessage")
        val updatedMessages = tourChatMessages.value?.toMutableList() ?: mutableListOf()
        updatedMessages.add(chatMessage)
        _tourChatMessages.postValue(updatedMessages)
    }

    override fun unsubscribe() {
        _tourChatMessages.value = emptyList()
        Log.d("SocketManager", "unsubscribeTourChat")
        InstabugLog.d("SocketManager -> unsubscribeTourChat")
        socket?.off(EVENT_CLIENT_CONNECT)
        socket?.off(EVENT_OPEN_TOUR_CHAT)
        socket?.off(EVENT_TOUR_CHAT_ANSWER)
        socket?.off(EVENT_CLIENT_CONNECT_ERROR)
        socket?.off(EVENT_SERVER_ERROR)
        socket?.off(EVENT_ERROR)
    }

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