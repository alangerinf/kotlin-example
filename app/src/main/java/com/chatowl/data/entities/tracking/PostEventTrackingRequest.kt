package com.chatowl.data.entities.tracking

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PostEventTrackingRequest private constructor(
        val name: String?,
        val type: String?,
        val category: String?,
        val properties: MutableList<EventTrackingProperty> = mutableListOf()
) : Parcelable {

        abstract class Builder(
                var name: String? = null,
                var type: String? = null,
                var category: String? = null,
                var properties: MutableList<EventTrackingProperty> = mutableListOf()) {

                //abstract fun addCustomProperties(name: String, data: String)
                fun build() = PostEventTrackingRequest(name, type, category, properties)
        }

        data class SimpleBuilder(val eventName: EventDataName) : Builder(
                name = eventName.name,
                type = eventName.type,
                category = eventName.category) {
                fun addCustomProperties(name: String, data: String) = apply {
                        if (data.isNotEmpty()) properties.add(EventTrackingProperty(name, data))
                }
        }

        class ViewedScreenBuilder : Builder(
                name = EventDataName.ViewedScreen.name,
                type = EventDataName.ViewedScreen.type,
                category = EventDataName.ViewedScreen.category) {
                fun addCustomProperties(name: String, data: String) {
                        if (data.isNotEmpty()) properties.add(EventTrackingProperty(name, data))
                }
                fun screenName(data: String) = apply { if (data.isNotEmpty()) this.addCustomProperties("ScreenName", data) }
                fun toolboxCategoryName(data: String) = apply { if (data.isNotEmpty()) this.addCustomProperties("ToolboxCategoryName", data) }
                fun therapyPlanName(data: String) = apply { if (data.isNotEmpty()) this.addCustomProperties("TherapyPlanName", data) }
                fun exerciseName(data: String) = apply { if (data.isNotEmpty()) this.addCustomProperties("ExerciseName", data) }
        }

        data class EventNotificationBuilder(val eventDataName: EventDataName) : Builder(
                name = eventDataName.name,
                type = eventDataName.type,
                category = eventDataName.category) {
                fun addCustomProperties(name: String, data: String) = apply{
                        properties.add(EventTrackingProperty(name, data))
                }
                fun notificationType(data: String) = apply { if (data.isNotEmpty()) this.addCustomProperties("NotificationType", data) }
                fun toolId(data: String) = apply { if (data.isNotEmpty()) this.addCustomProperties("ToolId", data)}
        }

        companion object{
                private const val CATEGORY_NAVIGATION = "Navigation"
                private const val CATEGORY_MESSAGING = "Messaging"
                private const val CATEGORY_ENGAGEMENT = "Engagement"
                private const val CATEGORY_SUBSCRIPTION = "Subscription"
                private const val CATEGORY_ONBOARDING = "Onboarding"
                private const val TYPE_TRACKING = "Tracking"

                enum class EventDataName(val type: String, val category: String){
                        OpenedApp(TYPE_TRACKING, CATEGORY_NAVIGATION),
                        ClosedApp(TYPE_TRACKING, CATEGORY_NAVIGATION),
                        ViewedScreen(TYPE_TRACKING, CATEGORY_NAVIGATION),
                        LoggedIn(TYPE_TRACKING, CATEGORY_NAVIGATION),
                        LoggedOut(TYPE_TRACKING, CATEGORY_NAVIGATION),
                        ReadMessage(TYPE_TRACKING, CATEGORY_MESSAGING),
                        ReceivedMessage(TYPE_TRACKING, CATEGORY_MESSAGING),
                        SentMessage(TYPE_TRACKING, CATEGORY_MESSAGING),
                        ReceivedPushNotification(TYPE_TRACKING, CATEGORY_ENGAGEMENT),
                        TappedPushNotification(TYPE_TRACKING, CATEGORY_ENGAGEMENT),
                        ReceivedAppNotification(TYPE_TRACKING, CATEGORY_ENGAGEMENT),
                        TappedAppNotification(TYPE_TRACKING, CATEGORY_ENGAGEMENT),
                        Subscribed(TYPE_TRACKING, CATEGORY_SUBSCRIPTION),
                        Unsubscribed(TYPE_TRACKING, CATEGORY_SUBSCRIPTION),
                        AllowedPushNotifications(TYPE_TRACKING, CATEGORY_ONBOARDING),
                        AbortedAppTour(TYPE_TRACKING, CATEGORY_ONBOARDING),
                        InstalledApp(TYPE_TRACKING, CATEGORY_ONBOARDING),
                        FinishedOnboarding(TYPE_TRACKING, CATEGORY_ONBOARDING)
                }
        }
}