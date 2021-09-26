package com.chatowl.presentation.tabs

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.settings.apppreferences.FeedbackType
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.chatowl.presentation.common.utils.get
import com.chatowl.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import java.util.*

class TabViewModel(application: Application,
                   private val homeRepository: ChatOwlHomeRepository) : BaseViewModel(application) {

    private var navigationParam: Int? = null

    val tabNavigation: LiveData<Int> get() = _tabNavigation
    private val _tabNavigation = SingleLiveEvent<Int>()

    val tourChatFlow: LiveData<Int> get() = _tourChatFlowId
    private val _tourChatFlowId = SingleLiveEvent<Int>()


    val nickname: LiveData<String> get() = _nickname
    private val _nickname = MutableLiveData<String>()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val activitiesRepository: ChatOwlHomeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TabViewModel(application, activitiesRepository) as T
        }
    }

    // TODO this may be moved to be trigger on the Fragment's ON_RESUME
    init {
        loadStoredClientInfo()
        updateTimeZone()
    }

    var wasWelcomeShowedBefore = false
    private fun updateTimeZone() {
        viewModelScope.fetch({
            ChatOwlUserRepository.updateTimeZoneAndShowWelcome(TimeZone.getDefault().id)
        }, {
            //if(!wasWelcomeShowedBefore && it.showWelcome) _navigate.value = TabFragmentDirections.actionGlobalWelcomeFragment(_nickname.value?:"Human")
            if(!wasWelcomeShowedBefore) _navigate.value = TabFragmentDirections.actionGlobalWelcomeFragment(_nickname.value?:"Human")
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


    fun navigateToFeedback() {
        _navigate.value = TabFragmentDirections.actionGlobalFeedbackDialog(FeedbackType.FEEDBACK)
    }

    fun navigateToContactUs() {
        _navigate.value =
            TabFragmentDirections.actionGlobalFeedbackDialog(FeedbackType.`CONTACT-US`)
    }

    fun navigateToAccountSettings() {
        _navigate.value = TabFragmentDirections.actionGlobalMyAccountFragment()
    }

    fun navigateToPreferencesSettings() {
        _navigate.value = TabFragmentDirections.actionGlobalAppPreferencesFragment()
    }

    fun navigateToNotificationsSettings() {
        _navigate.value = TabFragmentDirections.actionGlobalNotificationsFragment()
    }


    //TODO replace the calls made to this function with "handleTabNavigation"
    fun navigateToChatWithSessionId(sessionId: Int? = null) {
        handleTabNavigation(R.id.nav_chat, sessionId)
    }

    fun navigateToToolbox(page: Int? = null) {
        handleTabNavigation(R.id.nav_toolbox, page)
    }

    fun navigateToPlan() {
        handleTabNavigation(R.id.nav_plan, null)
    }

    fun navigateToHome() {
        handleTabNavigation(R.id.nav_home, null)
    }

    fun navigateToChat() {
        handleTabNavigation(R.id.nav_chat, null)
    }

    fun navigateToJourney() {
        handleTabNavigation(R.id.nav_journey, null)
    }


    fun navigateToExercise(data: Int?) {
        //todo, is not ready yet, the backend needs a refactor
        viewModelScope.fetch({}, {}, {})
    }


    fun getNavigationParam(): Int? {
        val id = navigationParam
        navigationParam = null
        return id
    }


    //TODO replace the calls made to this function with "handleTabNavigation"
    fun tourChatRequested(flowId: Int) {
        _tourChatFlowId.value = flowId
    }

    fun handleTabNavigation(navResId: Int, param: Int?) {
        _tabNavigation.value = navResId
        this.navigationParam = param

    }
}
