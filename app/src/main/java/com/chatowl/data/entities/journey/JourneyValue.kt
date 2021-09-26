package com.chatowl.data.entities.journey

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class JourneyValue(
    val date: String,
    val value: Float
): Parcelable