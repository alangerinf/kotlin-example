package com.chatowl.data.api.chatowl

import com.chatowl.BuildConfig
import com.chatowl.data.api.RetrofitClient
import com.chatowl.data.entities.apptour.AppTourData
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.entities.chat.flows.Flow
import com.chatowl.data.entities.chat.values.PropertyValue
import com.chatowl.data.entities.chat.values.UpdateValueBody
import com.chatowl.data.entities.home.HomeData
import com.chatowl.data.entities.home.UpdateTimeZoneResponse
import com.chatowl.data.entities.journal.GetJournalsResponse
import com.chatowl.data.entities.journal.Journal
import com.chatowl.data.entities.journey.GetJourneyResponse
import com.chatowl.data.entities.messages.GetMessagesResponse
import com.chatowl.data.entities.messages.Message
import com.chatowl.data.entities.plan.ProgramDetail
import com.chatowl.data.entities.plan.ProgramHome
import com.chatowl.data.entities.settings.apppreferences.AppPreferences
import com.chatowl.data.entities.settings.apppreferences.FeedbackMessage
import com.chatowl.data.entities.settings.emails.EmailPreferences
import com.chatowl.data.entities.settings.notifications.NotificationPreferences
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.entities.toolbox.categorydetail.ToolboxCategoryDetail
import com.chatowl.data.entities.toolbox.event.EventBody
import com.chatowl.data.entities.toolbox.home.ToolboxHome
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.entities.user.UpdateTimezoneBody
import com.chatowl.data.entities.welcome.ContactUsMessage
import com.chatowl.data.entities.welcome.UpdateShowWelcomeBody
import com.chatowl.presentation.common.extensions.obtain
import okhttp3.MultipartBody


object ChatOwlAPIClient {

    private var service = RetrofitClient
        .provideRetrofit(BuildConfig.BASE_URL)
        .create(ChatOwlService::class.java)

    // USER
    suspend fun updateTimezoneAndShowWelcome(updateTimezoneBody: UpdateTimezoneBody): ChatOwlDataResponse<UpdateTimeZoneResponse> = service.updateTimezoneAndShowWelcome(updateTimezoneBody).obtain()

    suspend fun downloadData(): Any = service.downloadData().obtain()

    suspend fun updateShowWelcome(updateShowWelcomeBody: UpdateShowWelcomeBody): Any = service.updateShowWelcome(updateShowWelcomeBody).obtain()

    suspend fun getAppTour(): ChatOwlDataResponse<AppTourData> = service.getAppTour().obtain()

    // SETTINGS
    suspend fun getAppPreferences(): ChatOwlDataResponse<AppPreferences> = service.getAppPreferences().obtain()

    suspend fun sendLegalEmailToMe(): ChatOwlBasicResponse = service.sendLegalEmailToMe().obtain()

    suspend fun updateAppPreferences(appPreferences: AppPreferences): ChatOwlDataResponse<Any> = service.updateAppPreferences(appPreferences).obtain()

    suspend fun getNotificationPreferences(): ChatOwlDataResponse<NotificationPreferences> = service.getNotificationPreferences().obtain()

    suspend fun getEmailPreferences(): ChatOwlDataResponse<EmailPreferences> = service.getEmailPreferences().obtain()

    suspend fun updateNotificationPreferences(notificationPreferences: NotificationPreferences): ChatOwlDataResponse<Any> = service.updateNotificationPreferences(notificationPreferences).obtain()

    suspend fun updateEmailPreferences(emailPreferences: EmailPreferences): ChatOwlDataResponse<EmailPreferences> = service.updateEmailPreferences(emailPreferences).obtain()

    suspend fun deleteAccount(): ChatOwlDataResponse<Any> = service.deleteAccount().obtain()

    suspend fun sendFirebaseToken(token: String): ChatOwlDataResponse<Any> = service.sendFirebaseToken(token).obtain()

    suspend fun clearFirebaseToken(): ChatOwlDataResponse<Any> = service.clearFirebaseToken().obtain()

    suspend fun sendContactUsMessage(contactUsMessage: ContactUsMessage) = service.sendContactUsMessage(contactUsMessage).obtain()

    suspend fun sendFeedbackMessage(feedbackMessage: FeedbackMessage) = service.sendFeedbackMessage(feedbackMessage).obtain()

    // HOME
    suspend fun getHome(): ChatOwlDataResponse<HomeData> = service.getHome().obtain()

    // ACTIVITIES
    suspend fun getActivityExercise(activityId: Int): ChatOwlDataResponse<ToolboxExercise> = service.getActivityExercise(activityId).obtain()

