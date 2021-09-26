package com.chatowl.data.entities.settings.apppreferences

import android.os.Parcelable
import com.chatowl.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeedbackMessage(val message:String, val language:String, val type:String):Parcelable {
}
enum class FeedbackType {
    FEEDBACK,
    `CONTACT-US`
}
