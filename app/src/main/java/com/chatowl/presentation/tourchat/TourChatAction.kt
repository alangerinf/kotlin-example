package com.chatowl.presentation.tourchat

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TourChatAction(
    val name: String,
    val data: Int
) : Parcelable