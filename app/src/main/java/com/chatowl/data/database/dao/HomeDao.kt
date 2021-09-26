package com.chatowl.data.database.dao

import androidx.room.*
import com.chatowl.data.entities.home.*
import com.chatowl.data.entities.toolbox.Tool

@Dao
interface HomeDao {

    @Transaction
    suspend fun persistHome(homeData: HomeData) {
        // Clear data
        clearHome()
        // Insert program activities
        persistActivities(homeData.activitiesToday)
        // Insert suggested activities
        homeData.activitiesSuggested.forEach {
            it.isSuggested = true
        }
        persistActivities(homeData.activitiesSuggested)
        // Insert client information
        insertClientInformation(homeData.clientInfo)
    }

    @Transaction
    suspend fun clearHome() {
        clearActivities()
        clearClientInformation()
    }

    @Query("DELETE FROM activity")
    suspend fun clearActivities()

    @Query("DELETE FROM client_information")
    suspend fun clearClientInformation()

    @Transaction
    suspend fun getHomeData(): HomeData {
        val clientInformation = getClientInformation()
        val activitiesToday = getTodaysActivities().asProgramActivityList()
        val activitiesSuggested = getSuggestedActivities().asProgramActivityList()
        return HomeData(
            clientInfo = clientInformation,
            activitiesToday = activitiesToday,
            activitiesSuggested = activitiesSuggested
        )
    }

    @Query("SELECT * FROM client_information LIMIT 1")
    suspend fun getClientInformation(): ClientInformation

    @Transaction
    @Query("SELECT * FROM activity WHERE is_suggested = 0")
    suspend fun getTodaysActivities(): List<RoomProgramActivityWithTool>

    @Transaction
    @Query("SELECT * FROM activity WHERE is_suggested = 1")
    suspend fun getSuggestedActivities(): List<RoomProgramActivityWithTool>

    @Transaction
    private suspend fun persistActivities(activities: List<ProgramActivity>) {
        insertActivities(activities)
        activities.forEach { activity ->
            insertTool(activity.tool)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ProgramActivity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: Tool)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClientInformation(clientInformation: ClientInformation)
}