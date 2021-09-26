package com.chatowl.presentation.settings.emails

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chatowl.R
import com.chatowl.data.entities.settings.emails.EmailPreferences
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch
import java.net.UnknownHostException

class EmailsViewModel(application: Application) : BaseViewModel(application) {

    private val repository = ChatOwlUserRepository

    val emailPreferences: LiveData<EmailPreferences> get() = _emailPreferences
    private val _emailPreferences = MutableLiveData<EmailPreferences>()

    val errorMessage: LiveData<String> get() = _errorMessage
    private val _errorMessage = MutableLiveData<String>()

    init {
        fetchNotificationPreferences()
    }

    private fun fetchNotificationPreferences() {
        _isLoading.value = true
        viewModelScope.fetch({
            repository.getEmailPreferences()
        }, {
            _emailPreferences.value = it
            _isLoading.value = false
        }, {
            _isLoading.value = false
            if (it is UnknownHostException) {
                _navigate.value =
                    EmailsFragmentDirections.actionGlobalMessageDialog(R.string.no_internet_connection)
            } else {
                _navigate.value =
                    EmailsFragmentDirections.actionGlobalMessageDialog(R.string.unexpected_email_error)
            }

        })
    }

    fun updateSessionDueEmails(value: Boolean) {
        emailPreferences.value?.let{
            //_emailPreferences.value = emailPreferences.value?.copy(sessionDue = value)
            updateEmailPreferences(emailPreferences.value?.copy(sessionDue = value))
        }
    }

    fun updateExerciseDueEmails(value: Boolean) {
        emailPreferences.value?.let{
            //_emailPreferences.value = emailPreferences.value?.copy(exerciseDue = value)
            updateEmailPreferences(emailPreferences.value?.copy(exerciseDue = value))
        }
    }

    fun updateNewExerciseEmails(value: Boolean) {
        emailPreferences.value?.let{
            //_emailPreferences.value = emailPreferences.value?.copy(newExerciseInToolbox = value)
            updateEmailPreferences(emailPreferences.value?.copy(newExerciseInToolbox = value))
        }
    }

    fun updateImageOfTheDayEmails(value: Boolean) {
        emailPreferences.value?.let{
            //_emailPreferences.value = emailPreferences.value?.copy(imageOfTheDay = value)
            updateEmailPreferences(emailPreferences.value?.copy(imageOfTheDay = value))
        }
    }

    fun updateQuoteOfTheDayEmails(value: Boolean) {//es aca
        emailPreferences.value?.let{
            //_emailPreferences.value = emailPreferences.value?.copy(quoteOfTheDay = value)
            updateEmailPreferences(emailPreferences.value?.copy(quoteOfTheDay = value))
        }
    }

    fun updateMessageFromTherapistEmails(value: Boolean) {
        emailPreferences.value?.let{
            //_emailPreferences.value = emailPreferences.value?.copy(messageFromTherapist = value)
            updateEmailPreferences(emailPreferences.value?.copy(messageFromTherapist = value))
        }
    }

    private fun updateEmailPreferences(newEmailPreferences: EmailPreferences? = null) {
        newEmailPreferences?.let { emailPreferences ->
            _isLoading.value = true
            viewModelScope.fetch({
                repository.updateEmailPreferences(emailPreferences)
            }, {
                _isLoading.value = false
                _emailPreferences.value = it
            }, {
                _isLoading.value = false
                _emailPreferences.value = _emailPreferences.value
                if (it is UnknownHostException){
                    _errorMessage.value =
                    getApplication<ChatOwlApplication>().getString(R.string.no_internet_connection)
                } else {
                    _errorMessage.value =
                        getApplication<ChatOwlApplication>().getString(R.string.unexpected_error)
                }
            })
        }
    }
}