    // TOOLBOX
    suspend fun getToolboxHome(): ChatOwlDataResponse<ToolboxHome> = service.getToolboxHome().obtain()

    suspend fun getToolboxProgram(programId: Int): ChatOwlDataResponse<ToolboxCategoryDetail> = service.getToolboxProgram(programId).obtain()

    suspend fun getToolboxExercise(toolboxItemId: Int): ChatOwlDataResponse<ToolboxExercise> = service.getToolboxExercise(toolboxItemId).obtain()

    suspend fun getToolboxMoodMeters(): ChatOwlDataResponse<List<ToolboxMoodMeter>> = service.getToolboxMoodMeters().obtain()

    suspend fun addToolboxMood(moodId: String, intensity: Int): ChatOwlDataResponse<Any> = service.addToolboxMood(moodId, intensity).obtain()

    // TOOLS
    suspend fun addToFavorites(toolId: Int): ChatOwlDataResponse<Any> = service.addToFavorites(toolId).obtain()


    suspend fun postRateExercise(payload: PostRatePayload): ChatOwlDataResponse<Any> = service.postRateExercise(payload).obtain()

    suspend fun removeFromFavorites(toolId: Int): ChatOwlDataResponse<Any> = service.removeFromFavorites(toolId).obtain()

    suspend fun postEvent(toolboxItemId: Int, eventBody: EventBody): ChatOwlDataResponse<Any> = service.postEvent(toolboxItemId, eventBody).obtain()

    suspend fun getToolAvailability(toolId: Int): ChatOwlDataResponse<ToolAvailability> = service.getToolAvailability(toolId).obtain()

    suspend fun getToolFeedBackAvailability(toolId: Int): ChatOwlDataResponse<GetFeedbackAvailabilityResponse> = service.getToolFeedBackAvailability(toolId).obtain()

    //CHAT HISTORY
    suspend fun getChatHistory(sessionId: Int): ChatOwlDataResponse<List<ChatMessage>> = service.getChatHistory(sessionId).obtain()

    // CHAT TEST
    suspend fun getFlows(): ChatOwlDataResponse<List<Flow>> = service.getFlows().obtain()

    suspend fun getValues(): ChatOwlDataResponse<List<PropertyValue>> = service.getValues().obtain()

    suspend fun setValue(id: Int, value: String): ChatOwlDataResponse<Any> = service.setValue(UpdateValueBody(id, value)).obtain()

    // FILES
    suspend fun uploadFile(part: MultipartBody.Part): ChatOwlDataResponse<List<String>> = service.uploadFile(part).obtain()

    // JOURNAL
    @ExperimentalStdlibApi
    suspend fun getJournals(search: String? = null, pageNumber: Int): ChatOwlDataResponse<GetJournalsResponse> = service.getJournals(search, pageNumber).obtain()

    suspend fun getJournalById(journalId: String) = service.getJournalById(journalId).obtain()

    suspend fun saveJournal(journal: Journal): ChatOwlDataResponse<Journal> = service.saveJournal(journal).obtain()

    suspend fun updateJournal(journal: Journal): ChatOwlDataResponse<Journal> = service.updateJournal(journal.id, journal).obtain()

    suspend fun deleteJournal(journalId: String) = service.deleteJournal(journalId).obtain()

    // MESSAGES
    suspend fun getMessages(pageNumber: Int): ChatOwlDataResponse<GetMessagesResponse> = service.getMessages(pageNumber).obtain()

    suspend fun sendMessage(message: Message): ChatOwlDataResponse<Message> = service.sendMessage(message).obtain()

    suspend fun setMessageAsRead(messageId: String) = service.setMessageAsRead(messageId).obtain()

    // PLANS
    suspend fun getPlanHome(): ChatOwlDataResponse<ProgramHome> = service.getPlanHome().obtain()

    suspend fun getPlanDetail(programId: Int): ChatOwlDataResponse<ProgramDetail> = service.getPlanDetail(programId).obtain()

    suspend fun changePlanWeight(programId: Int): ChatOwlBasicResponse = service.changePlanWeight(programId).obtain()

    suspend fun changePlanFull(newProgramId: Int): ChatOwlBasicResponse = service.changePlanFull(newProgramId).obtain()

    //JOURNEY
    suspend fun getJourney(dateUnit: String, startDate: String, endDate: String): ChatOwlDataResponse<GetJourneyResponse> = service.getJourney(dateUnit, startDate, endDate).obtain()

    //TRACKING
    suspend fun sendTracking(postEventTrackingRequest: PostEventTrackingRequest) = service.sendTracking(postEventTrackingRequest).obtain()

}