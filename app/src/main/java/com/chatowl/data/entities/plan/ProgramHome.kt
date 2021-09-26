package com.chatowl.data.entities.plan

data class ProgramHome(
    var activePlan: Program?,
    val availablePlansDto: List<ProgramPreview>
)