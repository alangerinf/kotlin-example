package com.chatowl.data.entities.journal

data class GetJournalsResponse(
    val journals: List<Journal>,
    val total: Int
)