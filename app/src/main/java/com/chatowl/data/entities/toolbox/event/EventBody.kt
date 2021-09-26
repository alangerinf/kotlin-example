package com.chatowl.data.entities.toolbox.event

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventBody(
        val state: String,
        val position: Long?
) : Parcelable
