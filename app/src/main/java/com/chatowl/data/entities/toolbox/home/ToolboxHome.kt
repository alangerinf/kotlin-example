package com.chatowl.data.entities.toolbox.home

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ToolboxHome(
        val toolboxPrograms: List<ToolboxProgram> = emptyList(),
        val toolboxCategories: List<ToolboxCategory> = emptyList()
) : Parcelable {
    fun isEmpty(): Boolean {
        return toolboxPrograms.isEmpty() && toolboxCategories.isEmpty()
    }
}