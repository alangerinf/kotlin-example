package com.chatowl.data.repositories

import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.entities.home.HomeData

class ChatOwlChatRepository {
    private val client = ChatOwlAPIClient

    suspend fun getChatHistory(sessionId:Int): List<ChatMessage> {
        return try {
            val chatHistory = client.getChatHistory(sessionId).data
            chatHistory
        } catch (e: Exception) {
                throw e
        }
    }

}