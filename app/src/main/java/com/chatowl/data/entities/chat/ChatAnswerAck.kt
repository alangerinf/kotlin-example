package com.chatowl.data.entities.chat

data class ChatAnswerAck(
    val status: String,
    val chatItem: ChatMessage? = null,
    val clientAnswer: ChatAnswer? = null,
    val message: String?
)

enum class ChatAnswerAckStatus {
    SUCCESS,
    FAIL
}