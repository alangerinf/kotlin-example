package com.chatowl.presentation.common.utils

import android.content.Context
import android.content.SharedPreferences
import com.chatowl.data.entities.toolbox.offline.DownloadManagerIdRegistry
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi


object PreferenceHelper {

    private const val PREFERENCE_NAME = "com.chatowl.common.utils.PREFERENCE_FILE_KEY"

    fun getChatOwlPreferences(): SharedPreferences =
        ChatOwlApplication.get().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    enum class Key(val key: String) {
        ACCESS_TOKEN("access_token"),
        DOWNLOAD_MANAGER_ID_REGISTRY("download_manager_id_registry"),
        IS_TESTER("is_tester"),
        CHAT_TOOL("chat_tool"),
        CHAT_TOOL_STATUS("chat_tool_status"),
        SECURE_APP_MODE("secure_app_mode"),
        SECURE_PIN("secure_app_pin"),
        // TOUR CHAT FLOW IDs
        PLAN_TOUR_CHAT_OPENED("plan_tour_chat_opened"),
        USER_EMAIL("user_email"),
        IS_FIRST_TIME_OPEN("is_first_time_open")
    }

    fun clearSharedPreferences() {
        val sharedPreferences = getChatOwlPreferences()
        sharedPreferences.edit().clear().apply()
    }
}

/**
 * puts a key value pair in shared prefs if doesn't exists, otherwise updates value on given [key]
 */
fun SharedPreferences.set(key: PreferenceHelper.Key, value: Any?) {
    when (value) {
        is String -> edit { it.putString(key.key, value) }
        is Int -> edit { it.putInt(key.key, value) }
        is Boolean -> edit { it.putBoolean(key.key, value) }
        is Float -> edit { it.putFloat(key.key, value) }
        is Long -> edit { it.putLong(key.key, value) }
        is DownloadManagerIdRegistry -> edit {
            val adapter: JsonAdapter<DownloadManagerIdRegistry> =
                Moshi.Builder().build().adapter(DownloadManagerIdRegistry::class.java)
            val json = adapter.toJson(value)
            it.putString(key.key, json)
        }
        null -> edit { it.remove(key.key) }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

/**
 * finds value on given key.
 * [T] is the type of value
 * @param defaultValue optional default value - will take null for strings, false for bool and -1 for numeric values if [defaultValue] is not specified
 */
inline fun <reified T : Any> SharedPreferences.get(
    key: PreferenceHelper.Key,
    defaultValue: T? = null
): T? {
    return when (T::class) {
        String::class -> getString(key.key, defaultValue as? String) as T?
        Int::class -> getInt(key.key, defaultValue as? Int ?: -1) as T?
        Boolean::class -> getBoolean(key.key, defaultValue as? Boolean ?: false) as T?
        Float::class -> getFloat(key.key, defaultValue as? Float ?: -1f) as T?
        Long::class -> getLong(key.key, defaultValue as? Long ?: -1) as T?
        DownloadManagerIdRegistry::class -> {
            val json = getString(key.key, defaultValue as? String ?: "")
            if (json.isNullOrEmpty()) {
                return null
            }
            val adapter: JsonAdapter<DownloadManagerIdRegistry> = Moshi.Builder().build().adapter(DownloadManagerIdRegistry::class.java)
            return adapter.fromJson(json) as T?
        }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}