package com.chatowl.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.entities.chat.RoomChatMessageWithTool
import com.chatowl.data.entities.toolbox.Tool

@Dao
interface ChatDao {

    @Transaction
    @Query("SELECT * FROM chat_message ORDER BY timestamp")
    fun getChatMessagesWithTools(): LiveData<List<RoomChatMessageWithTool>>

    @Transaction
    fun persistMessages(messages: List<ChatMessage>) {
        insertMessages(messages)
        insertTools(messages.mapNotNull { it.tool })
    }

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<ChatMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTools(tools: List<Tool>)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Update
    fun updateMessage(message: ChatMessage)

    @Query("DELETE FROM chat_message WHERE id = :messageId")
    fun deleteMessageById(messageId: String)

    @Query ("SELECT * FROM chat_message WHERE sent = 0")
    fun getUnsentMessages(): List<ChatMessage>

}