package com.chatowl.data.entities.journal.gallery

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GalleryItem(
    val id: Long,
    val displayName: String,
    val mediaType: Int,
    val mimeType: String,
    val contentUri: Uri
) : Parcelable