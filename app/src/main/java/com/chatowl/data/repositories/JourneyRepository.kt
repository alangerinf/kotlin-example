package com.chatowl.data.repositories

import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.database.dao.JourneyDao
import com.chatowl.data.entities.journey.GetJourneyResponse
import com.chatowl.presentation.journey.JourneyTimeOption

class JourneyRepository(private val journeyDao: JourneyDao) {

    private val client = ChatOwlAPIClient

    suspend fun getJourneyResponse(dateUnit: String, startDate: String, endDate: String): GetJourneyResponse {
        val id = when (dateUnit) {
            JourneyTimeOption.days.toString() -> 1
            JourneyTimeOption.months.toString() -> 2
            JourneyTimeOption.weeks.toString() -> 3
            else -> -1
        }
        return try {
            val response = client.getJourney(dateUnit, startDate, endDate).data
            response.id = id
            val isSavedSuccessfully = journeyDao.insertJourneyResponse(response)
            if (isSavedSuccessfully != -1L) journeyDao.getJourneyResponse(id)!! else response
        } catch (e: Exception) {
            val data = journeyDao.getJourneyResponse(id)
            data ?: throw e
        }
    }
}