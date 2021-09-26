package com.chatowl.data.entities.chat

//interface GenericChatItemInterface
//
//abstract class GenericChatItem: GenericChatItemInterface {
//    abstract val createdAt: String
//}

data class ChatHistoryResponse(
        val data: List<ChatMessage>
)