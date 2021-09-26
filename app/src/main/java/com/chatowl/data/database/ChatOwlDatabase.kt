package com.chatowl.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chatowl.data.database.converters.*
import com.chatowl.data.database.dao.*
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.entities.home.ClientInformation
import com.chatowl.data.entities.home.ProgramActivity
import com.chatowl.data.entities.journal.Journal
import com.chatowl.data.entities.journey.GetJourneyResponse
import com.chatowl.data.entities.messages.Message
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.entities.toolbox.home.ToolboxCategory
import com.chatowl.data.entities.toolbox.home.ToolboxProgram
import com.chatowl.data.entities.toolbox.offline.OfflineEvent

@Database(
    entities = [
        ProgramActivity::class,
        ClientInformation::class,
        ToolboxProgram::class,
        ToolboxCategory::class,
        ChatMessage::class,
        ToolboxItem::class,
        Tool::class,
        ToolboxExercise::class,
        OfflineEvent::class,
        Journal::class,
        Message::class,
        GetJourneyResponse::class],
    version = 35,
    exportSchema = false
)
@TypeConverters(
    BotChoiceConverter::class,
    StringListConverter::class,
    ToolboxItemListConverter::class,
    EntryItemListConverter::class,
    JourneyListConverter::class,
    JourneyValuesListConverter::class
)
abstract class ChatOwlDatabase : RoomDatabase() {

    abstract val toolboxDao: ToolboxDao
    abstract val chatDao: ChatDao
    abstract val journalDao: JournalDao
    abstract val messageDao: MessageDao
    abstract val homeDao: HomeDao
    abstract val journeyDao: JourneyDao

    companion object {

        @Volatile
        private var INSTANCE: ChatOwlDatabase? = null

        fun getInstance(context: Context): ChatOwlDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ChatOwlDatabase::class.java,
                        "chatowl_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }
}