package com.chatowl.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.entities.chat.RoomChatMessageWithTool
import com.chatowl.data.entities.journal.DRAFT_ID
import com.chatowl.data.entities.journal.Journal
import com.chatowl.data.entities.messages.Message
import com.chatowl.data.entities.toolbox.Tool

@Dao
interface MessageDao {

    @Transaction
    @Query("SELECT * FROM message ORDER BY date")
    fun getMessagesLiveData(): LiveData<List<Message>>

    @Query("SELECT * FROM message WHERE id =:messageId")
    suspend fun getMessageById(messageId: String): Message?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessageList(messages: List<Message>)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM message WHERE id =:messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("UPDATE message SET unread = 0 WHERE id =:messageId")
    suspend fun setMessageAsRead(messageId: String)
}