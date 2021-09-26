package com.chatowl.data.entities.chat.survey

import com.squareup.moshi.Json

data class Survey(
        @field:Json(name = "androidToken") val token: String
)