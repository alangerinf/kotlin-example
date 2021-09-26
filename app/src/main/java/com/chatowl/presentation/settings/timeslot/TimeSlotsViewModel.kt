package com.chatowl.presentation.settings.timeslot

import android.app.Application
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.BaseViewModel


class TimeSlotsViewModel(application: Application) : BaseViewModel(application) {

    private val repository = ChatOwlUserRepository

    init {

    }

}
