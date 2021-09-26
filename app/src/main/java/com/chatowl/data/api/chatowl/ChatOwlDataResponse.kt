package com.chatowl.data.api.chatowl

data class ChatOwlDataResponse<T>(
        var success: Boolean,
        var data: T,
        var message: String?
)