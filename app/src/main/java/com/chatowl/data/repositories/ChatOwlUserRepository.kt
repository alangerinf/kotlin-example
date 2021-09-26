package com.chatowl.data.repositories

import com.chatowl.data.api.chatowl.ChatOwlAPIClient
import com.chatowl.data.entities.apptour.AppTourData
import com.chatowl.data.entities.home.UpdateTimeZoneResponse
import com.chatowl.data.entities.settings.apppreferences.AppPreferences
import com.chatowl.data.entities.settings.apppreferences.FeedbackMessage
import com.chatowl.data.entities.settings.emails.EmailPreferences
import com.chatowl.data.entities.settings.notifications.NotificationPreferences
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.entities.user.UpdateTimezoneBody
import com.chatowl.data.entities.welcome.ContactUsMessage
import com.chatowl.data.entities.welcome.UpdateShowWelcomeBody

object ChatOwlUserRepository {
    private val client = ChatOwlAPIClient

    suspend fun updateTimeZoneAndShowWelcome(timezone: String): UpdateTimeZoneResponse =
        client.updateTimezoneAndShowWelcome(UpdateTimezoneBody(timezone)).data

    suspend fun updateShowWelcome(status: Boolean): Any =
        client.updateShowWelcome(UpdateShowWelcomeBody(showWelcome = status))

    suspend fun getAppTour(): AppTourData =
        client.getAppTour().data

    suspend fun getAppPreferences(): AppPreferences =
        client.getAppPreferences().data

    suspend fun updateAppPreferences(appPreferences: AppPreferences): Any =
        client.updateAppPreferences(appPreferences).data

    suspend fun sendLegalEmailToMe(): Boolean =
        client.sendLegalEmailToMe().success

    suspend fun getNotificationPreferences(): NotificationPreferences =
        client.getNotificationPreferences().data

    suspend fun getEmailPreferences(): EmailPreferences =
        client.getEmailPreferences().data

    suspend fun updateNotificationPreferences(notificationPreferences: NotificationPreferences): Any =
        client.updateNotificationPreferences(notificationPreferences).data

    suspend fun updateEmailPreferences(emailPreferences: EmailPreferences): EmailPreferences =
        client.updateEmailPreferences(emailPreferences).data

    suspend fun deleteAccount(): Boolean =
        client.deleteAccount().success

    suspend fun downloadData(): Any =
        client.downloadData()

    suspend fun sendFirebaseToken(token: String): Boolean =
        client.sendFirebaseToken(token).success

    suspend fun clearFirebaseToken(): Boolean =
        client.clearFirebaseToken().success

    suspend fun sendContactUsMessage(contactUsMessage: ContactUsMessage) =
        client.sendContactUsMessage(contactUsMessage)

    suspend fun sendFeedbackMessage(feedbackMessage: FeedbackMessage) =
        client.sendFeedbackMessage(feedbackMessage)

    suspend fun sendTracking(postEventTrackingRequest: PostEventTrackingRequest) = client.sendTracking(postEventTrackingRequest)


}