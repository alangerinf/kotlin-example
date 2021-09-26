package com.chatowl.data.entities.toolbox.home

import android.os.Parcelable
import android.util.Log
import androidx.room.*
import com.chatowl.data.entities.toolbox.RoomToolboxItemWithTool
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.data.entities.toolbox.asToolboxItemList
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "toolbox_program")
@Parcelize
data class ToolboxProgram(
    @PrimaryKey(autoGenerate = false)
    var id: Int = -1,
    var name: String = "",
    @ColumnInfo(name = "is_active")
    var isActive: Boolean = false,
    @ColumnInfo(name = "paused_date")
    var pausedAt: String? = null,
    @Ignore
    @Json(name = "items") var exercises: List<ToolboxItem> = emptyList()
) : Parcelable

data class RoomProgramWithToolboxItem(
    @Embedded val program: ToolboxProgram,
    @Relation(
        entity = ToolboxItem::class,
        parentColumn = "id",
        entityColumn = "program_id"
    )
    val toolboxItems: List<RoomToolboxItemWithTool>
) {
    fun asToolboxProgram(): ToolboxProgram {
        val toolboxProgram = this.program
        toolboxProgram.exercises = this.toolboxItems.asToolboxItemList()
        return toolboxProgram
    }
}

fun List<RoomProgramWithToolboxItem>.asToolboxProgramList(): List<ToolboxProgram> {
        return map { roomProgram ->
                roomProgram.asToolboxProgram()
        }
}