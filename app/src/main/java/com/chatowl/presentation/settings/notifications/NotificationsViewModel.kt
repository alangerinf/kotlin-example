package com.chatowl.presentation.settings.notifications

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chatowl.data.entities.settings.notifications.NotificationPreferences
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch


class NotificationsViewModel(application: Application) : BaseViewModel(application) {

    private val repository = ChatOwlUserRepository

    val notificationPreferences: LiveData<NotificationPreferences> get() = _notificationPreferences
    private val _notificationPreferences = MutableLiveData<NotificationPreferences>()

    init {
        fetchNotificationPreferences()
    }

    private fun fetchNotificationPreferences() {
        _isLoading.value = true
        viewModelScope.fetch({
            repository.getNotificationPreferences()
        }, {
            _notificationPreferences.value = it
            _isLoading.value = false
        }, {
            _isLoading.value = false
        })
    }

    fun updateSessionDueNotifications(value: Boolean) {
        _notificationPreferences.value = notificationPreferences.value?.copy(sessionDue = value)
        updateNotificationPreferences()
    }

    fun updateExerciseDueNotifications(value: Boolean) {
        _notificationPreferences.value = notificationPreferences.value?.copy(exerciseDue = value)
        updateNotificationPreferences()
    }

    fun updateNewExerciseNotifications(value: Boolean) {
        _notificationPreferences.value = notificationPreferences.value?.copy(newExerciseInToolbox = value)
        updateNotificationPreferences()
    }

    fun updateImageOfTheDayNotifications(value: Boolean) {
        _notificationPreferences.value = notificationPreferences.value?.copy(imageOfTheDay = value)
        updateNotificationPreferences()
    }

    fun updateQuoteOfTheDayNotifications(value: Boolean) {
        _notificationPreferences.value = notificationPreferences.value?.copy(quoteOfTheDay = value)
        updateNotificationPreferences()
    }

    fun updateMessageFromTherapistNotifications(value: Boolean) {
        _notificationPreferences.value = notificationPreferences.value?.copy(messageFromTherapist = value)
        updateNotificationPreferences()
    }


    private fun updateNotificationPreferences() {
        notificationPreferences.value?.let { notificationPreferences ->
            _isLoading.value = true
            viewModelScope.fetch({
                repository.updateNotificationPreferences(notificationPreferences)
            }, {
                _isLoading.value = false
            }, {
                _isLoading.value = false
            })
        }
    }
}
