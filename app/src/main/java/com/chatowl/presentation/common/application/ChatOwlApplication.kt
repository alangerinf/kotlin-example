package com.chatowl.presentation.common.application

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.regions.Regions
import com.chatowl.BuildConfig
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.get
import com.chatowl.presentation.main.MainGraphViewModel
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


class ChatOwlApplication : Application(), LifecycleObserver {

    lateinit var userPool: CognitoUserPool

    override fun onCreate() {
        super.onCreate()

        application = this

        userPool = CognitoUserPool(
            this,
            BuildConfig.USER_POOL_ID,
            BuildConfig.CLIENT_ID,
            BuildConfig.CLIENT_SECRET,
            Regions.fromName(BuildConfig.REGION)
        )

        //instabug documentation availbale on https://docs.instabug.com/docs/android-onboarding-experience
        if (BuildConfig.DEBUG) {
            Instabug.Builder(this, BuildConfig.INSTABUG_API_KEY)
                .setInvocationEvents(
                    InstabugInvocationEvent.SHAKE,
                    InstabugInvocationEvent.FLOATING_BUTTON
                )
                .build()
        } else {
            Instabug.Builder(this, BuildConfig.INSTABUG_API_KEY)
                .setInvocationEvents(
                    InstabugInvocationEvent.NONE
                )
                .build()
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun sentOpenAppEvent() {
        Log.d(ChatOwlApplication::class.java.simpleName, "sentOpenAppEvent")
        val accessToken =
            PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "")
                ?: ""
        if (accessToken.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ChatOwlUserRepository.sendTracking(
                        PostEventTrackingRequest.SimpleBuilder(
                            PostEventTrackingRequest.Companion.EventDataName.OpenedApp
                        ).build()
                    )
                } catch (e: Exception) {
                    Log.e(ChatOwlApplication::class.java.simpleName, "sentOpenAppEvent: "+e.toString())
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun sentClosedAppEvent() {
        Log.d(ChatOwlApplication::class.java.simpleName, "sentClosedAppEvent")
        val accessToken =
            PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "")
                ?: ""
        if (accessToken.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ChatOwlUserRepository.sendTracking(
                        PostEventTrackingRequest.SimpleBuilder(
                            PostEventTrackingRequest.Companion.EventDataName.ClosedApp
                        ).build()
                    )
                } catch (e: Exception) {
                    Log.e(ChatOwlApplication::class.java.simpleName, e.toString())
                }
            }
        }
    }

    companion object {
        private lateinit var application: ChatOwlApplication

        fun get(): ChatOwlApplication {
            return application
        }
    }

}
