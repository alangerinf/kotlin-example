package com.chatowl.data.entities.home

import android.os.Parcelable
import androidx.room.*
import com.chatowl.data.entities.toolbox.PendingTool
import com.chatowl.data.entities.toolbox.Tool
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "activity")
@Parcelize
data class ProgramActivity(
    @PrimaryKey(autoGenerate = false)
    var id: Int = -1,
    @ColumnInfo(name = "display_order")
    var displayOrder: Int = 0,
    @ColumnInfo(name = "is_completed")
    var isCompleted: Boolean = false,
    @ColumnInfo(name = "is_mandatory")
    var isMandatory: Boolean = false,
    @ColumnInfo(name = "is_assessment")
    var isAssessment: Boolean = false,
    @ColumnInfo(name = "is_free")
    var isFree: Boolean = true,
    @ColumnInfo(name = "is_locked")
    var isLocked: Boolean = true,
    @ColumnInfo(name = "is_new")
    var isNew: Boolean = false,
    @ColumnInfo(name = "is_ongoing")
    var isOngoing: Boolean = false,
    @ColumnInfo(name = "is_suggested")
    var isSuggested: Boolean = false,
    @ColumnInfo(name = "days_until_activity")
    var daysUntilActivity: Int = 0,

    @Embedded(prefix = "pending_item_")
    var pendingTool: PendingTool? = null,
    @Ignore
    var tool: Tool = Tool(),
    @ColumnInfo(name = "tool_id")
    var toolId: Int = tool.id
) : Parcelable {
    companion object {
        const val TYPE_REGULAR: Int = 0
        const val TYPE_OVERVIEW: Int = 1
    }

    @Transient
    var type = TYPE_REGULAR
}

data class RoomProgramActivityWithTool(
    @Embedded val programActivity: ProgramActivity,
    @Relation(
        parentColumn = "tool_id",
        entityColumn = "id"
    )
    val tool: Tool
) {
    fun asProgramActivity(): ProgramActivity {
        val programActivity = this.programActivity
        programActivity.tool = this.tool
        return programActivity
    }
}

fun List<RoomProgramActivityWithTool>.asProgramActivityList(): List<ProgramActivity> {
    return this.map {
        it.asProgramActivity()
    }
}
