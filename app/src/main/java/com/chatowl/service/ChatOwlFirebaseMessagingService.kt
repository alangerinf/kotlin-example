package com.chatowl.service

import android.content.Intent
import android.util.Log
import com.chatowl.data.entities.notification.ChatOwlNotification
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatOwlFirebaseMessagingService  : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MessagingService"
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        Log.d(TAG, "From: ${remoteMessage.from}")
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // For long-running tasks (10 seconds or more) use WorkManager.
            //for more info see the link below
            // https://github.com/firebase/quickstart-android/blob/89e74f707c0ad10e1283938777fd6a7834d167be/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/kotlin/MyFirebaseMessagingService.kt

            sendNotificationBroadcast(ChatOwlNotification.extractNotification(remoteMessage.data))
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See link below (just the service class).
        // https://github.com/firebase/quickstart-android/tree/89e74f707c0ad10e1283938777fd6a7834d167be/messaging
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "sendRegistration Refreshed token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ChatOwlUserRepository.sendFirebaseToken(token)
                Log.d(TAG, "sendRegistration response" + response.toString())
            } catch(e: Exception) {
                //Todo handle new token registration, anyway it will be overrided on next init
            }
        }
    }
    // [END on_new_token]

    private fun sendNotificationBroadcast(notification: ChatOwlNotification?){
        notification?.let {
            val broadcastIntent = Intent(ChatOwlNotification.BROADCAST_NOTIFICATION)
            broadcastIntent.putExtra(ChatOwlNotification.BROADCAST_NOTIFICATION_KEY, notification)
            sendBroadcast(broadcastIntent)
        }
    }
}