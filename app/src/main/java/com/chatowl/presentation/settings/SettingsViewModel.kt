package com.chatowl.presentation.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.chatowl.BuildConfig
import com.chatowl.data.entities.settings.apppreferences.FeedbackMessage
import com.chatowl.data.entities.settings.apppreferences.FeedbackType
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import java.net.InetAddress


class SettingsViewModel(application: Application) : BaseViewModel(application) {

    val userRepository = ChatOwlUserRepository
    fun onMyAccountClicked() {
        _navigate.value = SettingsFragmentDirections.actionSettingsToMyAccountFragment()
    }

    fun onAppTourClicked() {
        viewModelScope.fetch({
            userRepository.getAppTour()
        }, {
            _navigate.value = SettingsFragmentDirections.actionSettingsToAppTourFragment(it)

        }, {
           //TODO display error
            Log.e("SettingsViewModel", it?.message.toString())
        })
    }

    fun onAppPreferencesClicked() {
        checkInternetAndContinue(SettingsFragmentDirections.actionSettingsToAppPreferences())
        //_navigate.value = SettingsFragmentDirections.actionSettingsToAppPreferences()
    }

    fun onNotificationPreferencesClicked() {
        checkInternetAndContinue(SettingsFragmentDirections.actionSettingsToNotificationPreferences())
        //_navigate.value = SettingsFragmentDirections.actionSettingsToNotificationPreferences()
    }

    fun onEmailPreferencesClicked() {
        val navDirections = SettingsFragmentDirections.actionSettingsToEmailPreferences()
        checkInternetAndContinue(navDirections)
    }

    fun onContactUsClicked() {
        checkInternetAndContinue(SettingsFragmentDirections.actionGlobalFeedbackDialog(FeedbackType.`CONTACT-US`))
    }

    fun onFeedbackClicked() {
        val navDirections = SettingsFragmentDirections.actionGlobalFeedbackDialog(FeedbackType.FEEDBACK)
        checkInternetAndContinue(navDirections)
    }

    fun onLogoutClicked() {
        _navigate.value = SettingsFragmentDirections.actionSettingsToLogout()
    }

    fun onCrisisSupportClicked() {
        _navigate.value = SettingsFragmentDirections.actionSettingsToCrisisSupport()
    }

    fun onAboutChatOwlClicked() {
        _navigate.value = SettingsFragmentDirections.actionSettingsToAboutUs()
    }

    fun checkInternetAndContinue(navDirections: NavDirections){
        viewModelScope.fetch({
            var address = getURL(BuildConfig.BASE_URL) //BuildConfig.BASE_URL
            val ipAddr: InetAddress = InetAddress.getByName(address)
            //You can replace it with your name
            !ipAddr.equals("")
        }, {
            _navigate.value = navDirections

        }, {
            _navigate.value = SettingsFragmentDirections.actionGlobalNoInternetDialogFragment()
        })
    }

    private fun getURL(fullURL:String):String{
        return fullURL.substring(fullURL.indexOf("://") + 3, fullURL.indexOf(".com") + 4)
    }
}
