package com.chatowl.data.api.chat

import io.socket.client.Socket

const val EVENT_CLIENT_CONNECT = Socket.EVENT_CONNECT
const val EVENT_CLIENT_DISCONNECT = Socket.EVENT_DISCONNECT
const val EVENT_CLIENT_CONNECT_ERROR = Socket.EVENT_CONNECT_ERROR
const val EVENT_CLIENT_OPEN_CHAT = "eventClientOpenChat"
const val EVENT_CLIENT_ANSWER = "eventClientAnswer"
const val EVENT_CLIENT_PAUSED = "eventClientChatPaused"
const val EVENT_CLIENT_SURVEY = "eventClientSurvey"
const val EVENT_BOT_TYPING_START = "eventBotTypingStart"
const val EVENT_BOT_TYPING_END = "eventBotTypingEnd"
const val EVENT_SERVER_ERROR = "eventServerError"
const val EVENT_ERROR = "error"
const val EVENT_APP_MANAGEMENT = "eventAppManagement"

const val EVENT_OPEN_TOUR_CHAT = "eventTourOpenChat"
const val EVENT_TOUR_CHAT_ANSWER = "eventTourAnswer"

const val TOUR_CHAT_TEST_TOUR_FLOW_ID = 0
const val TOUR_CHAT_WELCOME_FLOW_ID = 1
const val TOUR_CHAT_PLANS_FLOW_ID = 2
const val TOUR_CHAT_CHANGE_THERAPY_FLOW_ID = 4
const val APP_TOUR_FLOW_ID = -1