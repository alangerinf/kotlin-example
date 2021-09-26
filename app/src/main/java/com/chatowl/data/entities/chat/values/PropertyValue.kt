package com.chatowl.data.entities.chat.values

import android.os.Parcelable
import com.chatowl.R
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PropertyValue(
        @Json(name = "propertyId") val id: Int,
        @Json(name = "propertyName") val name: String? = null,
        @Json(name = "propertyType") val type: String? = null,
        @Json(name = "clientValue") val value: String? = null
) : Parcelable

enum class PropertyValueType(val apiReference: String, val stringResourceId: Int) {
    BOOLEAN("boolean", R.string.chat_value_boolean_description),
    BOOLEAN_ARRAY("boolean[]", R.string.chat_value_boolean_array_description),
    STRING("string", R.string.chat_value_string_description),
    STRING_ARRAY("string[]", R.string.chat_value_string_array_description),
    NUMBER("number", R.string.chat_value_number_description),
    NUMBER_ARRAY("number[]", R.string.chat_value_number_array_description);

    companion object {
        fun getDescriptionString(apiReference: String): Int? {
            return values().find { it.apiReference == apiReference }?.stringResourceId
        }
    }
}