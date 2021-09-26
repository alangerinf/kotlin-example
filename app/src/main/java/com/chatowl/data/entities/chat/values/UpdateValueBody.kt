package com.chatowl.data.entities.chat.values

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateValueBody(
        val propertyId: Int,
        val newValue: String
) : Parcelable
