package com.chatowl.data.database.converters

import androidx.room.TypeConverter
import com.chatowl.data.entities.journey.Journey
import com.chatowl.data.entities.journey.JourneyValue
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType

class JourneyListConverter {

    private val moshi = Moshi.Builder().build()
    private val listMyData : ParameterizedType = Types.newParameterizedType(List::class.java, Journey::class.java )
    private val jsonAdapter: JsonAdapter<List<Journey>> = moshi.adapter(listMyData)

    @TypeConverter
    fun toJson(listMyModel: List<Journey>?): String? {
        return jsonAdapter.toJson(listMyModel)
    }

    @TypeConverter
    fun fromJson(jsonStr: String?): List<Journey>? {
        return jsonStr?.let { jsonAdapter.fromJson(jsonStr) }
    }
}

class JourneyValuesListConverter {

    private val moshi = Moshi.Builder().build()
    private val listMyData : ParameterizedType = Types.newParameterizedType(List::class.java, JourneyValue::class.java )
    private val jsonAdapter: JsonAdapter<List<JourneyValue>> = moshi.adapter(listMyData)

    @TypeConverter
    fun toJson(listMyModel: List<JourneyValue>?): String? {
        return jsonAdapter.toJson(listMyModel)
    }

    @TypeConverter
    fun fromJson(jsonStr: String?): List<JourneyValue>? {
        return jsonStr?.let { jsonAdapter.fromJson(jsonStr) }
    }
}