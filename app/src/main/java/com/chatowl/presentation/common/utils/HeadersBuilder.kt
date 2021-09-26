package com.chatowl.presentation.common.utils

import android.os.Build
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders


object HeadersBuilder {

    const val AUTHORIZATION_HEADER = "Authorization"
    const val OS_HEADER_LABEL = "chatowl-app-os"
    const val OS_HEADER_VALUE = "android"
    const val DEVICE_MODEL_HEADER_LABEL = "chatowl-app-device"
    const val OS_VERSION_HEADER_VALUE ="chatOwl-app-os-version"


    fun getCommonHeaders(): Headers {
        val headers = HashMap<String, String>()
        val accessToken = PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "") ?: ""
        headers[AUTHORIZATION_HEADER] = "Bearer $accessToken"
        headers[OS_HEADER_LABEL] = OS_HEADER_VALUE
        headers[DEVICE_MODEL_HEADER_LABEL] = getDeviceName()
        headers[OS_VERSION_HEADER_VALUE] = android.os.Build.VERSION.SDK_INT.toString()
        //Analia
        //test158d85dd-3164-4542-8752-6bbe8124d755
        //Graviel - Preview
        //test08341362-af66-42b1-af66-211712bb37fd"
        //Thomas - Staging
        //testf0619ecb-cacb-40c9-8248-48f05cd2d997"
        //Thomas 2 - Staging
        //test46650edc-db2b-4c72-aa51-41673bdceff8"

        return headers.toHeaders()
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.toUpperCase()
        } else manufacturer.toUpperCase() + " " + model.toUpperCase()
    }

}
