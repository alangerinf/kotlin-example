package com.chatowl.data.entities.notification

import android.os.Bundle
import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class NotificationActionName():Parcelable {
    //usar PLAN_MENU.name.toLowerCase()
    NOTIFICATION,
    PLAN_TOUR,
    TOOLBOX_OTUR,
    JOURNEY_TOUR,
    PLAN_MENU,
    CHAT_MENU,
    TOOLBOX_MENU,
    JOURNEY_MENU,
    HOME,
    PLAN,
    CHAT,
    JOURNEY,
    TOOLBOX,
    ACCONT,
    PREFERENCES,
    NOTIFICATIONS,
    CONTACT,
    FEEDBACK,
    START,
    SESSOIN
}

@Parcelize
enum class NotificationAlertType():Parcelable {

    ERROR,
    MESSAGE,
    NEW_ITEM,
    DUE_ITEM
}

@Parcelize
data class Action(val name:String, val paramId:Int? = null):Parcelable {
}

@Parcelize
data class Alert(val type:String, val title:String, val body:String? = null):Parcelable{

}

@Parcelize
data class ChatOwlNotification(val action:Action, val alert:Alert):Parcelable{

    companion object {
        const val BROADCAST_NOTIFICATION = "BroadcastNotification"
        const val BROADCAST_NOTIFICATION_KEY = "BroadcastNotificationKey"

        const val ACTION_KEY = "action"
        const val ALERT_KEY = "alert"

        fun extractNotification(notificationData: Map<String, String>):ChatOwlNotification?{
            if (notificationData.containsKey(ACTION_KEY) &&
                notificationData.containsKey(ALERT_KEY)) {
                val action =
                    Gson().fromJson<Action>(notificationData.get(ACTION_KEY), Action::class.java)
                val alert =
                    Gson().fromJson<Alert>(notificationData.get(ALERT_KEY), Alert::class.java)
                return ChatOwlNotification(action, alert)
            } else {
                return null
            }
        }

        fun extractNotification(notificationBundle: Bundle?):ChatOwlNotification?{
            if (notificationBundle != null &&
                notificationBundle.containsKey(ACTION_KEY) &&
                notificationBundle.containsKey(ALERT_KEY)) {
                val action = Gson().fromJson<Action>(
                    notificationBundle.getString(ACTION_KEY),
                    Action::class.java
                )
                val alert = Gson().fromJson<Alert>(
                    notificationBundle.getString(ALERT_KEY),
                    Alert::class.java
                )
                notificationBundle.remove(ACTION_KEY)
                notificationBundle.remove(ALERT_KEY)
                return ChatOwlNotification(action, alert)
            } else {
                return null
            }
        }
    }
}

