package com.chatowl.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chatowl.data.entities.journey.GetJourneyResponse

@Dao
interface JourneyDao {

    @Transaction
    @Query("SELECT * FROM get_journey_response  WHERE id = :id")
    fun getJourneyResponse(id: Int): GetJourneyResponse?

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourneyResponse(getJourneyResponse: GetJourneyResponse): Long

    @Query("DELETE FROM get_journey_response")
    suspend fun deleteJourneyResponse()
}