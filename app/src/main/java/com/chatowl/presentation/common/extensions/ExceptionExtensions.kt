package com.chatowl.presentation.common.extensions

import android.accounts.NetworkErrorException
import okhttp3.internal.http2.StreamResetException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

fun Exception.isConnectionException(): Boolean =
    this is java.net.UnknownHostException ||
            this is SocketTimeoutException ||
            this is StreamResetException ||
            this is SSLHandshakeException ||
            this is ConnectException ||
            this is NetworkErrorException