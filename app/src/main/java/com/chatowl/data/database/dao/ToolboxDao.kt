package com.chatowl.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.entities.toolbox.home.*
import com.chatowl.data.entities.toolbox.offline.OfflineEvent

@Dao
interface ToolboxDao {

    @Insert
    suspend fun insertPrograms(toolboxPrograms: List<ToolboxProgram>)

    @Insert
    suspend fun insertCategories(toolboxCategories: List<ToolboxCategory>)

    @Transaction
    @Query("SELECT * FROM toolbox_category")
    fun getToolboxCategories(): LiveData<List<RoomCategoryWithToolboxItem>>

    @Transaction
    @Query("SELECT * FROM toolbox_program")
    fun getToolboxPrograms(): LiveData<List<RoomProgramWithToolboxItem>>

    @Transaction
    suspend fun persistToolboxHome(toolboxHome: ToolboxHome) {
        // Clear data
        clearToolboxHome()
        // Insert programs
        insertPrograms(toolboxHome.toolboxPrograms)
        // Insert toolbox items
        toolboxHome.toolboxPrograms.forEach { program ->
            insertToolboxItems(program.exercises.map { it.apply { programIdKey = programId?: -1 } })
            // Insert tools
            program.exercises.forEach { toolboxItem ->
                toolboxItem.programIdKey = toolboxItem.programId?: -1
                insertTool(toolboxItem.tool)
            }
        }
        // Insert categories
        insertCategories(toolboxHome.toolboxCategories)
        // Insert toolbox items
        toolboxHome.toolboxCategories.forEach { category ->
            insertToolboxItems(category.exercises.map { it.apply { programIdKey = programId?: -1 } })
            // Insert tools
            category.exercises.forEach { toolboxItem ->
                insertTool(toolboxItem.tool)
            }
        }
    }

    @Query("SELECT * FROM toolbox_program")
    suspend fun getPrograms(): List<ToolboxProgram>

    @Query("SELECT * FROM toolbox_category")
    suspend fun getCategories(): List<ToolboxCategory>

    @Transaction
    suspend fun clearToolboxHome() {
        clearToolboxPrograms()
        clearToolboxCategories()
        clearToolboxItems()
    }

    @Query("DELETE FROM toolbox_program")
    suspend fun clearToolboxPrograms()

    @Query("DELETE FROM toolbox_category")
    suspend fun clearToolboxCategories()

    @Query("DELETE FROM toolbox_item")
    suspend fun clearToolboxItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToolboxItems(toolboxItems: List<ToolboxItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: Tool)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTools(tools: List<Tool>)

    @Transaction
    @Query("SELECT * FROM toolbox_item WHERE category_id = :categoryId")
    fun getCategoryToolboxItemsLiveData(categoryId: Int): LiveData<List<RoomToolboxItemWithTool>>

    @Transaction
    @Query("SELECT * FROM toolbox_item WHERE category_id = :categoryId")
    suspend fun getCategoryToolboxItems(categoryId: Int): List<RoomToolboxItemWithTool>

    @Transaction
    @Query("SELECT * FROM toolbox_item WHERE program_id = :programId")
    fun getProgramToolboxItemsLiveData(programId: Int): LiveData<List<RoomToolboxItemWithTool>>

    @Transaction
    @Query("SELECT * FROM toolbox_item WHERE program_id = :programId")
    suspend fun getProgramToolboxItems(programId: Int): List<RoomToolboxItemWithTool>

    @Transaction
    @Query("SELECT * FROM tool WHERE is_favorite = 1")
    fun getFavoriteToolboxItemsLiveData(): LiveData<List<RoomToolWithParent>>

    @Transaction
    suspend fun persistToolboxExercise(toolboxExercise: ToolboxExercise) {
        insertToolboxExercise(toolboxExercise)
        insertTool(toolboxExercise.tool)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToolboxExercise(toolboxExercise: ToolboxExercise)

    @Transaction
    @Query("SELECT * FROM toolbox_exercise WHERE id = :toolboxExerciseId")
    suspend fun getToolboxExercise(toolboxExerciseId: Int): RoomToolboxExerciseWithTool?

    @Query("UPDATE toolbox_exercise SET watch_position =:position WHERE id =:toolboxExerciseId")
    suspend fun updateExerciseWatchPosition(toolboxExerciseId: Int, position: Long? = null)

    @Transaction
    suspend fun persistRepeatInformation(
        toolboxExerciseId: Int,
        newTimeLastWatched: String,
        newWithinRepeatLimit: Boolean
    ) {
        updateToolboxExerciseRepeatInformation(
            toolboxExerciseId,
            newTimeLastWatched
        )
        updateToolboxItemRepeatInformation(
            toolboxExerciseId,
            newTimeLastWatched
        )
    }

    @Query("UPDATE toolbox_exercise SET time_last_watched =:newTimeLastWatched WHERE id =:toolboxExerciseId")
    suspend fun updateToolboxExerciseRepeatInformation(
        toolboxExerciseId: Int,
        newTimeLastWatched: String
    )

    @Query("UPDATE toolbox_item SET time_last_watched =:newTimeLastWatched WHERE id =:toolboxItemId")
    suspend fun updateToolboxItemRepeatInformation(
        toolboxItemId: Int,
        newTimeLastWatched: String
    )

    @Query("SELECT * FROM toolbox_exercise WHERE id = :toolboxExerciseId")
    fun getToolboxExerciseLiveData(toolboxExerciseId: Int): LiveData<ToolboxExercise>

    @Insert
    suspend fun insertOfflineEvent(offlineEvent: OfflineEvent)

    @Query("SELECT * FROM offline_event")
    suspend fun getOfflineEvents(): List<OfflineEvent>

    @Delete
    suspend fun deleteOfflineEvent(offlineEvent: OfflineEvent)

    @Query("UPDATE tool SET is_favorite =:add WHERE id =:toolId")
    suspend fun setToolAsFavorite(toolId: Int, add: Boolean)
}