package com.chatowl.data.entities.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateTimezoneBody(
        val timezone: String
) : Parcelable
