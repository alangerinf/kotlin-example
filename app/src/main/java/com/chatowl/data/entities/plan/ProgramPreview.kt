package com.chatowl.data.entities.plan

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProgramPreview(
    val id: Int,
    val name: String,
    val tagline: String,
    val thumbnailUrl: String
) : Parcelable