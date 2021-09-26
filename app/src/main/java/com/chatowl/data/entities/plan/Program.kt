package com.chatowl.data.entities.plan

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Program(
    val id: Int,
    val name: String,
    val tagline: String,
    val programDescription: ProgramDescription,
    val isActive: Boolean,
    val version: String,
    val weekActivities: List<ProgramWeek>
):Parcelable {
    fun getFullProgramDescription(): FullProgramDescription {
        return FullProgramDescription(this)
    }
}

enum class ProgramVersion {
    SLIM,
    FULL
}