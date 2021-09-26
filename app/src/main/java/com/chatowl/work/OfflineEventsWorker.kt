package com.chatowl.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.toolbox.event.ToolEvent
import com.chatowl.data.entities.toolbox.offline.OfflineEventType
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.extensions.isConnectionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineEventsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val OFFLINE_EVENTS_WORKER_NAME = "offline_events_worker"
        const val WORKER_BACKOFF_DELAY_IN_MINUTES = 5L
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val toolboxRepository =
            ToolboxRepository(ChatOwlDatabase.getInstance(applicationContext).toolboxDao)

        val pendingEvents = toolboxRepository.getOfflineEvents()
        if (pendingEvents.isEmpty()) {
            Result.success()
        } else {
            pendingEvents.forEach { offlineEvent ->
                try {
                    when (OfflineEventType.valueOf(offlineEvent.type.toUpperCase())) {
                        OfflineEventType.EXERCISE_STARTED -> {
                            toolboxRepository.postEvent(
                                offlineEvent.toolId ?: -1,
                                ToolEvent.STARTED,
                                null
                            )
                        }
                        OfflineEventType.EXERCISE_PAUSED -> {
                            toolboxRepository.postEvent(
                                offlineEvent.toolId ?: -1,
                                ToolEvent.PAUSED,
                                offlineEvent.payload?.toLong()
                            )
                        }
                        OfflineEventType.EXERCISE_RESUMED -> {
                            toolboxRepository.postEvent(
                                offlineEvent.toolId ?: -1,
                                ToolEvent.RESUMED,
                                null
                            )
                        }
                        OfflineEventType.EXERCISE_FINISHED -> {
                            toolboxRepository.postEvent(
                                offlineEvent.toolId ?: -1,
                                ToolEvent.FINISHED,
                                null
                            )
                        }
                        OfflineEventType.EXERCISE_SAVE_FAVORITE -> {
                            ChatOwlAPIClient.addToFavorites(offlineEvent.toolId ?: -1)
                        }
                        OfflineEventType.EXERCISE_REMOVE_FAVORITE -> {
                            ChatOwlAPIClient.removeFromFavorites(offlineEvent.toolId ?: -1)
                        }
                    }
                    toolboxRepository.deleteOfflineEvent(offlineEvent)
                } catch (e: Exception) {
                    if (!e.isConnectionException()) {
                        toolboxRepository.deleteOfflineEvent(offlineEvent)
                    }
                }
            }

            val remainingEvents = toolboxRepository.getOfflineEvents()
            if (remainingEvents.isEmpty()) {
                Result.success()
            } else {
                Result.retry()
            }
        }
    }
}