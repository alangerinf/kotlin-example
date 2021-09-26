package com.chatowl.presentation.common.utils

import android.content.Context
import android.content.SharedPreferences
import com.chatowl.data.entities.toolbox.offline.DownloadManagerIdRegistry
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi


object SecureAppUtils {

    private const val PREFERENCE_NAME = "com.chatowl.common.utils.PREFERENCE_FILE_KEY"

    private fun getChatOwlPreferences(): SharedPreferences =
        ChatOwlApplication.get().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun setSaveSecureMode(mode: SecureMode, string: String = "") {
        getChatOwlPreferences().set(PreferenceHelper.Key.SECURE_APP_MODE, mode.name)
        getChatOwlPreferences().set(PreferenceHelper.Key.SECURE_PIN, string)
    }

    fun cleanSecureMode() {
        getChatOwlPreferences().set(PreferenceHelper.Key.SECURE_APP_MODE, SecureMode.NONE.name)
        getChatOwlPreferences().set(PreferenceHelper.Key.SECURE_PIN, "")
    }

    fun getCurrentSecureMode(): Pair<SecureMode,String> {
        val mode =  PreferenceHelper.getChatOwlPreferences()
            .get(PreferenceHelper.Key.SECURE_APP_MODE, "")
        val pin = PreferenceHelper.getChatOwlPreferences()
            .get(PreferenceHelper.Key.SECURE_PIN, "")
        return if (mode == null || mode == ""){
            Pair(SecureMode.NONE, "")
        } else {
            Pair(SecureMode.valueOf(mode), pin?:"")
        }
    }


    enum class SecureMode {
        MODE_BIOMETRIC,
        MODE_PIN,
        NONE
    }
}
