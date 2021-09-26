package com.chatowl.data.entities.messages

data class GetMessagesResponse(
    val messages: List<Message>,
    val total: Int
)