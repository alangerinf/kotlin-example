package com.chatowl.data.entities.chat

import android.os.Parcelable
import androidx.room.*
import com.chatowl.data.entities.chat.ChatMessageSender.BOT
import com.chatowl.data.entities.toolbox.Tool
import com.chatowl.data.entities.toolbox.ToolboxExercise
import com.chatowl.data.entities.toolbox.fullscreen.FullscreenPlayerData
import com.chatowl.presentation.chat.*
import com.chatowl.presentation.common.extensions.upperCase
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
@Entity(tableName = "chat_message")
data class ChatMessage(
    @PrimaryKey(autoGenerate = false)
    var id: String = "",
    @ColumnInfo(name = "bot_step_id")
    var botStepId: Int = -1,
    @ColumnInfo(name = "bot_choice_id")
    var botChoiceId: Int? = null,
    var order: Int = 0,
    var sender: String = "",
    @ColumnInfo(name = "message_type")
    var messageType: String = "",
    var timestamp: String = "",
    var text: String? = null,
    @ColumnInfo(name = "custom_answer")
    var customAnswer: String? = null,
    var metadata: String = "",
    @ColumnInfo(name = "sub_text")
    var subText: String? = null,
    @Ignore
    var tool: Tool? = null,
    @ColumnInfo(name = "tool_id")
    var toolId: Int? = tool?.id,
    @Ignore
    @field:Json(name = "data") val messageData: @RawValue MessageData? = null,
    @ColumnInfo(name = "thumbnail_url")
    var thumbnailUrl: String? = null,
    @ColumnInfo(name = "media_url")
    var mediaUrl: String? = null,
    @Embedded(prefix = "question_")
    var question: @RawValue Question? = null,
    var sent: Boolean = true,
    var textToSpeechUrl: String? = null,
    @Ignore
    var isAlreadyRead: Boolean = false
):Parcelable {
    fun getChatItemType(): Int {
        val senderValue = ChatMessageSender.valueOf(sender.upperCase())
        val messageTypeValue = ChatMessageType.valueOf(messageType.upperCase())
        return if (senderValue == BOT) {
            when (messageTypeValue) {
                ChatMessageType.TEXT -> {
                    ITEM_VIEW_TYPE_TEXT_RECEIVED
                }
                ChatMessageType.IMAGE -> {
                    ITEM_VIEW_TYPE_IMAGE_RECEIVED
                }
                ChatMessageType.VIDEO -> {
                    ITEM_VIEW_TYPE_VIDEO_RECEIVED
                }
                ChatMessageType.CHAT_MARKER -> {
                    ITEM_VIEW_TYPE_MARKER
                }
                ChatMessageType.QUESTION -> {
                    ITEM_VIEW_TYPE_QUESTION
                }
                ChatMessageType.TOOL -> {
                    ITEM_VIEW_TYPE_TOOL_RECEIVED
                }
            }
        } else {
            when (ChatMessageType.valueOf(messageType.upperCase())) {
                ChatMessageType.TEXT -> {
                    ITEM_VIEW_TYPE_TEXT_SENT
                }
                ChatMessageType.IMAGE -> {
                    ITEM_VIEW_TYPE_IMAGE_SENT
                }
                ChatMessageType.VIDEO -> {
                    ITEM_VIEW_TYPE_VIDEO_SENT
                }
                ChatMessageType.CHAT_MARKER -> {
                    ITEM_VIEW_TYPE_MARKER
                }
                ChatMessageType.QUESTION -> {
                    ITEM_VIEW_TYPE_QUESTION
                }
                ChatMessageType.TOOL -> {
                    ITEM_VIEW_TYPE_TOOL_SENT
                }
            }
        }
    }

    fun toImageToolboxExercise(): ToolboxExercise {
        return ToolboxExercise(this)
    }

    fun toFullscreenPlayerData(): FullscreenPlayerData {
        return FullscreenPlayerData(this)
    }

    fun asSocketMessage(): ChatAnswer {
        return ChatAnswer(
            customAnswer = this.customAnswer,
            botChoiceId = this.botChoiceId,
            botStepId = this.botStepId,
            metadata = this.metadata,
            timestamp = this.timestamp
        )
    }
}

data class RoomChatMessageWithTool(
    @Embedded val chatMessage: ChatMessage,
    @Relation(
        parentColumn = "tool_id",
        entityColumn = "id"
    )
    val tool: Tool?
) {
    fun toChatMessage(): ChatMessage {
        val chatHistoryItem = this.chatMessage
        chatHistoryItem.tool = this.tool
        chatHistoryItem.tool = this.tool
        return chatHistoryItem
    }
}

enum class ChatMessageType {
    TEXT,
    IMAGE,
    VIDEO,
    CHAT_MARKER,
    QUESTION,
    TOOL
}

enum class ChatMessageSender {
    BOT,
    USER,
    BOT_CHAT_MARKER,
    BOT_QUESTION
}

data class MessageData(
    val markerType: String? = null
)

enum class ChatMarkerType {
    START,
    END
}

data class Question(
    @ColumnInfo(name = "question_type")
    val questionType: String,
    @ColumnInfo(name = "allow_custom_answer")
    val allowCustomAnswer: Boolean,
    val text: String?,
    @ColumnInfo(name = "sub_text")
    val subText: String?,
    val metadata: String?,
    @ColumnInfo(name = "bot_choices")
    val botChoices: List<BotChoice>?
)

enum class ChatQuestionType {
    TEXT,
    MULTIPLE_CHOICE,
    IMAGE,
    SLIDER
}

@Parcelize
data class BotChoice(
    val id: Int = -1,
    val order: Int? = null,
    val label: String? = null,
    val thumbnailUrl: String? = null,
    val fullscreenUrl: String? = null,
    val isDefault: Boolean? = null,
    val value: String? = null,
    val action: ChatAction? = null
) : Parcelable

@Parcelize
data class ChatAction(
    val name: String? = null,
    val data: Int? = null,
    var tool_id: Int? = null
) : Parcelable

enum class ChatActionType {
    HOME,
    PLAN,
    CHAT,
    JOURNEY,
    TOOLBOX,
    JOURNAL,
    ACCOUNT,
    PREFERENCES,
    NOTIFICATIONS,
    CONTACT,
    FEEDBACK,
    CHANGE_PLAN_LIST,
    SESSION,
    EXERCISE,
    QUOTE,
    IMAGE;

    var data: Int? = null
}
