package com.chatowl.data.entities.chat

import com.chatowl.presentation.common.extensions.lowerCase

data class ChatAnswer(
    val customAnswer: String? = null,
    val botChoiceId: Int? = null,
    val botStepId: Int,
    val metadata: String = "",
    val timestamp: String
) {
    fun inferMessageId(): String =
        ChatMessageSender.USER.name.lowerCase() + "|" + botStepId + "|" + 0
}