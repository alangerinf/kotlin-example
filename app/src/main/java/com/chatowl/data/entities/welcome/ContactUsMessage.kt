package com.chatowl.data.entities.welcome

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContactUsMessage(val message:String, val language:String, val email:String): Parcelable {
}