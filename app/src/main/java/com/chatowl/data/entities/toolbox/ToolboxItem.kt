package com.chatowl.data.entities.toolbox

import android.os.Parcelable
import androidx.room.*
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroupItem
import com.chatowl.presentation.common.utils.getDateFromServerDateString
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "toolbox_item", primaryKeys = ["id","program_id_key"])
@Parcelize
data class ToolboxItem(

    var id: Int = -1,
    @ColumnInfo(name = "category_id")
    var categoryId: Int = -1,
    @ColumnInfo(name = "program_id_key")
    var programIdKey: Int = -1,
    @ColumnInfo(name = "program_id")
    var programId: Int? = -1,
    @ColumnInfo(name = "is_new")
    var isNew: Boolean = false,
    @ColumnInfo(name = "is_paywalled")
    var isPaywalled: Boolean = false,
    @ColumnInfo(name = "is_suggested")
    var isSuggested: Boolean = false,
    @ColumnInfo(name = "is_due")
    var isDue: Boolean = false,
    @ColumnInfo(name = "created_at")
    var createdAt: String = "",
    @ColumnInfo(name = "time_last_watched")
    var timeLastWatched: String? = null,
    @Embedded(prefix = "pending_item_")
    var pendingTool: PendingTool? = null,
    @Ignore
    var tool: Tool = Tool(),
    @ColumnInfo(name = "tool_id")
    var toolId: Int = tool.id,
    @ColumnInfo(name = "is_locked")
    var isLocked: Boolean = true,
    @ColumnInfo(name = "days_until_activity")
    var daysUntilActivity: Int = 0,
    var displayOrder: Int = 0,
) : Parcelable {

    companion object{
        fun deleteDuplicates(toolboxItemList: List<ToolboxItem>): List<ToolboxItem>{
            val result = mutableListOf<ToolboxItem>()
            for (groupItem in toolboxItemList){
                if (result.find{ it.id == groupItem.id } == null){
                    result.add(groupItem)
                }
            }
            return result
        }
    }
    fun withinRepeatLimit(): Boolean {
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

enum class ToolboxCategoryType {
    CATEGORY,
    PROGRAM,
    FAVORITES
}

data class RoomToolboxItemWithTool(
    @Embedded val toolboxItem: ToolboxItem,
    @Relation(
        parentColumn = "tool_id",
        entityColumn = "id"
    )
    val tool: Tool
) {
    fun asToolboxItem(): ToolboxItem {
        val toolboxItem = this.toolboxItem
        toolboxItem.tool = this.tool
        return toolboxItem
    }
}

fun List<RoomToolboxItemWithTool>.asToolboxItemList(): List<ToolboxItem> {
    return map { toolboxItem ->
        toolboxItem.asToolboxItem()
    }.sortedBy { item -> item.displayOrder }
}