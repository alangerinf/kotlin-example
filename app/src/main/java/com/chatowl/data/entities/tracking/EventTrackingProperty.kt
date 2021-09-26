package com.chatowl.data.entities.tracking

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventTrackingProperty(
        val name: String?,
        val value: String?
) : Parcelable


