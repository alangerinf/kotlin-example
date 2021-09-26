package com.chatowl.data.entities.chat.socketerror

import com.squareup.moshi.Json

class SocketError(
    @field:Json(name = "name") val errorCode: String
)

enum class SocketErrorCode {
    TOKEN_INVALID
}