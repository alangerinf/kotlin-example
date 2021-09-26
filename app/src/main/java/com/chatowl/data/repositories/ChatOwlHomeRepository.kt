package com.chatowl.data.repositories

import android.util.Log
import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.database.dao.HomeDao
import com.chatowl.data.entities.home.ClientInformation
import com.chatowl.data.entities.home.HomeData
import com.chatowl.data.entities.toolbox.ToolAvailability
import com.chatowl.data.entities.toolbox.ToolboxExercise

class ChatOwlHomeRepository(private val homeDao: HomeDao) {
    private val client = ChatOwlAPIClient

    private val TAG = this::class.java.simpleName

    suspend fun getHomeData(): HomeData {
        return try {
            val homeData = client.getHome().data
            homeDao.persistHome(homeData)
            homeData
        } catch (e: Exception) {
            val homeData = homeDao.getHomeData()
            if (homeData.isEmpty()) {
                Log.e(TAG, e.toString())
                throw e
            } else {
                homeData
            }
        }
    }

    suspend fun getClientInfoFromDB(): ClientInformation {
        return try {
            //val homeData = client.getHome()
            homeDao.getClientInformation()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            val homeData = client.getHome().data
            homeData.clientInfo
        }
    }


    suspend fun getActivityExercise(activityId: Int): ToolboxExercise {
        return client.getActivityExercise(activityId).data
    }

    suspend fun getToolAvailability(toolId: Int): ToolAvailability {
        return client.getToolAvailability(toolId).data
    }
}