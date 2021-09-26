package com.chatowl.presentation.settings.apppreferences

import android.app.Application
import androidx.lifecycle.*
import com.chatowl.R
import com.chatowl.data.entities.settings.apppreferences.AppPreferences
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch


class AppPreferencesViewModel(application: Application) : BaseViewModel(application) {

    private val repository = ChatOwlUserRepository

    val preferences: LiveData<AppPreferences> get() = _preferences
    private val _preferences = MutableLiveData<AppPreferences>()

    init {
        fetchAppPreferences()
    }

    private fun fetchAppPreferences() {
        _isLoading.value = true
        viewModelScope.fetch({
            repository.getAppPreferences()
        }, {
            _preferences.value = it
            _isLoading.value = false
        }, {
            _isLoading.value = false
        })
    }

    fun onToolFeedbackSwitchChanged(checked: Boolean) {
        preferences.value?.let {
            val newPreferences = it.copy(allowToolFeedback = checked)
            _preferences.value = newPreferences
            updateAppPreferences(newPreferences)
        }
    }

    private fun updateAppPreferences(appPreferences: AppPreferences) {
        _isLoading.value = true
        viewModelScope.fetch({
            repository.updateAppPreferences(appPreferences)
        }, {
            _isLoading.value = false
        }, {
            _isLoading.value = false
        })
    }

    fun clearFilesClicked() {
        _navigate.value = AppPreferencesFragmentDirections.actionAppPreferencesToClearFilesDialog()
    }

    fun restorePurchasesClicked() {
        viewModelScope.fetch({

        }, {
            _navigate.value = AppPreferencesFragmentDirections
                .actionGlobalMessageDialog(R.string.purchases_restored_success_message)
                .setShowIcon(true)
        }, {

        })
    }

}
