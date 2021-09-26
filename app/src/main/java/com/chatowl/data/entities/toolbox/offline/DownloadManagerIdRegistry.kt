package com.chatowl.data.entities.toolbox.offline

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadManagerIdRegistry(
    val toolDownloadIds: MutableMap<Int, Long> = mutableMapOf()
) : Parcelable