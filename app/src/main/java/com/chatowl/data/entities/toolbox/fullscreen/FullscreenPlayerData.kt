package com.chatowl.data.entities.toolbox.fullscreen

import android.net.Uri
import android.os.Parcelable
import com.chatowl.data.entities.chat.ChatMessage
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FullscreenPlayerData(
    val name: String = "",
    val backgroundUrl: String = "",
    val mediaUri: Uri = Uri.parse(""),
    val position: Long = 0L,
    val fixedLandscape: Boolean = true
) : Parcelable {
    constructor(chatItem: ChatMessage) : this(
        chatItem.text ?: "",
        chatItem.thumbnailUrl ?: "",
        Uri.parse(chatItem.mediaUrl ?: ""),
        0
    )
}
