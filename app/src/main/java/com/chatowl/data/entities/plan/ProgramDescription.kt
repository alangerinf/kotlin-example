package com.chatowl.data.entities.plan

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProgramDescription(
    val intro: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String
) : Parcelable

@Parcelize
data class FullProgramDescription(
    val name: String,
    val tagLine: String,
    val intro: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String
) : Parcelable {

    constructor(program: Program) : this(
        program.name,
        program.tagline,
        program.programDescription.intro,
        program.programDescription.description,
        program.programDescription.thumbnailUrl,
        program.programDescription.videoUrl
    )

    constructor(programDetail: ProgramDetail) : this(
        programDetail.name,
        programDetail.tagline,
        programDetail.programDescription.intro,
        programDetail.programDescription.description,
        programDetail.programDescription.thumbnailUrl,
        programDetail.programDescription.videoUrl
    )
}