package com.chatowl.presentation.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.chatowl.R
import com.chatowl.data.entities.home.ClientInformation
import com.chatowl.data.entities.home.HomeData
import com.chatowl.data.entities.home.ProgramActivity
import com.chatowl.data.entities.home.ProgramActivity.Companion.TYPE_OVERVIEW
import com.chatowl.data.entities.toolbox.*
import com.chatowl.data.helpers.DateUtils.formatTo
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.*
import com.chatowl.presentation.common.utils.*
import com.chatowl.presentation.tabs.TabFragmentDirections
import com.chatowl.presentation.toolbox.host.ToolboxHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import java.util.*
import kotlin.random.Random


@ExperimentalStdlibApi
class HomeViewModel(
    application: Application,
    private val homeRepository: ChatOwlHomeRepository
) : BaseViewModel(application), LifecycleObserver {

    private val TAG = this::class.java.simpleName

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val activitiesRepository: ChatOwlHomeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(application, activitiesRepository) as T
        }
    }

    val application = getApplication<ChatOwlApplication>()

    val nickname: LiveData<String> get() = _nickname
    private val _nickname = MutableLiveData<String>()

    val fullScreenNavigate: LiveData<NavDirections> get() = _fullScreenNavigate
    private val _fullScreenNavigate = SingleLiveEvent<NavDirections>()

    val title: LiveData<String> get() = _title
    private val _title = MutableLiveData<String>()

    val deepNavigation: LiveData<String> get() = _deepNavigation
    private val _deepNavigation = SingleLiveEvent<String>()

    private val homeData: LiveData<HomeData> get() = _homeData
    private val _homeData = MutableLiveData<HomeData>()

    val startChatSessionId: LiveData<Int> get() = _startChatSessionId
    val _startChatSessionId = SingleLiveEvent<Int>()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        loadStoredClientInfo()
        retrieveFireBaseId()
        fetchHomeData()
        checkForAppTour()
    }

    private fun checkForAppTour() {
        val shouldShowAppTour = PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.IS_FIRST_TIME_OPEN, false)?: false
        when {
            shouldShowAppTour -> {
                PreferenceHelper.getChatOwlPreferences()
                    .set(PreferenceHelper.Key.IS_FIRST_TIME_OPEN, false)
                showApptour()
            }
        }
    }

    private fun showApptour() {
        viewModelScope.fetch({
            ChatOwlUserRepository.getAppTour()
        }, {
            _navigate.value = HomeFragmentDirections.actionHomeToAppTourFragment(it)
        }, {
            Log.e(TAG, "showApptour: "+it.toString())
        })
    }

    var wasWelcomeShowedBefore = false

    private fun updateTimeZoneAndShowWelcome() {
        viewModelScope.fetch({
            ChatOwlUserRepository.updateTimeZoneAndShowWelcome(TimeZone.getDefault().id)
        }, {
            //if(!wasWelcomeShowedBefore && it.showWelcome) _fullScreenNavigate.value = TabFragmentDirections.actionGlobalWelcomeFragment(_nickname.value?:"Human")
            if(!wasWelcomeShowedBefore) _fullScreenNavigate.value = TabFragmentDirections.actionGlobalWelcomeFragment(_nickname.value?:"Human")
            wasWelcomeShowedBefore = true
        }, {
            Log.e("ChatOwlError", it.toString())
        })
    }

    private fun loadStoredClientInfo() {
        viewModelScope.fetch({
            homeRepository.getClientInfoFromDB()
        }, {
            _nickname.value = it.nickname ?: ""
        }, {
        })
    }

    private fun retrieveFireBaseId() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(
                    "ChatOwlApplication",
                    "Fetching FCM registration token failed",
                    task.exception
                )
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "firebase's token; $token"
            Log.d("ChatOwlApplication", msg)

            //Send FirebaseId to Server
            token?.let {
                viewModelScope.fetch({
                    val response = ChatOwlUserRepository.sendFirebaseToken(it)
                    Log.d("ChatOwlApplication", "sendRegistration response:" + response)
                }, {}, {})
            }
        })
    }

    private fun fetchHomeData() {
        _isLoading.value = true
        viewModelScope.fetch({
            homeRepository.getHomeData()
        }, { response ->
            _homeData.value = response
            _title.value = getTitle(response.clientInfo)
            _isLoading.value = false
            updateTimeZoneAndShowWelcome()
        }, {
            Log.e(TAG, it.toString())
            _isLoading.value = false
        })
    }

    private fun getTitle(clientInfo: ClientInformation): String {
        val createdAt = clientInfo.createdAt
        createdAt?.let {
            val createdDate = getDateFromServerDateString(it)
            val daysPast = getDaysPast(createdDate)
            if ((!clientInfo.clientActiveProgramStatus.programStarted) && clientInfo.clientActiveProgramStatus.assessmentDone) {
                return application.resources.getString(
                    R.string.home_greeting_ready,
                    clientInfo.nickname
                )
            } else if (daysPast < 1) {
                return application.resources.getString(
                    R.string.home_greeting_welcome,
                    clientInfo.nickname
                )
            } else {
                return application.resources.getString(
                    getRandomWelcomeResource(),
                    clientInfo.nickname
                )
            }
        }
        return ""
    }

    val isLoadingAndEmpty = isLoading.combineWith(homeData) { isLoading, homeData ->
        isLoading == true &&
                (homeData == null || homeData.isEmpty())
    }

    val assessmentActivity = Transformations.map(homeData) { homeData ->
        homeData.activitiesToday.firstOrNull { it.isAssessment }
    }

    val therapyList = Transformations.map(homeData) { homeData ->
        val mutableList = homeData.activitiesToday.toMutableList()
        mutableList.add(getTherapyPlanOverViewCard())
        homeData.activitiesToday = mutableList
        homeData.activitiesToday.filter { !it.isAssessment }
            .sortedWith(
                compareBy(
                    //   { it.isCompleted },
                    { it.displayOrder })
            )
    }

    val suggestedList = Transformations.map(homeData) { homeData ->
        homeData.activitiesSuggested.sortedWith(
            compareBy(
                //{ it.isCompleted },
                { it.displayOrder }
            )
        )
    }

    val subscriptionMessage = Transformations.map(homeData) { homeData ->
        val subscription = homeData.clientInfo.subscription
        if (subscription.isTrial) {
            val timeLeftInTrial =
                getRemainingTimeInTrial(application, subscription.trialEndsAt ?: "")
            application.getString(
                R.string.format_time_left_in_trial,
                timeLeftInTrial.second
            )
        } else {
            null
        }
    }

    val newMessages = Transformations.map(homeData) { homeData ->
        homeData.clientInfo.newMessages
    }

    fun onMenuClicked() {
        _fullScreenNavigate.value = TabFragmentDirections.actionMainFragmentToSettingsFragment()
    }

    fun checkAvailabilityAndContinue(activity: ProgramActivity) {
        _isLoading.value = true
        viewModelScope.fetch({
            homeRepository.getToolAvailability(activity.tool.id)
        }, { toolAvailability ->
            _isLoading.value = false
            val canPlayStatus = ToolAvailability.getCanPlayStatus(toolAvailability)
            when {
                canPlayStatus == CanPlayStatus.CAN_PLAY -> {
                    _startChatSessionId.value = activity.tool.id
                }
                canPlayStatus == CanPlayStatus.CANNOT_PLAY -> {
                    _navigate.value = HomeFragmentDirections.actionHomeToChatHistoryFragment(
                        activity.tool.id,
                        activity.tool.title
                    )
                }
                canPlayStatus == CanPlayStatus.HAVE_TO_WAIT -> {
                    showHaveToWaitMessage(activity)
                }
            }
        }, {
            Log.e(TAG, it.toString())
            _isLoading.value = false
            if (it.isConnectionException()) {
                _navigate.value = HomeFragmentDirections.actionHomeToNoInternetDialog()
            }
        })
    }

    private fun showHaveToWaitMessage(activity: ProgramActivity) {
        val repeatLimitInSeconds = activity.tool.repeatLimit ?: 0
        val repeatLimitMessage = repeatLimitInSeconds
            .secondsToRepeatLimitDescriptiveString(application)

        val calendar = Calendar.getInstance()
        val now = calendar.time
        val stringDate = now.formatTo(SERVER_FORMAT)

        val timeLeftDescription = getNextAvailableTime(
            application,
            repeatLimitInSeconds, stringDate
        )

        val message = application.getString(
            R.string.limited_exercise_body,
            repeatLimitMessage,
            timeLeftDescription
        )
        _navigate.value = HomeFragmentDirections.actionHomeToExerciseLockedDialog(
            ExerciseLockedMessage(
                application.getString(R.string.limited_exercise_title),
                message,
                activity.tool.title,
                ToolSubtype.valueOf(activity.tool.toolSubtype.upperCase()).drawableId,
                false
            )
        )
    }

    fun onActivityClicked(activity: ProgramActivity) {

        if (isLoading.value == false) {
            if (activity.isLocked) {
                when {
                    !activity.isFree -> {
                        _navigate.value = HomeFragmentDirections.actionHomeToExerciseLockedDialog(
                            ExerciseLockedMessage(
                                application.getString(R.string.locked_exercise_title),
                                application.getString(R.string.paywalled_exercise_body),
                                activity.tool.title,
                                activity.tool.toolIcon(),
                                true
                            )
                        )
                    }
                    activity.pendingTool != null -> {
                        activity.pendingTool?.let { pendingItem ->
                            _navigate.value =
                                HomeFragmentDirections.actionHomeToExerciseLockedDialog(
                                    ExerciseLockedMessage(
                                        application.getString(R.string.locked_exercise_title),
                                        application.getString(R.string.locked_exercise_body),
                                        pendingItem.toolName,
                                        ToolSubtype.valueOf(pendingItem.toolSubtype.upperCase()).drawableId,
                                        false
                                    )
                                )
                        }
                    }
                    activity.daysUntilActivity > 0 -> {
                        val daysText =
                            getRemainingDaysInTrial(activity.daysUntilActivity, application)
                        _navigate.value = HomeFragmentDirections.actionHomeToExerciseLockedDialog(
                            ExerciseLockedMessage(
                                application.getString(R.string.locked_exercise_title),
                                application.getString(
                                    R.string.exercise_coming_up_message,
                                    daysText
                                ),
                                activity.tool.title,
                                activity.tool.toolIcon(),
                                false
                            )
                        )
                    }
                }
            } else {
                when {
                    activity.type == TYPE_OVERVIEW -> {
                        //TODO replace param value to nativate to the proper plan
                        val stringUri = "com.chatowl://tabview?tab=${R.id.nav_plan}&param=3"
                        _deepNavigation.value = stringUri
                    }
                    activity.tool.toolType == ToolType.SESSION.name.lowerCase() -> {
                        checkAvailabilityAndContinue(activity)
                    }
                    activity.tool.toolSubtype.equals(ToolSubtype.JOURNAL.name, true) -> {
                        val stringUri =
                            "com.chatowl://tabview?tab=${R.id.nav_toolbox}&param=${ToolboxHostFragment.PAGE_JOURNAL}"
                        _deepNavigation.value = stringUri
                    }
                    activity.tool.toolSubtype.equals(ToolSubtype.`MOOD-METER`.name, true) -> {
                        val stringUri =
                            "com.chatowl://tabview?tab=${R.id.nav_toolbox}&param=${ToolboxHostFragment.PAGE_MOOD}"
                        _deepNavigation.value = stringUri
                    }
                    else -> {
                        fetchActivityExercise(
                            activity.id,
                            application.getString(R.string.therapy_plan)
                        )
                    }
                }
            }
        }
    }


    private fun fetchActivityExercise(activityId: Int, title: String) {
        _isLoading.value = true
        viewModelScope.fetch({
            homeRepository.getActivityExercise(activityId)
        }, { exercise ->
            navigateToExercise(exercise, title)
            _isLoading.value = false
        }, {
            Log.e(TAG, it.toString())
            _isLoading.value = false
            if (it.isConnectionException()) {
                _navigate.value = HomeFragmentDirections.actionHomeToNoInternetDialog()
            }
        })
    }

    private fun navigateToExercise(exercise: ToolboxExercise, title: String?) {
        when (exercise.tool.toolSubtype) {
            ToolSubtype.VIDEO.name.lowerCase(), ToolSubtype.AUDIO.name.lowerCase() -> {
                _navigate.value = HomeFragmentDirections.actionHomeToMediaExercise(exercise)
                    .setHeader(title)
            }
            ToolSubtype.QUOTE.name.lowerCase() -> {
                _navigate.value = HomeFragmentDirections.actionHomeToQuoteExercise(exercise)
                    .setHeader(title)
            }
            ToolSubtype.IMAGE.name.lowerCase() -> {
                _navigate.value = HomeFragmentDirections.actionHomeToImageExercise(exercise)
                    .setHeader(title)
            }
        }
    }

    fun getRandomWelcomeResource(): Int {
        return when (Random.nextInt(5)) {
            0 -> {
                R.string.home_greeting_random_one
            }
            1 -> {
                R.string.home_greeting_random_two
            }
            2 -> {
                R.string.home_greeting_random_three
            }
            3 -> {
                R.string.home_greeting_random_four
            }
            else -> {
                R.string.home_greeting_random_five
            }
        }

    }

    fun getTherapyPlanOverViewCard(): ProgramActivity {
        val programActivity = ProgramActivity()
        programActivity.pendingTool = null
        programActivity.isFree = true
        programActivity.isLocked = false
        programActivity.daysUntilActivity = 0
        programActivity.tool.title = "See entire therapy plan"
        programActivity.isCompleted = false
        programActivity.displayOrder = Int.MAX_VALUE
        programActivity.type = TYPE_OVERVIEW
        return programActivity
    }
}
