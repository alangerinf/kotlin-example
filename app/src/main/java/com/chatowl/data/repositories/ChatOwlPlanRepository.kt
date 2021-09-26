package com.chatowl.data.repositories

import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.entities.plan.ProgramDetail
import com.chatowl.data.entities.plan.ProgramHome

object ChatOwlPlanRepository {
    private val client = ChatOwlAPIClient

    suspend fun getPlanHome(): ProgramHome =
        client.getPlanHome().data

    suspend fun getPlanDetail(programId: Int): ProgramDetail =
        client.getPlanDetail(programId).data

    suspend fun changePlanWeight(programId: Int) =
        client.changePlanWeight(programId).success

    suspend fun changePlanFull(newProgramId: Int)=
        client.changePlanFull(newProgramId).success

}