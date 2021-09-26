package com.chatowl.data.api.interceptors

import android.content.Intent
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.utils.HeadersBuilder
import com.chatowl.presentation.main.MainActivity
import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        // Request customization: add request headers
        val requestBuilder = original
            .newBuilder()
            .headers(HeadersBuilder.getCommonHeaders())
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}