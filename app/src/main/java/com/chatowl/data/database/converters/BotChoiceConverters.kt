package com.chatowl.data.database.converters

import androidx.room.TypeConverter
import com.chatowl.data.entities.chat.BotChoice
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType

class BotChoiceConverter {

    private val moshi = Moshi.Builder().build()
    private val listMyData : ParameterizedType = Types.newParameterizedType(List::class.java, BotChoice::class.java)
    private val jsonAdapter: JsonAdapter<List<BotChoice>> = moshi.adapter(listMyData)

    @TypeConverter
    fun toJson(listMyModel: List<BotChoice>?): String? {
        return jsonAdapter.toJson(listMyModel)
    }

    @TypeConverter
    fun fromJson(jsonStr: String?): List<BotChoice>? {
        return jsonStr?.let { jsonAdapter.fromJson(jsonStr) }
    }
}