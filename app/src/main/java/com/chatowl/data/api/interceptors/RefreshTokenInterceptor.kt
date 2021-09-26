package com.chatowl.data.api.interceptors

import android.content.Intent
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.utils.HeadersBuilder.AUTHORIZATION_HEADER
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.PreferenceHelper.Key
import com.chatowl.presentation.common.utils.set
import com.chatowl.presentation.main.MainActivity
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RefreshTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if(response.code == 401) {
            val application = ChatOwlApplication.get()
            // Get Cognito user pool instance
            val userPool = application.userPool
            // Intent in case refresh token task fails
            val intent = Intent(application, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            // Initialize new request
            var newRequest: Request? = null
            // Initialize countdown latch
            val countDownLatch = CountDownLatch(1)
            // Get new token
            userPool.currentUser.getSessionInBackground(object : AuthenticationHandler {
                override fun onSuccess(
                    userSession: CognitoUserSession?,
                    newDevice: CognitoDevice?
                ) {
                    val newToken = userSession?.accessToken?.jwtToken
                    PreferenceHelper.getChatOwlPreferences().set(Key.ACCESS_TOKEN, newToken)
                    newRequest = request.newBuilder()
                        .header(AUTHORIZATION_HEADER, "Bearer $newToken")
                        .build()
                    countDownLatch.countDown()
                }

                override fun getAuthenticationDetails(
                    authenticationContinuation: AuthenticationContinuation?,
                    userId: String?
                ) {
                    application.startActivity(intent)
                    countDownLatch.countDown()
                }

                override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                    application.startActivity(intent)
                    countDownLatch.countDown()
                }

                override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                    application.startActivity(intent)
                    countDownLatch.countDown()
                }

                override fun onFailure(exception: Exception?) {
                    application.startActivity(intent)
                    countDownLatch.countDown()
                }

            })

            countDownLatch.await(10L, TimeUnit.SECONDS)
            newRequest?.let {
                response.close()
                return chain.proceed(it)
            }
        }
        return response
    }
}