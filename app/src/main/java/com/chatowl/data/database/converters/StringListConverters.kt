package com.chatowl.data.database.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType

class StringListConverter {

    private val moshi = Moshi.Builder().build()
    private val listMyData : ParameterizedType = Types.newParameterizedType(List::class.java, String::class.java)
    private val jsonAdapter: JsonAdapter<List<String>> = moshi.adapter(listMyData)

    @TypeConverter
    fun toJson(listMyModel: List<String>?): String? {
        return jsonAdapter.toJson(listMyModel)
    }

    @TypeConverter
    fun fromJson(jsonStr: String?): List<String>? {
        return jsonStr?.let { jsonAdapter.fromJson(jsonStr) }
    }
}