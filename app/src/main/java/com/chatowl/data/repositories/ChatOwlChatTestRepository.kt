package com.chatowl.data.repositories

import com.chatowl.data.api.chatowl.ChatOwlAPIClient

object ChatOwlChatTestRepository {
    private val client = ChatOwlAPIClient

    suspend fun getFlows() =
        client.getFlows().data

    suspend fun getPropertyValues() =
        client.getValues().data

    suspend fun setPropertyValue(id: Int, value: String) =
        client.setValue(id, value).data

}