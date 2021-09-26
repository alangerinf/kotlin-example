package com.chatowl.data.entities.toolbox.offline

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "offline_event")
@Parcelize
data class OfflineEvent(
    @PrimaryKey (autoGenerate = true)
    var id: Int = 0,
    var type: String = "",
    @ColumnInfo(name = "tool_id")
    var toolId: Int? = null,
    var payload: String? = null
) : Parcelable

enum class OfflineEventType {
    EXERCISE_STARTED,
    EXERCISE_PAUSED,
    EXERCISE_RESUMED,
    EXERCISE_FINISHED,
    EXERCISE_SAVE_FAVORITE,
    EXERCISE_REMOVE_FAVORITE
}