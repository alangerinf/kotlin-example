package com.chatowl.data.entities.journal

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chatowl.R
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.getStringTimestampFromDate
import kotlinx.android.parcel.Parcelize

const val DRAFT_ID = "draft_id"

@Entity(tableName = "journal")
@Parcelize
data class Journal(
    @PrimaryKey(autoGenerate = false)
    var id: String = DRAFT_ID,
    @ColumnInfo(name = "tool_id")
    var toolId: Int? = null,
    var date: String = getStringTimestampFromDate(serverTimeZone = true),
    var components: List<EntryItem> = arrayListOf(),
    var eDeleted: Boolean = false,
    var eUpdated: Boolean = false,
    var draftNoSaved: Boolean = false
) : Parcelable {

    fun cleanEmptyComponents() {
        components = components.filter {
            !(it.type.equals(EntryItemType.TEXT.name, true) && it.content?.isBlank() == true)
        }
    }

}

fun List<Journal>.asJournalPreviewList(): List<JournalPreview> {
    return map { journal ->
        val id = journal.id
        val isDraft = id == DRAFT_ID
        val description = journal.components.firstOrNull { it.type == EntryItemType.TEXT.name.lowerCase() }?.content ?: ""
        val entry = journal.components.firstOrNull { it.type == EntryItemType.IMAGE.name.lowerCase() }
        val thumbnailUrl  = entry?.uri?: entry?.mediaUrl

        val firstMediaType = if(thumbnailUrl != null) null else journal.components.firstOrNull { it.type == EntryItemType.AUDIO.name.lowerCase() || it.type == EntryItemType.VIDEO.name.lowerCase()}?.type
        val iconResourceId = when (firstMediaType) {
            EntryItemType.AUDIO.name.lowerCase() -> {
                R.drawable.ic_media_type_audio
            }
            EntryItemType.VIDEO.name.lowerCase() -> {
                R.drawable.ic_media_type_video
            }
            else -> {
                null
            }
        }

        JournalPreview(
            id = id,
            description = description,
            thumbnailUrl = thumbnailUrl,
            iconResourceId = iconResourceId,
            date = journal.date,
            isDraft = isDraft
        )
    }
}