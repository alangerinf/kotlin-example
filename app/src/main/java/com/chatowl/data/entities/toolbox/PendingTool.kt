package com.chatowl.data.entities.toolbox

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PendingTool(
    var toolId: Int = -1,
    var toolName: String = "",
    @ColumnInfo(name = "tool_type")
    var toolType: String = "",
    @ColumnInfo(name = "tool_subtype")
    var toolSubtype: String = ""
) : Parcelable