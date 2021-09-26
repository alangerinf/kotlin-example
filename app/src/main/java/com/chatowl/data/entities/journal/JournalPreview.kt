package com.chatowl.data.entities.journal

data class JournalPreview(
    val id: String,
    val description: String,
    val thumbnailUrl: String?,
    val iconResourceId: Int? = null,
    val date: String,
    val isDraft: Boolean = false
)