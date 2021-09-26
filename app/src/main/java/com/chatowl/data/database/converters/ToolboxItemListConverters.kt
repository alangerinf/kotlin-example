package com.chatowl.data.database.converters

import androidx.room.TypeConverter
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType

class ToolboxItemListConverter {

    private val moshi = Moshi.Builder().build()
    private val listMyData : ParameterizedType = Types.newParameterizedType(List::class.java, ToolboxItem::class.java)
    private val jsonAdapter: JsonAdapter<List<ToolboxItem>> = moshi.adapter(listMyData)

    @TypeConverter
    fun toJson(listMyModel: List<ToolboxItem>?): String? {
        return jsonAdapter.toJson(listMyModel)
    }

    @TypeConverter
    fun fromJson(jsonStr: String?): List<ToolboxItem>? {
        return jsonStr?.let { jsonAdapter.fromJson(jsonStr) }
    }
}