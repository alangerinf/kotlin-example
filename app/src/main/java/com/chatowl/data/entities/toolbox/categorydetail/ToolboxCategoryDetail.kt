package com.chatowl.data.entities.toolbox.categorydetail

import android.os.Parcelable
import com.chatowl.data.entities.toolbox.ToolboxItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ToolboxCategoryDetail(
    var id: Int = -1,
    var name: String = "",
    var type: String = "",
    val items: List<ToolboxItem> = emptyList()
) : Parcelable