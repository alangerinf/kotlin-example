package com.chatowl.presentation.journey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.data.repositories.JourneyRepository
import com.chatowl.data.database.ChatOwlDatabase
import androidx.lifecycle.ViewModelProvider
import com.chatowl.presentation.common.application.ChatOwlApplication


import com.chatowl.databinding.FragmentJourneyBinding
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.chatowl.databinding.TabJourneyMoodModeBinding
import com.chatowl.databinding.TabJourneyTimeModeBinding
import com.chatowl.data.entities.journey.Journey
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import android.view.MotionEvent
import android.os.Handler
import android.os.Looper
import android.graphics.Color


class JourneyFragment() : ViewModelFragment<JourneyViewModel>(JourneyViewModel::class.java) {

    private lateinit var binding: FragmentJourneyBinding

    val xAxisDefaultLabelCount = 4
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_journey,
            container,
            false
        )
        return binding.root
    }

    override fun getScreenName() = "Journey chart"


    override fun setViewModel(): JourneyViewModel {
        val application = ChatOwlApplication.get()
        val journeyDao = ChatOwlDatabase.getInstance(application).journeyDao
        val journeyRepository = JourneyRepository(journeyDao)
        return ViewModelProvider(
            this,
            JourneyViewModel.Factory(application, journeyRepository)
        ).get(
            JourneyViewModel::class.java
        )
    }

    fun enableEmptyStatus() {
        binding.iViewEmptyStatus.visibility = View.VISIBLE
        binding.tViewNoChart.visibility = View.VISIBLE
        binding.chart.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    fun disableEmptyStatus() {
        binding.iViewEmptyStatus.visibility = View.GONE
        binding.tViewNoChart.visibility = View.GONE
        binding.chart.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    @ExperimentalStdlibApi
    private fun setTimeOptionTabs() {
        binding.tabDays.apply {
            tViewModeName.text = getString(R.string.days)
            root.setOnClickListener {
                viewModel.setTimeOption(JourneyTimeOption.days)
                binding.progressBar.visibility = View.VISIBLE
            }
        }
        binding.tabWeeks.apply {
            tViewModeName.text = getString(R.string.weeks)
            root.setOnClickListener {
                viewModel.setTimeOption(JourneyTimeOption.weeks)
                binding.progressBar.visibility = View.VISIBLE
            }
        }
        binding.tabMonths.apply {
            tViewModeName.text = getString(R.string.months)
            root.setOnClickListener {
                viewModel.setTimeOption(JourneyTimeOption.months)
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun setMoodModeTabs() {
        binding.cardHope.apply {
            tViewModeName.text = getString(R.string.worry)
            root.setOnClickListener {
                if (viewModel.currentMoodMood.value == Journey.JourneyItemMood.Hope) {
                    viewModel.currentMoodMood.value = null
                } else viewModel.currentMoodMood.value = Journey.JourneyItemMood.Hope
            }
        }
        binding.cardAnger.apply {
            tViewModeName.text = getString(R.string.anger)
            root.setOnClickListener {
                if (viewModel.currentMoodMood.value == Journey.JourneyItemMood.Anger) {
                    viewModel.currentMoodMood.value = null
                } else viewModel.currentMoodMood.value = Journey.JourneyItemMood.Anger
            }
        }
        binding.cardLoneliness.apply {
            tViewModeName.text = getString(R.string.depression)
            root.setOnClickListener {
                if (viewModel.currentMoodMood.value == Journey.JourneyItemMood.Loneliness) {
                    viewModel.currentMoodMood.value = null
                } else viewModel.currentMoodMood.value = Journey.JourneyItemMood.Loneliness
            }
        }
        binding.cardNervousness.apply {
            tViewModeName.text = getString(R.string.nervousness)
            root.setOnClickListener {
                if (viewModel.currentMoodMood.value == Journey.JourneyItemMood.Nervousness) {
                    viewModel.currentMoodMood.value = null
                } else viewModel.currentMoodMood.value = Journey.JourneyItemMood.Nervousness
            }
        }
        binding.cardAnxiety.apply {
            tViewModeName.text = getString(R.string.anxiety)
            root.setOnClickListener {
                if (viewModel.currentMoodMood.value == Journey.JourneyItemMood.Anxiety) {
                    viewModel.currentMoodMood.value = null
                } else viewModel.currentMoodMood.value = Journey.JourneyItemMood.Anxiety
            }
        }
    }

    private fun setCurrentTimeOptionMode(timeOption: JourneyTimeOption) {
        when (timeOption) {
            JourneyTimeOption.days -> {
                enableTimeMode(binding.tabDays)
                disableTimeMode(binding.tabWeeks)
                disableTimeMode(binding.tabMonths)
            }
            JourneyTimeOption.weeks -> {
                disableTimeMode(binding.tabDays)
                enableTimeMode(binding.tabWeeks)
                disableTimeMode(binding.tabMonths)
            }
            JourneyTimeOption.months -> {
                disableTimeMode(binding.tabDays)
                disableTimeMode(binding.tabWeeks)
                enableTimeMode(binding.tabMonths)
            }
        }
    }

    private fun enableTimeMode(tabView: TabJourneyTimeModeBinding) {
        tabView.root.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.background_primary_rounded_small)
        tabView.tViewModeName.alpha = 1f
    }

    private fun disableTimeMode(tabView: TabJourneyTimeModeBinding) {
        tabView.root.setBackgroundResource(0)
        tabView.tViewModeName.alpha = 0.8f
    }


    private fun setCurrentMoodMode(mood: Journey.JourneyItemMood?) {
        when (mood) {
            Journey.JourneyItemMood.Hope -> {
                enableMoodMode(binding.cardHope, mood)
                disableMoodMode(binding.cardAnger)
                disableMoodMode(binding.cardLoneliness)
                disableMoodMode(binding.cardNervousness)
                disableMoodMode(binding.cardAnxiety)
            }
            Journey.JourneyItemMood.Anger -> {
                disableMoodMode(binding.cardHope)
                enableMoodMode(binding.cardAnger, mood)
                disableMoodMode(binding.cardLoneliness)
                disableMoodMode(binding.cardNervousness)
                disableMoodMode(binding.cardAnxiety)
            }
            Journey.JourneyItemMood.Loneliness -> {
                disableMoodMode(binding.cardHope)
                disableMoodMode(binding.cardAnger)
                enableMoodMode(binding.cardLoneliness, mood)
                disableMoodMode(binding.cardNervousness)
                disableMoodMode(binding.cardAnxiety)
            }
            Journey.JourneyItemMood.Nervousness -> {
                disableMoodMode(binding.cardHope)
                disableMoodMode(binding.cardAnger)
                disableMoodMode(binding.cardLoneliness)
                enableMoodMode(binding.cardNervousness, mood)
                disableMoodMode(binding.cardAnxiety)
            }
            Journey.JourneyItemMood.Anxiety -> {
                disableMoodMode(binding.cardHope)
                disableMoodMode(binding.cardAnger)
                disableMoodMode(binding.cardLoneliness)
                disableMoodMode(binding.cardNervousness)
                enableMoodMode(binding.cardAnxiety, mood)
            }
            null -> {
                disableMoodMode(binding.cardHope)
                disableMoodMode(binding.cardAnger)
                disableMoodMode(binding.cardLoneliness)
                disableMoodMode(binding.cardNervousness)
                disableMoodMode(binding.cardAnxiety)
            }
        }
    }

    private fun enableMoodMode(tabView: TabJourneyMoodModeBinding, mood: Journey.JourneyItemMood) {
        tabView.tViewModeName.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.windowBackground
            )
        )
        when (mood) {
            Journey.JourneyItemMood.Hope -> {
                tabView.card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_hope
                    )
                )
            }
            Journey.JourneyItemMood.Anger -> {
                tabView.card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_anger
                    )
                )
            }
            Journey.JourneyItemMood.Loneliness -> {
                tabView.card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_loneliness
                    )
                )
            }
            Journey.JourneyItemMood.Nervousness -> {
                tabView.card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_nervousness
                    )
                )
            }
            Journey.JourneyItemMood.Anxiety -> {
                tabView.card.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_anxiety
                    )
                )
            }
        }
    }

    private fun disableMoodMode(tabView: TabJourneyMoodModeBinding) {
        when (tabView) {
            binding.cardHope -> {
                tabView.tViewModeName.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_hope
                    )
                )
            }
            binding.cardAnger -> {
                tabView.tViewModeName.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_anger
                    )
                )
            }
            binding.cardLoneliness -> {
                tabView.tViewModeName.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_loneliness
                    )
                )
            }
            binding.cardNervousness -> {
                tabView.tViewModeName.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_nervousness
                    )
                )
            }
            binding.cardAnxiety -> {
                tabView.tViewModeName.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.emotion_anxiety
                    )
                )
            }
        }
        tabView.card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark))
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp()
        viewModel.setTimeOption(JourneyTimeOption.weeks)
        enableEmptyStatus()
        binding.progressBar.visibility = View.VISIBLE
        viewModel.currentMoodMood.value = null
        setTimeOptionTabs()
        setMoodModeTabs()
    }


    fun setXAxis() {
        binding.chart.xAxis.apply {
            granularity = 1f
            labelCount = xAxisDefaultLabelCount
            setAvoidFirstLastClipping(true)
            setDrawGridLines(true)
            textColor = Color.WHITE
            gridColor = Color.GRAY
            axisLineColor = Color.TRANSPARENT
            position = XAxis.XAxisPosition.TOP
            textSize = 10F
            val formatter = CustomFormatterLive(viewModel.currentTimeOptionLiveData, 0)
            valueFormatter = formatter
        }
    }

    @ExperimentalStdlibApi
    override fun addObservers() {
        binding.toolbar.setTitle("Journey")
        viewModel.MPChartdataSetsLiveData.observe(viewLifecycleOwner, {
            updateDataSets(it)
            if (it.isEmpty()) {
                enableEmptyStatus()
            } else {
                disableEmptyStatus()
            }
        })
        viewModel.currentTimeOptionLiveData.observe(viewLifecycleOwner, {
            setCurrentTimeOptionMode(it)
        })
        viewModel.currentMoodMoodLiveData.observe(viewLifecycleOwner, { mood ->
            setCurrentMoodMode(mood)
            viewModel.currentData?.let { data ->
                data[mood]
            }
            viewModel.MPChartdataSetsLiveData.value?.let { data ->
                updateDataSets(data)
            }
        })

        binding.chart.onChartGestureListener = object : OnChartGestureListener {
            override fun onChartGestureStart(
                me: MotionEvent?,
                lastPerformedGesture: ChartGesture?
            ) {

            }


            override fun onChartGestureEnd(
                me: MotionEvent,
                lastPerformedGesture: ChartGesture
            ) {
                return
            }

            override fun onChartLongPressed(me: MotionEvent) {
                return
            }

            override fun onChartDoubleTapped(me: MotionEvent) {
                return
            }

            override fun onChartSingleTapped(me: MotionEvent) {
                return
            }

            override fun onChartFling(
                me1: MotionEvent,
                me2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ) {
            }

            override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {
                return
            }

            override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
                val numberOfDaysPerPage =
                    viewModel.currentTimeOptionLiveData.value?.numberOfDaysPerPage()
                numberOfDaysPerPage?.let {
                    val threshold = numberOfDaysPerPage / 5.0f
                    val remainingQuantityOfDaysToShow =
                        binding.chart.lowestVisibleX - binding.chart.xAxis.axisMinimum
                    if (remainingQuantityOfDaysToShow < threshold && !viewModel.isFetching) {
                        viewModel.fetchData()
                    }
                }
                return
            }
        }
    }

    override fun removeObservers() {
        viewModel.MPChartdataSetsLiveData.removeObservers(viewLifecycleOwner)
        viewModel.currentTimeOptionLiveData.removeObservers(viewLifecycleOwner)
        viewModel.currentMoodMoodLiveData.removeObservers(viewLifecycleOwner)
    }

    private fun setUp() {
        binding.toolbar.setTitle("Journey")
        setUpChart()
    }

    private fun setUpChart() {
        Handler(Looper.getMainLooper()).post {
            binding.chart.setNoDataTextColor(R.color.white)
            binding.chart.isDoubleTapToZoomEnabled = false
            binding.chart.setPinchZoom(false)
            binding.chart.legend.isEnabled = false
            binding.chart.setScaleEnabled(false)
        }

        Handler(Looper.getMainLooper()).post {
            val marker = CustomMarker(requireContext())
            marker.chartView = binding.chart
            binding.chart.marker = marker
        }

        Handler(Looper.getMainLooper()).post {
            binding.chart.axisLeft.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                granularity = 1f
                axisLineColor = Color.TRANSPARENT
                val horizontalLimitLine = LimitLine(0f)
                horizontalLimitLine.enableDashedLine(6f, 8f, 0f)
                horizontalLimitLine.lineWidth = 0.75f
                horizontalLimitLine.lineColor = Color.WHITE
                addLimitLine(horizontalLimitLine)
                val yPadding = 3.0
                axisMinimum = (-10 - yPadding).toFloat()
                axisMaximum = (10 + yPadding).toFloat()
            }
        }

        Handler(Looper.getMainLooper()).post {
            binding.chart.axisRight.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
                axisLineColor = Color.TRANSPARENT
            }
        }

        Handler(Looper.getMainLooper()).post {
            setXAxis()
        }
    }


    var minQuantity = 0

    private fun updateSelection(chartData: LineData, largestDatasetEntriesQuantity: Int) {

        if (chartData.dataSets.isEmpty()) return

        val lowestVisibleX = binding.chart.lowestVisibleX
        minQuantity = minOf(largestDatasetEntriesQuantity, xAxisDefaultLabelCount)

        Handler(Looper.getMainLooper()).post {
            binding.chart.xAxis.apply {
                //axisMinimum = largestDatasetEntriesQuantity.toFloat()
                //axisMaximum = quantity.toFloat()
                when (viewModel.currentTimeOptionLiveData.value) {
                    JourneyTimeOption.days -> {
                        granularity = 1f
                        labelCount = minQuantity
                    }
                    JourneyTimeOption.weeks -> {
                        setLabelCount(3, true)
                    }
                    JourneyTimeOption.months -> {
                        setLabelCount(4, true)
                    }
                }
            }
            binding.chart.data = chartData
        }

        if (largestDatasetEntriesQuantity < xAxisDefaultLabelCount) {
            updateVisibleXRange(xAxisDefaultLabelCount.toFloat())
        } else {
            updateVisibleXRange()
        }

        val shouldScrollToLastPoint = viewModel.currentDataWasEmpty
        Handler(Looper.getMainLooper()).post {
            if (shouldScrollToLastPoint) {
                binding.chart.moveViewToX(binding.chart.xAxis.axisMaximum)
            } else {
                binding.chart.moveViewToX(lowestVisibleX)
            }
        }

        Handler(Looper.getMainLooper()).post {
            binding.chart.xAxis.apply {
                val formatter = CustomFormatterLive(viewModel.currentTimeOptionLiveData, 0)
                valueFormatter = formatter
            }
            binding.chart.notifyDataSetChanged()
            viewModel.currentDataWasEmpty = false
        }
    }


    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun updateDataSets(entries: List<Pair<Journey.JourneyItemMood, List<Entry>>>) {
        var largestDatasetEntriesQuantity = 0

        val dataSets: MutableList<LineDataSet> = arrayListOf()
        var moodAverageDataSet: LineDataSet? = null

        entries.forEach { (mood, entries) ->
            largestDatasetEntriesQuantity = maxOf(largestDatasetEntriesQuantity, entries.size)

            val dataSet = LineDataSet(entries, mood.name(requireContext()))

            dataSet.apply {
                axisDependency = YAxis.AxisDependency.LEFT
                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                circleHoleRadius = 0f
                setCircleColor(mood.color(requireContext()))
                when {
                    mood == Journey.JourneyItemMood.MoodAverage -> {
                        lineWidth = 10f
                        color = mood.color(requireContext())
                        setDrawCircles(false)
                        isHighlightEnabled = false
                    }
                    viewModel.currentMoodMoodLiveData.value == mood -> {
                        lineWidth = 4f
                        circleRadius = 6f
                        setCircleColor(mood.color(requireContext()))
                        circleHoleColor = mood.color(requireContext())
                        color = mood.color(requireContext())
                        setDrawCircles(true)
                        isHighlightEnabled = true
                    }
                    else -> {
                        lineWidth = 2f
                        circleRadius = 4f
                        val originalColor = mood.color(requireContext())
                        viewModel.currentMoodMoodLiveData.value?.let {
                            val alphaColor = adjustAlpha(originalColor, 0.2f)
                            color = alphaColor
                        } ?: run {
                            color = originalColor
                        }
                        setCircleColor(color)
                        circleHoleColor = color
                        setDrawCircles(viewModel.currentMoodMoodLiveData.value == null)
                        isHighlightEnabled = (viewModel.currentMoodMoodLiveData.value == null)

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
        updateSelection(LineData(dataSets.toList()), largestDatasetEntriesQuantity)
    }


    private fun updateVisibleXRange(forcedValue: Float? = null) {
        Handler(Looper.getMainLooper()).post {
            binding.chart.highlightValue(null)
            forcedValue?.let {
                binding.chart.setVisibleXRange(
                    forcedValue,
                    forcedValue
                )
            } ?: run {
                binding.chart.setVisibleXRange(
                    viewModel.currentTimeOptionLiveData.value!!.visibleXRange(),
                    viewModel.currentTimeOptionLiveData.value!!.visibleXRange()
                )
            }
        }
    }

}