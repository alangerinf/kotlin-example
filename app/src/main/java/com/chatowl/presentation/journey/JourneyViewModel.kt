package com.chatowl.presentation.journey

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatowl.data.entities.journey.Journey
import com.chatowl.data.helpers.DateUtils
import com.chatowl.data.helpers.DateUtils.CONSTANT_MILLIS_TO_DAYS
import com.chatowl.data.helpers.DateUtils.formatTo
import com.chatowl.data.repositories.JourneyRepository
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch

import com.github.mikephil.charting.data.Entry
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*

class JourneyViewModel(application: Application, private val repository: JourneyRepository) : BaseViewModel(application) {

    val TAG = JourneyViewModel::class.java.simpleName

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val repository: JourneyRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return JourneyViewModel(application, repository) as T
        }
    }

    var isFetching = false
        private set

    var shouldKeepFetching = true
        private set

    var moods = Journey.JourneyItemMood.values()
    var currentDataWasEmpty = true
    var currentData = emptyMap<String, List<Entry>>().toMutableMap()

    private val currentTimeOption = MutableLiveData(JourneyTimeOption.days)
    val currentTimeOptionLiveData: LiveData<JourneyTimeOption>
        get() = currentTimeOption

    val  currentMoodMood: MutableLiveData<Journey.JourneyItemMood?> = MutableLiveData(null)
    val currentMoodMoodLiveData: LiveData<Journey.JourneyItemMood?>
        get() = currentMoodMood

    private var lastRequestStartDate: Date? = null

    private val MPChartdataSets: MutableLiveData<MutableList<Pair<Journey.JourneyItemMood, List<Entry>>>> = MutableLiveData(arrayListOf())
    val MPChartdataSetsLiveData: LiveData<MutableList<Pair<Journey.JourneyItemMood, List<Entry>>>>
        get() = MPChartdataSets

    @ExperimentalStdlibApi
    fun fetchData() {
        currentDataWasEmpty = currentData.keys.isEmpty()
        var endDate  = Date()
        lastRequestStartDate?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            endDate = calendar.time
        }

        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DATE, -currentTimeOption.value!!.numberOfDaysPerPage())
        val startDate = calendar.time

        if (!isFetching && shouldKeepFetching) else return
        isFetching = true
        viewModelScope.fetch({
            repository.getJourneyResponse(
                currentTimeOption.value!!.toString(),
                startDate.formatTo(DateUtils.COMPLETE_FORMAT),
                endDate.formatTo(DateUtils.COMPLETE_FORMAT))
        }, { response ->
            isFetching = false
            val newDataInfo = emptyMap<String, List<Entry>>().toMutableMap()
            val tempCalendarStart = Calendar.getInstance()
            tempCalendarStart.time = DateUtils.parseFromString(response.firstDate, DateUtils.COMPLETE_FORMAT)!!
            val tempCalendarEnd = Calendar.getInstance()
            tempCalendarEnd.time = endDate
            if(response.journey.isNotEmpty()) {
                lastRequestStartDate = startDate
                if ((tempCalendarStart.timeInMillis/CONSTANT_MILLIS_TO_DAYS).toInt() > (tempCalendarEnd.timeInMillis/CONSTANT_MILLIS_TO_DAYS).toInt() ){
                    shouldKeepFetching = false
                    throw NullPointerException()
                }
            } else {
                shouldKeepFetching = false
                throw NullPointerException()
            }

            response.journey.forEach { item ->
                val dataEntries: List<Entry> = item.values.map {
                    val date = DateUtils.parseFromString(it.date, DateUtils.COMPLETE_FORMAT)
                    val cal = Calendar.getInstance()
                    cal.time = date
                    val xValue = (cal.timeInMillis / (CONSTANT_MILLIS_TO_DAYS)).toFloat()
                    return@map Entry(xValue, it.value, item.moodLabel)
                }
                newDataInfo[item.moodLabel.toString()] = dataEntries
            }

            val entryArray: MutableList<Pair<Journey.JourneyItemMood, List<Entry>>> = arrayListOf()
            var averageEntries: MutableList<Entry> = arrayListOf()

            moods.forEach { mood ->
                var dataEntries: MutableList<Entry> = arrayListOf()

                newDataInfo[mood.toString()]?.let {
                    dataEntries = it.toMutableList()
                }

                currentData[mood.toString()]?.let {
                    dataEntries.addAll(it.toMutableList())
                }

                if (dataEntries.isEmpty()) {
                    return@forEach
                }
                if (mood == Journey.JourneyItemMood.MoodAverage) {
                    averageEntries = dataEntries
                } else {
                    entryArray.add(Pair(mood, dataEntries))
                }
                currentData[mood.toString()] = dataEntries
            }
            if (averageEntries.isNotEmpty()) {
                entryArray.add(0, Pair(Journey.JourneyItemMood.MoodAverage, averageEntries))
            }
            MPChartdataSets.value = entryArray
        }, {
            Log.e(TAG, it.toString())
        })
    }

    private fun setDefaultState() {
        shouldKeepFetching = true
        lastRequestStartDate = null
        currentData = emptyMap<String, List<Entry>>().toMutableMap()
    }

    @ExperimentalStdlibApi
    fun setTimeOption(option: JourneyTimeOption) {
        setDefaultState()
        currentTimeOption.value = option
        fetchData()
    }

}
