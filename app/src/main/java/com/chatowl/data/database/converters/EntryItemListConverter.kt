package com.chatowl.data.database.converters

import androidx.room.TypeConverter
import com.chatowl.data.entities.journal.EntryItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType

class EntryItemListConverter {

    private val moshi = Moshi.Builder().build()
    private val listMyData : ParameterizedType = Types.newParameterizedType(List::class.java, EntryItem::class.java)
    private val jsonAdapter: JsonAdapter<List<EntryItem>> = moshi.adapter(listMyData)

    @TypeConverter
    fun toJson(listMyModel: List<EntryItem>?): String? {
        return jsonAdapter.toJson(listMyModel)
    }

    @TypeConverter
    fun fromJson(jsonStr: String?): List<EntryItem>? {
        return jsonStr?.let { jsonAdapter.fromJson(jsonStr) }
    }
}