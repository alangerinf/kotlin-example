package com.chatowl.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.database.dao.ToolboxDao
import com.chatowl.data.entities.toolbox.ToolboxExercise
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.entities.toolbox.categorydetail.ToolboxCategoryDetail
import com.chatowl.data.entities.toolbox.event.ToolEvent
import com.chatowl.data.entities.toolbox.event.EventBody
import com.chatowl.data.entities.toolbox.home.ToolboxCategory
import com.chatowl.data.entities.toolbox.home.ToolboxProgram
import com.chatowl.data.entities.toolbox.home.asToolboxCategoryList
import com.chatowl.data.entities.toolbox.home.asToolboxProgramList
import com.chatowl.data.entities.toolbox.offline.OfflineEvent
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.extensions.upperCase

class ToolboxRepository(private val toolboxDao: ToolboxDao) {

    private val client = ChatOwlAPIClient

    suspend fun postEvent(
        toolboxItemId: Int,
        toolEvent: ToolEvent,
        position: Long?
    ) =
        client.postEvent(toolboxItemId, EventBody(toolEvent.name.lowerCase(), position)).data

    fun getPrograms(): LiveData<List<ToolboxProgram>> {
        return toolboxDao.getToolboxPrograms().map {
            it.asToolboxProgramList()
        }
    }

    fun getCategories(): LiveData<List<ToolboxCategory>> {
        return toolboxDao.getToolboxCategories().map {
            it.asToolboxCategoryList()
        }
    }

    suspend fun checkFeedbackToolAvailability(id: Int): Boolean {
        return (client.getToolFeedBackAvailability(id).data as? GetFeedbackAvailabilityResponse)
            ?.showFeedback ?:false
    }

    suspend fun getMoodMeters(): List<ToolboxMoodMeter> {
        return client.getToolboxMoodMeters().data
    }

    suspend fun sendToolboxMood(moodId: String, intensity: Int): Boolean {
        return  client.addToolboxMood(moodId, intensity).success
    }



    suspend fun refreshHome() {
        val toolboxHome = client.getToolboxHome().data
        toolboxDao.persistToolboxHome(toolboxHome)
    }

    fun getCategoryToolboxItems(toolboxCategoryDetail: ToolboxCategoryDetail): LiveData<ToolboxCategoryDetail> {
        return when (ToolboxCategoryType.valueOf(toolboxCategoryDetail.type.upperCase())) {
            ToolboxCategoryType.CATEGORY -> {
                toolboxDao.getCategoryToolboxItemsLiveData(
                    toolboxCategoryDetail.id,
                ).map {
                    toolboxCategoryDetail.copy(items = it.asToolboxItemList())
                }
            }
            ToolboxCategoryType.PROGRAM -> {
                toolboxDao.getProgramToolboxItemsLiveData(
                    toolboxCategoryDetail.id,
                ).map {
                    toolboxCategoryDetail.copy(items = it.asToolboxItemList())
                }
            }
            ToolboxCategoryType.FAVORITES -> {
                toolboxDao.getFavoriteToolboxItemsLiveData().map {
                    toolboxCategoryDetail.copy(items = it.asToolboxItemList())
                }
            }
        }
    }

    suspend fun getToolboxExercise(toolboxItemId: Int): ToolboxExercise {
        return try {
            val toolboxExercise = client.getToolboxExercise(toolboxItemId).data
            toolboxDao.persistToolboxExercise(toolboxExercise)
            toolboxExercise
        } catch (e: Exception) {
            val exercise = toolboxDao.getToolboxExercise(toolboxItemId)
            exercise?.asToolboxExercise() ?: throw e
        }
    }

    suspend fun setAsFavorite(add: Boolean, toolId: Int) {
        toolboxDao.setToolAsFavorite(toolId, add)
        if (add) {
            client.addToFavorites(toolId)
        } else {
            client.removeFromFavorites(toolId)
        }
    }

    suspend fun insertOfflineEvent(offlineEvent: OfflineEvent) {
        toolboxDao.insertOfflineEvent(offlineEvent)
    }

    suspend fun updateExerciseWatchPosition(exerciseId: Int, position: Long?) {
        toolboxDao.updateExerciseWatchPosition(exerciseId, position)
    }

    suspend fun persistRepeatInformation(
        exerciseId: Int,
        newTimeLastWatchedTimestamp: String,
        newWithinRepeatLimit: Boolean
    ) {
        toolboxDao.persistRepeatInformation(
            exerciseId,
            newTimeLastWatchedTimestamp,
            newWithinRepeatLimit
        )
    }

    suspend fun getOfflineEvents(): List<OfflineEvent> {
        return toolboxDao.getOfflineEvents()
    }

    suspend fun deleteOfflineEvent(offlineEvent: OfflineEvent) {
        toolboxDao.deleteOfflineEvent(offlineEvent)
    }
}