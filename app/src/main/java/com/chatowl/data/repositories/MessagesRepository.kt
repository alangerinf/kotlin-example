package com.chatowl.data.repositories

import androidx.lifecycle.LiveData
import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.database.dao.MessageDao
import com.chatowl.data.entities.journal.DRAFT_ID
import com.chatowl.data.entities.messages.Message

class MessagesRepository(private val messageDao: MessageDao) {

    private val client = ChatOwlAPIClient

    fun getMessagesLiveData(): LiveData<List<Message>> {
        return messageDao.getMessagesLiveData()
    }

    suspend fun getMessages(pageNumber: Int): Int {
        val response = client.getMessages(pageNumber).data
        val messages = response.messages
        messageDao.insertMessageList(messages)
        return response.total
    }

    suspend fun getMessageById(id: String): Message {
        return messageDao.getMessageById(id) ?: Message()
    }

    suspend fun sendMessage(message: Message) {
        val createdMessage = client.sendMessage(message).data
        messageDao.insertMessage(createdMessage)
        if(message.id == DRAFT_ID) {
            messageDao.deleteMessageById(DRAFT_ID)
        }
    }

    suspend fun saveDraft(draft: Message) {
        messageDao.insertMessage(draft)
    }

    suspend fun deleteDraft() {
        messageDao.deleteMessageById(DRAFT_ID)
    }

    suspend fun setMessageAsRead(messageId: String) {
        client.setMessageAsRead(messageId)
        messageDao.setMessageAsRead(messageId)
    }
}