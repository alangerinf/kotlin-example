package com.chatowl.data.api.chatowl

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
import com.chatowl.presentation.journal.JournalViewModel.Companion.JOURNAL_PAGE_SIZE
import com.chatowl.presentation.journey.JourneyTimeOption
import com.chatowl.presentation.messages.MessagesViewModel.Companion.MESSAGE_PAGE_SIZE
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface ChatOwlService {

    companion object {
        const val ORDER_ASCENDING = "asc"
        const val ORDER_DESCENDING = "desc"
    }

    @PATCH("clients/me")
    suspend fun updateTimezoneAndShowWelcome(@Body updateTimezoneBody: UpdateTimezoneBody): Response<ChatOwlDataResponse<UpdateTimeZoneResponse>>

    @POST("clients/download-data")
    suspend fun downloadData(): Response<Any>

    @PATCH("clients/settings/show-welcome")
    suspend fun updateShowWelcome(@Body showWelcomeBody: UpdateShowWelcomeBody): Response<Any>

    @GET("clients/app-tour")
    suspend fun getAppTour(): Response<ChatOwlDataResponse<AppTourData>>

    // SETTINGS
    @GET("clients/settings/application-preferences")
    suspend fun getAppPreferences(): Response<ChatOwlDataResponse<AppPreferences>>

    @PATCH("clients/settings/application-preferences")
    suspend fun updateAppPreferences(@Body appPreferences: AppPreferences): Response<ChatOwlDataResponse<Any>>

    @GET("clients/settings/notification-preferences")
    suspend fun getNotificationPreferences(): Response<ChatOwlDataResponse<NotificationPreferences>>

    @GET("clients/settings/email-preferences")
    suspend fun getEmailPreferences(): Response<ChatOwlDataResponse<EmailPreferences>>

    @PATCH("clients/settings/notification-preferences")
    suspend fun updateNotificationPreferences(@Body notificationPreferences: NotificationPreferences): Response<ChatOwlDataResponse<Any>>

    @PATCH("clients/settings/email-preferences")
    suspend fun updateEmailPreferences(@Body emailPreferences: EmailPreferences): Response<ChatOwlDataResponse<EmailPreferences>>

    @DELETE("clients/me")
    suspend fun deleteAccount(): Response<ChatOwlDataResponse<Any>>

    @POST("clients/device")
    @FormUrlEncoded
    suspend fun sendFirebaseToken(@Field("deviceToken") deviceToken: String): Response<ChatOwlDataResponse<Any>>

    @DELETE("clients/device")
    suspend fun clearFirebaseToken(): Response<ChatOwlDataResponse<Any>>

    @POST("clients/contact-us")
    suspend fun sendContactUsMessage(@Body contactUsMessage: ContactUsMessage): Response<Any>

    @POST("clients/feedback")
    suspend fun sendFeedbackMessage(@Body feedbackMessage: FeedbackMessage): Response<Any>

    // HOME
    @GET("v2/home")
    suspend fun getHome(): Response<ChatOwlDataResponse<HomeData>>

    // ACTIVITIES
    @GET("activities/{activityId}/item")
    suspend fun getActivityExercise(@Path("activityId") activityId: Int): Response<ChatOwlDataResponse<ToolboxExercise>>

    // TOOLBOX
    @GET("v2/toolbox")
    suspend fun getToolboxHome(): Response<ChatOwlDataResponse<ToolboxHome>>

    @GET("toolbox/programs/{programId}/tools")
    suspend fun getToolboxProgram(@Path("programId") programId: Int): Response<ChatOwlDataResponse<ToolboxCategoryDetail>>

    @GET("toolbox/mood-meter")
    suspend fun getToolboxMoodMeters(): Response<ChatOwlDataResponse<List<ToolboxMoodMeter>>>

    @PUT("toolbox/mood-meter/{moodId}/{intensity}")
    suspend fun addToolboxMood(@Path("moodId") moodId: String, @Path("intensity") intensity: Int): Response<ChatOwlDataResponse<Any>>

    @GET("items/{toolboxItemId}")
    suspend fun getToolboxExercise(@Path("toolboxItemId") toolboxItemId: Int): Response<ChatOwlDataResponse<ToolboxExercise>>

    // TOOLS
    @PUT("tools/{toolId}/favorite")
    suspend fun addToFavorites(@Path("toolId") toolId: Int): Response<ChatOwlDataResponse<Any>>

    @DELETE("tools/{toolId}/favorite")
    suspend fun removeFromFavorites(@Path("toolId") toolId: Int): Response<ChatOwlDataResponse<Any>>

    @GET("tools/{toolId}/availability")
    suspend fun getToolAvailability(@Path("toolId") toolId: Int): Response<ChatOwlDataResponse<ToolAvailability>>

    @GET("tools/feedback/{toolId}/availability")
    suspend fun getToolFeedBackAvailability(
        @Path("toolId") toolId: Int
    ): Response<ChatOwlDataResponse<GetFeedbackAvailabilityResponse>>

    @POST("items/{toolboxItemId}/events")
    suspend fun postEvent(
        @Path("toolboxItemId") toolboxItemId: Int,
        @Body eventBody: EventBody
    ): Response<ChatOwlDataResponse<Any>>

    //CHAT HISTORY
    @GET("tools/{sessionId}/chat-history")
    suspend fun getChatHistory(@Path("sessionId") sessionId: Int): Response<ChatOwlDataResponse<List<ChatMessage>>>

    @POST("tools/feedback")
    suspend fun postRateExercise(
        @Body postPayload: PostRatePayload
    ): Response<ChatOwlDataResponse<Any>>

    // CHAT TEST
    @GET("testing/flows")
    suspend fun getFlows(): Response<ChatOwlDataResponse<List<Flow>>>

    @GET("testing/property-values")
    suspend fun getValues(): Response<ChatOwlDataResponse<List<PropertyValue>>>

    @POST("testing/property-values")
    suspend fun setValue(@Body updateValueBody: UpdateValueBody): Response<ChatOwlDataResponse<Any>>

    // FILES
    @Multipart
    @POST("media-files/upload")
    suspend fun uploadFile(@Part part: MultipartBody.Part): Response<ChatOwlDataResponse<List<String>>>

    // JOURNAL
    @ExperimentalStdlibApi
    @GET("clients/journals")
    suspend fun getJournals(
        @Query("search") search: String?,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int = JOURNAL_PAGE_SIZE,
        @Query("order") order: String = ORDER_DESCENDING
    ): Response<ChatOwlDataResponse<GetJournalsResponse>>

    @GET("clients/journals/{journalId}")
    suspend fun getJournalById(@Path("journalId") journalId: String): Response<ChatOwlDataResponse<Journal>>

    @POST("clients/journals")
    suspend fun saveJournal(@Body journal: Journal): Response<ChatOwlDataResponse<Journal>>

    @PATCH("clients/journals/{journalId}")
    suspend fun updateJournal(
        @Path("journalId") journalId: String,
        @Body journal: Journal
    ): Response<ChatOwlDataResponse<Journal>>

    @DELETE("clients/journals/{journalId}")
    suspend fun deleteJournal(@Path("journalId") journalId: String): Response<Any>

    // MESSAGES
    @GET("clients/messaging")
    suspend fun getMessages(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int = MESSAGE_PAGE_SIZE,
        @Query("order") order: String = ORDER_DESCENDING
    ): Response<ChatOwlDataResponse<GetMessagesResponse>>

    @POST("clients/messaging")
    suspend fun sendMessage(@Body message: Message): Response<ChatOwlDataResponse<Message>>

    @PATCH("clients/messaging/{messageId}/setMessageAsRead")
    suspend fun setMessageAsRead(@Path("messageId") messageId: String): Response<ChatOwlDataResponse<Any>>

    // PLANS
    @GET("programs")
    suspend fun getPlanHome(): Response<ChatOwlDataResponse<ProgramHome>>

    @GET("programs/{programId}")
    suspend fun getPlanDetail(@Path("programId") programId: Int): Response<ChatOwlDataResponse<ProgramDetail>>

    @PUT("programs/{programId}")
    suspend fun changePlanWeight(@Path("programId") programId: Int): Response<ChatOwlBasicResponse>

    @POST("programs/{programId}")
    suspend fun changePlanFull(@Path("programId") newProgramId: Int): Response<ChatOwlBasicResponse>

    @POST("clients/legal")
    suspend fun sendLegalEmailToMe(): Response<ChatOwlBasicResponse>

    //JOURNEY
    @GET("journey")
    suspend fun getJourney(
        @Query("dateUnit") dateUnit: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ChatOwlDataResponse<GetJourneyResponse>>

    //TRACKING
    @POST("clients/event-tracking")
    suspend fun sendTracking(@Body postEventTrackingRequest: PostEventTrackingRequest): Response<Any>

}