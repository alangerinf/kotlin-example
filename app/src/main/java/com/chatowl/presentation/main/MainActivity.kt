package com.chatowl.presentation.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.chatowl.R
import com.chatowl.data.entities.notification.ChatOwlNotification
import com.chatowl.data.entities.notification.ChatOwlNotification.Companion.BROADCAST_NOTIFICATION
import com.chatowl.data.entities.notification.ChatOwlNotification.Companion.BROADCAST_NOTIFICATION_KEY
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.activities.BaseActivity
import com.chatowl.presentation.common.extensions.findNavController
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.get
import com.chatowl.presentation.notification.NotificationView
import com.chatowl.presentation.notification.NotificationView.NotificationClickListener
import com.chatowl.presentation.tabs.TabFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar


class MainActivity : BaseActivity() {

    private lateinit var unregistrar: Unregistrar
    private val viewModel: MainViewModel by viewModels()
    private val TAG = this::class.java.simpleName

    private val notificationBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val notification =
                intent?.getParcelableExtra<ChatOwlNotification>(BROADCAST_NOTIFICATION_KEY)
            notification?.let {
                displayNotification(notification)
            }
        }
    }

    fun displayNotification(notification: ChatOwlNotification) {
        val accessToken = PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "") ?: ""
        if (accessToken.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ChatOwlUserRepository.sendTracking(
                        PostEventTrackingRequest.EventNotificationBuilder(
                            PostEventTrackingRequest.Companion.EventDataName.ReceivedPushNotification
                        )
                            .toolId(notification.action.paramId.toString())
                            .notificationType(notification.action.name)
                            .build())
                } catch(e: Exception) {
                    Log.e(TAG, e.toString())
                }
        }

        }

        val view = findViewById<View>(android.R.id.content) as ViewGroup
        NotificationView.inflate(layoutInflater, notification, view, baseContext,
            object : NotificationClickListener {
                override fun onClick(notification: ChatOwlNotification) {
                    excecuteNotificationAction(notification)
                    val accessToken = PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "") ?: ""
                    if (accessToken.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                ChatOwlUserRepository.sendTracking(
                                    PostEventTrackingRequest.EventNotificationBuilder(
                                        PostEventTrackingRequest.Companion.EventDataName.TappedPushNotification
                                    )
                                        .toolId(notification.action.paramId.toString())
                                        .notificationType(notification.action.name)
                                        .build())
                            } catch(e: Exception) {
                                Log.e(TAG, e.toString())
                            }
                        }
                    }

                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        unregistrar = KeyboardVisibilityEvent.registerEventListener(
            this,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    viewModel.keyboardStatusChanged(isOpen)
                }
            })

        viewModel.isNetworkAvailable.observe(this, { isNetworkAvailable ->
            activity_main_text_view_offline_mode.visibility =
                if (isNetworkAvailable) View.GONE else View.VISIBLE
        })

        registerReceiver(notificationBroadcastReceiver, IntentFilter(BROADCAST_NOTIFICATION))

        viewModel.connectivityCheckRequested()

    }

    override fun onResume() {
        super.onResume()
        val bundle = intent.extras

        ChatOwlNotification.extractNotification(bundle)?.let {
            excecuteNotificationAction(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregistrar.unregister()

        unregisterReceiver(notificationBroadcastReceiver)
    }

    private fun excecuteNotificationAction(notification: ChatOwlNotification) {
        Log.d("MainActivity", "notif: $notification")

        //TODO replace this data with the parameters sent within the notificatio instance
        val stringUri = "com.chatowl://tabview?tab=${R.id.nav_toolbox}&param=3"
        findNavController(R.id.main_nav_host_fragment)?.navigate(stringUri.toUri())
    }

    // TODO remove this is just for test the FCMNotification/deepLink behaviour
/*    fun testDeepLink(){
        val stringUri = "com.chatowl://tabview?tabToSelect=${R.id.nav_toolbox}&toolId=3"
        findNavController(R.id.main_nav_host_fragment)?.navigate(stringUri.toUri())
    }*/

    val handler = Handler(Looper.getMainLooper())

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    handler.postDelayed({
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                    },200)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

}