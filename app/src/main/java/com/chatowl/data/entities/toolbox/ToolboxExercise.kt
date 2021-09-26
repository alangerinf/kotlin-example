package com.chatowl.data.entities.toolbox

import android.os.Parcelable
import androidx.room.*
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.getDateFromServerDateString
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "toolbox_exercise")
@Parcelize
data class ToolboxExercise(
        @PrimaryKey(autoGenerate = false)
        var id: Int = 0,
        @ColumnInfo(name = "is_locked")
        var isLocked: Boolean = false,
        @ColumnInfo(name = "is_paused")
        var isPaused: Boolean = false,
        @ColumnInfo(name = "is_paywalled")
        var isPaywalled: Boolean = false,
        @ColumnInfo(name = "suggested_items")
        var suggestedItems: List<ToolboxItem>? = null,
        @ColumnInfo(name = "time_last_watched")
        var timeLastWatched: String? = null,
        @Ignore
        var tool: Tool = Tool(),
        @ColumnInfo(name = "tool_id")
        var toolId: Int = tool.id,
        @Embedded(prefix = "pending_item_")
        var pendingItem: ToolboxItem? = null,
        @ColumnInfo(name = "watch_count")
        var watchCount: Int = 0,
        @ColumnInfo(name = "watch_position")
        var watchPosition: Long = 0,
        @ColumnInfo(name = "within_repeat_limit")
        var withinRepeatLimit: Boolean = true
) : Parcelable {
    constructor(tool: Tool) : this(
            0,
            false,
            false,
            false,
            null,
            null,
            tool,
            tool.id,
            null,
            0,
            0,
            true
    )

    constructor(imageChatItem: ChatMessage) : this(
            0,
            false,
            false,
            false,
            null,
            null,
            Tool(
                    toolType = ToolType.IMAGE.name.lowerCase(),
                    toolSubtype = ToolType.IMAGE.name.lowerCase(),
                    title = imageChatItem.text ?: "",
                    description = imageChatItem.text ?: "",
                    imageUrl = imageChatItem.mediaUrl
            ),
            -1,
            null,
            0,
            0,
            true
    )

    fun lockCheck(): Boolean {
        return this.isPaywalled || this.isLocked || !this.calculatedWithinRepeatLimit()
    }

    fun calculatedWithinRepeatLimit(): Boolean {
        return when {
            this.tool.repeatLimit == null -> true
            this.timeLastWatched.isNullOrBlank() -> true
            else -> {
                // Now
                val calendar = Calendar.getInstance()
                val now = calendar.time
                // Last time the exercise was watched
                val timeLastWatchedDate =
                    getDateFromServerDateString(this.timeLastWatched!!)
                calendar.time = timeLastWatchedDate
                // Add the repeat limit field to the time last watched
                calendar.add(Calendar.SECOND, this.tool.repeatLimit ?: 0)
                val nextAvailableTime = calendar.time
                // Check the status
                nextAvailableTime.before(now)
            }
        }
    }
}

data class RoomToolboxExerciseWithTool(
    @Embedded val toolboxExercise: ToolboxExercise,
    @Relation(
        parentColumn = "tool_id",
        entityColumn = "id"
    )
    val tool: Tool
) {
    fun asToolboxExercise(): ToolboxExercise {
        val toolboxExercise = this.toolboxExercise
        toolboxExercise.tool = this.tool
        return toolboxExercise
    }
}