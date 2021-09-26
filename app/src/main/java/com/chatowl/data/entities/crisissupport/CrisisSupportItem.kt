package com.chatowl.data.entities.crisissupport

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FullCrisisSupportResponse(
        val data: List<CrisisSupportItem>,
        val success: Boolean
) : Parcelable

@Parcelize
data class CrisisSupportItem(
        val id: Int,
        val name: String,
        val number: String
) : Parcelable