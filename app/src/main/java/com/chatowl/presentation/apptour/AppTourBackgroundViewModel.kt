package com.chatowl.presentation.apptour

import android.app.Application
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.entities.home.ProgramActivity
import com.chatowl.data.entities.journey.Journey
import com.chatowl.data.helpers.DateUtils
import com.chatowl.presentation.chat.ChatAdapterItem
import com.chatowl.presentation.common.BaseViewModel
import com.chatowl.presentation.common.extensions.fetch
import com.chatowl.presentation.common.utils.SingleLiveEvent
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_journey.*
import java.util.*

class AppTourBackgroundViewModel(application: Application) : BaseViewModel(application) {

    val chatAdapterItemList: LiveData<MutableList<ChatAdapterItem>> get() = _chatAdapterItemList
    private val _chatAdapterItemList = SingleLiveEvent<MutableList<ChatAdapterItem>>()


    val chartLineData: LiveData<LineData> get() = _chartLineData
    private val _chartLineData = MutableLiveData<LineData>()

    fun planBackgroundRequested() {

    }

    fun chatBackgroundRequested(chatMessages: List<ChatMessage>) {
        val itemList = mutableListOf<ChatAdapterItem>()
        for (i in 1..10) {

            viewModelScope.fetch({
                Thread.sleep(( i * 1000).toLong())
                val chatAdapterItem =
                    ChatAdapterItem.ChatHistoryAdapterItem(chatMessages.get(i), true)
                //Thread.sleep((i * 500).toLong())
                itemList.add(chatAdapterItem)
                Log.e("appTourBackground", "i: $i, messageAdded: ${chatAdapterItem.chatMessage.text}"
                )
                itemList
            }, {
                _chatAdapterItemList.postValue(it)// = it
            }, {
                Log.e("AppTourBackground", it.toString())
            })
        }
    }

    fun onActivityClicked(activity: ProgramActivity) {
        //TODO nothing this is not interactive
    }

    public fun journeyRequested(journeyList: List<Journey>){
        var newDataInfo = mutableMapOf<Journey.JourneyItemMood, List<Entry>>()//emptyMap<String, List<Entry>>().toMutableMap()
        journeyList.forEach { journey ->
            var dataEntries = journey.values.map {
                val date = DateUtils.parseFromString(it.date, DateUtils.COMPLETE_FORMAT)
                val cal = Calendar.getInstance()
                cal.time = date
                val xValue = (cal.timeInMillis / (DateUtils.CONSTANT_MILLIS_TO_DAYS)).toFloat()
                return@map Entry(xValue, it.value, journey.moodLabel)

            }
            newDataInfo[journey.moodLabel] = dataEntries
        }

        updateDataSets(newDataInfo.toList())
    }

    private fun updateDataSets(entries: List<Pair<Journey.JourneyItemMood, List<Entry>>>) {
        var largestDatasetEntriesQuantity = 0

        val dataSets: MutableList<LineDataSet> = arrayListOf()
        var moodAverageDataSet: LineDataSet? = null

        entries.forEach { (mood, entries) ->
            largestDatasetEntriesQuantity = maxOf(largestDatasetEntriesQuantity, entries.size)

            val dataSet = LineDataSet(entries, mood.name(getApplication()))

            dataSet.apply {
                axisDependency = YAxis.AxisDependency.LEFT
                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                circleHoleRadius = 0f
                setCircleColor(mood.color(getApplication()))
                when {
                    mood == Journey.JourneyItemMood.MoodAverage -> {
                        lineWidth = 10f
                        color = mood.color(getApplication())
                        setDrawCircles(false)
                        isHighlightEnabled = false
                    }
                    Journey.JourneyItemMood.Loneliness == mood -> {
                        lineWidth = 4f
                        circleRadius = 6f
                        setCircleColor(mood.color(getApplication()))
                        circleHoleColor = mood.color(getApplication())
                        color = mood.color(getApplication())
                        setDrawCircles(true)
                        isHighlightEnabled = true
                    }
                    else -> {
                        lineWidth = 2f
                        circleRadius = 4f
                        val originalColor = mood.color(getApplication())
//                        viewModel.currentMoodMoodLiveData.value?.let {
//                            val alphaColor = adjustAlpha(originalColor, 0.2f)
//                            color = alphaColor
//                        }?: run {
                        color = originalColor
//                        }
                        setCircleColor(color)
                        circleHoleColor = color
                        setDrawCircles(true)
                        isHighlightEnabled = (true)
                        setDrawCircles(true)
                        isHighlightEnabled = (true)
                    }
                }
                setDrawValues(false)
                highlightLineWidth = 1f
                highLightColor = Color.GRAY
                setDrawHorizontalHighlightIndicator(false)
            }

            if (mood == Journey.JourneyItemMood.MoodAverage) {
                moodAverageDataSet = dataSet
            } else {
                dataSets.add(dataSet)
            }
        }
        // Selected Mood should be displayed at top and Mood Average always behind
        dataSets.sortBy { it.lineWidth }
        moodAverageDataSet?.let {
            dataSets.add(0, it)
        }

        _chartLineData.value = LineData(dataSets.toList())
        //updateSelection(LineData(dataSets.toList()), largestDatasetEntriesQuantity)
    }



}