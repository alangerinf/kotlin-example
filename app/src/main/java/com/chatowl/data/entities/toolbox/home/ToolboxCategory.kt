package com.chatowl.data.entities.toolbox.home

import android.os.Parcelable
import androidx.room.*
import com.chatowl.data.entities.toolbox.RoomToolboxItemWithTool
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.data.entities.toolbox.asToolboxItemList
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "toolbox_category")
@Parcelize
data class ToolboxCategory(
    @PrimaryKey(autoGenerate = false)
    var id: Int = -1,
    var name: String = "",
    @Ignore
    @Json(name = "items") var exercises: List<ToolboxItem> = emptyList()
) : Parcelable

data class RoomCategoryWithToolboxItem(
    @Embedded val category: ToolboxCategory,
    @Relation(
        entity = ToolboxItem::class,
        parentColumn = "id",
        entityColumn = "category_id"
    )
    val toolboxItems: List<RoomToolboxItemWithTool>
) {
        fun asToolboxCategory(): ToolboxCategory {
                val toolboxCategory = this.category
                toolboxCategory.exercises = this.toolboxItems.asToolboxItemList()
                return toolboxCategory
        }
}

fun List<RoomCategoryWithToolboxItem>.asToolboxCategoryList(): List<ToolboxCategory> {
    return map { roomCategory ->
        roomCategory.asToolboxCategory()
    }.onEach { toolboxProgram ->
        toolboxProgram.exercises.map { toolboxItem ->
            sortedBy { toolboxItem.displayOrder }
        }
    }
}


