package com.chatowl.data.entities.journal

import android.os.Parcelable
import com.chatowl.presentation.journal.entryitem.EntryAdapterItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EntryItem(
    var id: Int = 0,
    var order: Int = 0,
    var type: String,
    var content: String? = null,
    var mediaUrl: String? = null,
    var uri:String? = null
) : Parcelable

fun List<EntryItem>.asAdapterItemList(): List<EntryAdapterItem> {
    return map { journalItem ->
        if(journalItem.type.equals(EntryItemType.TEXT.name, true)) {
            EntryAdapterItem.TextEntryAdapterItem(journalItem)
        } else {
            EntryAdapterItem.MediaEntryAdapterItem(journalItem)
        }
    }
}

enum class EntryItemType {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO
}