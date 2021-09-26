package com.chatowl.data.entities.journal

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SaveJournalBody(
        val clientData: SaveJournalClientData
) : Parcelable

@Parcelize
data class SaveJournalClientData (
        val date: String,
        val components: List<EntryItem>
) : Parcelable