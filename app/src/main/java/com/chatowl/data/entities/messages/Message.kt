package com.chatowl.data.entities.messages

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chatowl.data.entities.journal.DRAFT_ID
import com.chatowl.data.entities.journal.EntryItem
import com.chatowl.data.entities.journal.EntryItemType
import com.chatowl.presentation.common.extensions.lowerCase
import com.chatowl.presentation.common.utils.getStringTimestampFromDate
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "message")
@Parcelize
data class Message(
    @PrimaryKey(autoGenerate = false)
    var id: String = DRAFT_ID,
    @ColumnInfo(name = "tool_id")
    var toolId: Int? = null,
    var date: String = getStringTimestampFromDate(serverTimeZone = true),
    var unread: Boolean = false,
    var components: List<EntryItem> = arrayListOf(),
    @Embedded(prefix = "response_")
    var reply: MessageReply? = null
) : Parcelable {

    fun cleanEmptyComponents() {
        components = components.filter {
            !(it.type.equals(EntryItemType.TEXT.name, true) && it.content?.isBlank() == true)
        }
    }

}

fun List<Message>.asMessagePreviewList(): List<MessagePreview> {
    return map { message ->
        val messagePreview = message.components.firstOrNull { it.type == EntryItemType.TEXT.name.lowerCase() }?.content ?: ""
        val isDraft = message.id == DRAFT_ID
        MessagePreview(
            id = message.id,
            isDraft = isDraft,
            date = message.date,
            preview = messagePreview,
            showBadge = message.unread,
            reply = message.reply
        )
    }
}

@Parcelize
data class MessageReply(
    val date: String,
    val answer: String
) : Parcelable

data class MessagePreview(
    val id: String,
    val isDraft: Boolean,
    val date: String,
    val preview: String,
    val showBadge: Boolean,
    val reply: MessageReply?=null
)