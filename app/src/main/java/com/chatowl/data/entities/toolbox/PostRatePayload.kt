package com.chatowl.data.entities.toolbox

import android.os.Parcelable
import androidx.room.*
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.getDateFromServerDateString
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class PostRatePayload(
    var toolId: Int,
    var rating: Int,
    var comment: String?=null,
    val language: String = "en-US"
) : Parcelable