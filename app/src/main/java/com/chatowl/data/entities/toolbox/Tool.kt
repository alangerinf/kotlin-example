package com.chatowl.data.entities.toolbox

import android.os.Parcelable
import androidx.room.*
import com.chatowl.presentation.common.extensions.upperCase
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "tool")
@Parcelize
data class Tool(
    @PrimaryKey(autoGenerate = false)
    var id: Int = -1,
    @ColumnInfo(name = "category_id")
    var categoryId: Int? = -1,
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,
    @ColumnInfo(name = "tool_type")
    var toolType: String = "",
    @ColumnInfo(name = "tool_subtype")
    var toolSubtype: String = "",
    var title: String = "",
    var description: String? = "",
    @ColumnInfo(name = "media_type")
    var mediaType: String? = null,
    @ColumnInfo(name = "repeat_limit")
    var repeatLimit: Int? = null,
    var duration: Int? = null,
    @ColumnInfo(name = "quote_content")
    var quoteContent: String? = null,
    @ColumnInfo(name = "quote_author")
    var quoteAuthor: String? = null,
    @ColumnInfo(name = "thumbnail_url")
    var thumbnailUrl: String = "",
    @ColumnInfo(name = "image_url")
    var imageUrl: String? = null,
    @Embedded(prefix = "category_")
    var toolCategory: ToolCategory? = null,
    @ColumnInfo(name = "media_landscape_url")
    var mediaLandscapeUrl: String? = null,
    @ColumnInfo(name = "media_portrait_url")
    var mediaPortraitUrl: String? = null,
    @ColumnInfo(name = "image_portrait_url")
    var imagePortraitUrl: String? = null,
    @ColumnInfo(name = "image_landscape_url")
    var imageLandscapeUrl: String? = null,
    var instructions: List<String>? = null
) : Parcelable {

    fun toolIcon(): Int {
        return ToolSubtype.valueOf(toolSubtype.upperCase()).drawableId
    }

    fun toolboxIcon(): Int {
        return ToolSubtype.valueOf(toolSubtype.upperCase()).toolboxDrawableId
    }

    fun toToolboxExercise(): ToolboxExercise {
        return ToolboxExercise(this)
    }
}

data class RoomToolWithParent(
    @Embedded val tool: Tool,
    @Relation(
        parentColumn = "id",
        entityColumn = "tool_id"
    )
    val toolboxItem: ToolboxItem
) {
    fun asToolboxItem(): ToolboxItem {
        val toolboxItem = this.toolboxItem
        toolboxItem.tool = this.tool
        return toolboxItem
    }
}

fun List<RoomToolWithParent>.asToolboxItemList(): List<ToolboxItem> {
    return map { toolboxItem ->
        toolboxItem.asToolboxItem()
    }.sortedBy { item -> item.displayOrder }
}

@Parcelize
data class ToolCategory(
    val name: String? = null,
    val description: String? = null,
    val color: String? = null
) : Parcelable

@Parcelize
data class ToolAvailability(

    var isRepeatable: Boolean,
    var repeatLimit: Long,
    var alreadyDone: Boolean

) : Parcelable {
    companion object {
        public fun getCanPlayStatus(toolAvailability: ToolAvailability): CanPlayStatus {
            if (toolAvailability.alreadyDone && !toolAvailability.isRepeatable) {
                return CanPlayStatus.CANNOT_PLAY
            } else if (toolAvailability.isRepeatable && (toolAvailability.repeatLimit > 0)) {
                return CanPlayStatus.HAVE_TO_WAIT
            } else {
                return CanPlayStatus.CAN_PLAY
            }
        }
    }
}

public enum class CanPlayStatus {
    CAN_PLAY,
    HAVE_TO_WAIT,
    CANNOT_PLAY
}

enum class ExerciseInstruction {
    COMFORTABLE,
    DIM_LIGHTS,
    HEADPHONES
}