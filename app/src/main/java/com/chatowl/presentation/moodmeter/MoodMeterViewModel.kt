package com.chatowl.presentation.moodmeter

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.toolbox.MoodMeterMode
import com.chatowl.data.entities.toolbox.ToolboxMoodMeter
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.fetch


class MoodMeterViewModel(application: Application) : BaseViewModel(application) {

    private val TAG = MoodMeterViewModel::class.java.simpleName
    private val toolboxRepository =
        ToolboxRepository(ChatOwlDatabase.getInstance(application).toolboxDao)

    private val _moodMeters = MutableLiveData<List<ToolboxMoodMeter>>(emptyList())
    val moodMeters: LiveData<List<ToolboxMoodMeter>>
        get() = _moodMeters

    fun getMoodMeters() {
        viewModelScope.fetch({
            toolboxRepository.getMoodMeters()
        }, {
            _moodMeters.value = it.map { toolboxMoodMeter ->
                when(toolboxMoodMeter.moodLabel.toUpperCase()){
                    MoodMeterMode.HOPE.name -> {
                        ToolboxMoodMeter(
                            toolboxMoodMeter.moodId,
                            getApplication<ChatOwlApplication>().getString(R.string.worry),
                            mutableListOf<String>().apply {
                                add(getApplication<ChatOwlApplication>().getString(R.string.emotion_worry_desc_min))
                                add(getApplication<ChatOwlApplication>().getString(R.string.emotion_worry_desc_max))
                            },
                            toolboxMoodMeter.inputs,
                        )
                    }
                    MoodMeterMode.LONELINESS.name -> {
                        ToolboxMoodMeter(
                            toolboxMoodMeter.moodId,
                            getApplication<ChatOwlApplication>().getString(R.string.depression),
                            mutableListOf<String>().apply {
                                add(getApplication<ChatOwlApplication>().getString(R.string.emotion_depression_desc_min))
                                add(getApplication<ChatOwlApplication>().getString(R.string.emotion_depression_desc_max))
                            },
                            toolboxMoodMeter.inputs,
                        )

                    }
                    else -> {
                        toolboxMoodMeter
                    }
                }
            }
        }, {
            Log.e(TAG, it.toString())
        })
    }



}
