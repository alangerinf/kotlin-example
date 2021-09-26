package com.chatowl.data.entities.plan

import android.os.Parcelable
import com.chatowl.data.entities.toolbox.Tool
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProgramDetail(
    var id: Int = 0,
    var fullId: Int = 0,
    var slimId: Int = 0,
    val name: String,
    val tagline: String,
    val programDescription: ProgramDescription,
    val isActive: Boolean,
    val assessmentTool: Tool?,
    val thumbnailUrl: String,
    val slim: List<ProgramWeek>,
    val full: List<ProgramWeek>
) : Parcelable {
    fun getFullProgramDescription(): FullProgramDescription {
        return FullProgramDescription(this)
    }
    fun getProgramPreview(): ProgramPreview {
        return ProgramPreview(
            id,
            name,
            tagline,
            thumbnailUrl
        )
    }
}