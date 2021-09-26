package com.chatowl.presentation.moodmeter

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch

class MoodMeterPickerViewModel(application: Application) : BaseViewModel(application) {

    val TAG = MoodMeterViewModel::class.java.simpleName
    val mode: LiveData<String> get() = _mode
    private val _mode = MutableLiveData<String>()
    private val toolboxRepository =
        ToolboxRepository(ChatOwlDatabase.getInstance(application).toolboxDao)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MoodMeterPickerViewModel(application) as T
        }
    }


    fun sendMood(moodId: String, intensity: Int) : LiveData<Boolean>{
        val liveData = MutableLiveData<Boolean>()
        viewModelScope.fetch({
            toolboxRepository.sendToolboxMood(moodId, intensity)
        }, {
            liveData.value = it
        }, {
            liveData.value = false
            Log.e(TAG, it.toString())
        })
        return liveData
    }

}