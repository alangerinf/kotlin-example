package com.chatowl.data.entities.chat.flows

import com.squareup.moshi.Json

data class Flow(
        @Json(name = "sessionId") val id: Int,
        val name: String? = null
)