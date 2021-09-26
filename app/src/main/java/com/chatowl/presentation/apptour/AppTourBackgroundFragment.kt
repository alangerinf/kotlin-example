package com.chatowl.presentation.apptour

import android.animation.AnimatorSet
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.apptour.AppTourData
import com.chatowl.data.entities.chat.ChatAction
import com.chatowl.data.entities.plan.Program
import com.chatowl.data.entities.toolbox.ToolboxCategoryType
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroup
import com.chatowl.data.entities.toolbox.home.ToolboxHomeGroupItem
import com.chatowl.presentation.chat.ChatAdapterItem
import com.chatowl.presentation.chat.ChatItemAdapter
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.Animations
import com.chatowl.presentation.journey.CustomFormatter
import com.chatowl.presentation.journey.JourneyTimeOption
import com.chatowl.presentation.plan.ProgramWeekAdapter
import com.chatowl.presentation.toolbox.home.ToolboxGroupAdapter
import com.chatowl.presentation.tourchat.TourChatViewModel
import com.github.mikephil.charting.data.LineData
import kotlinx.android.synthetic.main.fragment_app_tour_background.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_current_plan.*
import kotlinx.android.synthetic.main.fragment_journey.*
import kotlinx.android.synthetic.main.fragment_toolbox_home.*
import kotlinx.android.synthetic.main.row_week_active_plan.view.*


class AppTourBackgroundFragment() :
    ViewModelFragment<AppTourBackgroundViewModel>(AppTourBackgroundViewModel::class.java) {

    private val PLAN_ROW_INDEX = 1
    private val WEEK_INDEX = 3

    lateinit var tourChatViewModel: TourChatViewModel
    lateinit var appTourViewModel: AppTourViewModel
    private var appTourData: AppTourData? = null
    lateinit var layoutChangeListener: View.OnLayoutChangeListener
    lateinit var chatItemAdapter: ChatItemAdapter
    lateinit var pulseAnimatorSet: AnimatorSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appTourData = arguments?.getParcelable(APP_TOUR_DATA_ID_KEY)
    }

    companion object {
        private const val APP_TOUR_DATA_ID_KEY = "AppTourDataId"

        fun newInstance(appTourData: AppTourData) =
            AppTourBackgroundFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(APP_TOUR_DATA_ID_KEY, appTourData)
                }
            }
    }


    override fun onStart() {
        super.onStart()
        Log.d("appTourBackground", "onstart ")
        if (!this::tourChatViewModel.isInitialized) {
            tourChatViewModel =
                ViewModelProvider(requireActivity()).get(TourChatViewModel::class.java)
        }
        try {
            appTourViewModel =
                ViewModelProvider(requireActivity()).get(AppTourViewModel::class.java)
        } catch (e: Exception){
            Log.d("appTourBackground", "onExeption: ${e.message}")
        }

        addTourChatObservers()
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeTourChatObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app_tour_background, container, false)
    }

    fun addTourChatObservers() {
        tourChatViewModel.action.observe(viewLifecycleOwner, { action ->
            handleAction(action)
        })
    }

    override fun addObservers() {
        viewModel.chatAdapterItemList.observe(viewLifecycleOwner, {
            chatItemAdapter = ChatItemAdapter(requireContext()) { item ->
                //nada
            }
            //fragment_chat_recycler_view.layoutManager.setReverseLayout(true);
            fragment_chat_recycler_view.adapter = chatItemAdapter

            val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    fragment_chat_recycler_view?.smoothScrollToPosition(0)
                }
            }
            chatItemAdapter.registerAdapterDataObserver(adapterDataObserver)
            chatItemAdapter.submitList(it.reversed())
        })

        viewModel.chartLineData.observe(viewLifecycleOwner, {
            updateSelection(it, it.dataSetCount)
        })

    }

    private fun handleAction(action: ChatAction) {
        when {
            action.name!!.equals("PLAN_START", true) -> {
                appTourData?.let {
                    planStart(it.plan)
                }
            }
            action.name!!.equals("PLAN_BLUR", true) -> {
                tourChatViewModel.collapseRequested(false)
            }//chat_start
            action.name!!.equals("CHAT_START", true) -> {
                appTourData?.let {
                    chatStart()
                }
            }
            action.name!!.equals("TOOLBOX_START", true) -> {
                appTourData?.let {
                    toolboxStart()
                }
            }
            action.name!!.equals("JOURNEY_START", true) -> {
                    journeyStart()
            }

            action.name!!.equals("CHAT_BLUR", true) -> {
                tourChatViewModel.collapseRequested(false)
            }
            action.name!!.equals("TOOLBOX_BLUR", true) -> {
                tourChatViewModel.collapseRequested(false)
            }
            action.name!!.equals("PLAN_TAB_BLINK", true) -> {
                app_tour_backgroud_bottom_navigation_view.selectedItemId = R.id.nav_plan
                blinkTab(
                    app_tour_backgroud_blink_small_plan,
                    app_tour_backgroud_blink_large_plan
                )
            }
            action.name!!.equals("CHAT_TAB_BLINK", true) -> {
                app_tour_backgroud_bottom_navigation_view.selectedItemId = R.id.nav_chat
                blinkTab(
                    app_tour_backgroud_blink_small_chat,
                    app_tour_backgroud_blink_large_chat
                )
            }
            action.name!!.equals("TOOLBOX_TAB_BLINK", true) -> {
                app_tour_backgroud_bottom_navigation_view.selectedItemId = R.id.nav_toolbox
                blinkTab(
                    app_tour_backgroud_blink_small_toolbox,
                    app_tour_backgroud_blink_large_toolbox
                )
            }
            action.name!!.equals("JOURNEY_TAB_BLINK", true) -> {
                app_tour_backgroud_bottom_navigation_view.selectedItemId = R.id.nav_journey
                blinkTab(
                    app_tour_backgroud_blink_small_journey,
                    app_tour_backgroud_blink_large_journey
                )
            }
            action.name!!.equals("JOURNEY_BLUR", true) -> {
                appTourData?.let {
                    tourChatViewModel.collapseRequested(false)
                }
            }
            action.name!!.equals("PLAN", true) -> {
                tourCompleted()
            }
        }
    }

    private fun tourCompleted(){
        tourChatViewModel.onViewDestroyed()
        appTourViewModel.tourCompleted()
    }

    private fun blinkTab(smallBlinkView: View, largeBlinkView: View) {
        val set = ConstraintSet()

        smallBlinkView.visibility = View.VISIBLE
        largeBlinkView.visibility = View.VISIBLE
        pulseAnimatorSet = Animations.getPulseAnimation(
            smallBlinkView, largeBlinkView
        )

        pulseAnimatorSet.start()

        Handler(Looper.getMainLooper()).postDelayed(
            {
                cancelBlink(smallBlinkView, largeBlinkView, pulseAnimatorSet)
            }, 5000
        )
    }


    private fun cancelBlink(smallBlinkView: View, largeBlinkView: View, animatorSet: AnimatorSet) {
        smallBlinkView.visibility = View.GONE
        largeBlinkView.visibility = View.GONE
        if (animatorSet.isRunning) {
            animatorSet.cancel()
        }
    }

    private fun init() {
        tourChatViewModel.collapseRequested(false)
        app_tour_background_main_container.visibility = View.GONE
        app_tour_background_current_plan.visibility = View.GONE
        app_tour_background_chat.visibility = View.GONE
        app_tour_background_journey.visibility = View.GONE

        setupBottomNavigationBar()

    }

    private fun setupBottomNavigationBar() {
        app_tour_backgroud_bottom_navigation_view.itemIconTintList = null
    }

    private fun planStart(plan: Program) {
        Log.d("appTourBackground", "handleaction planstart")
        app_tour_backgroud_bottom_navigation_view.selectedItemId = R.id.nav_plan

        tourChatViewModel.collapseRequested(true)
        viewModel.planBackgroundRequested()
        app_tour_background_main_container.visibility = View.VISIBLE
        app_tour_background_current_plan.visibility = View.VISIBLE
        app_tour_background_chat.visibility = View.GONE
        app_tour_background_toolbox.visibility = View.GONE
        app_tour_background_journey.visibility = View.GONE

        val weeksAdapter =
            ProgramWeekAdapter(true) { activity -> viewModel.onActivityClicked(activity) }
        fragment_current_plan_recycler_view.adapter = weeksAdapter
        for(programWeek in plan.weekActivities){
            programWeek.isExpanded = false
        }
        weeksAdapter.submitList(plan.weekActivities)

        layoutChangeListener =
            View.OnLayoutChangeListener() { view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int ->

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        val view = fragment_current_plan_recycler_view.get(PLAN_ROW_INDEX)
                        view.performClick()
                        slideToolBoxItems()
                    }, 1500
                )

                fragment_current_plan_recycler_view.removeOnLayoutChangeListener(
                    layoutChangeListener
                )
            }

        fragment_current_plan_recycler_view.addOnLayoutChangeListener(layoutChangeListener)
    }

    private fun chatStart() {
        Log.d("appTourBackground", "handleaction chatStart")
        tourChatViewModel.collapseRequested(true)
        app_tour_background_main_container.visibility = View.VISIBLE
        app_tour_background_current_plan.visibility = View.GONE
        app_tour_background_chat.visibility = View.VISIBLE
        app_tour_background_toolbox.visibility = View.GONE
        app_tour_background_journey.visibility = View.GONE

        chatItemAdapter = ChatItemAdapter(requireContext()) { item ->
            //nada
        }

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = true

        fragment_chat_recycler_view.layoutManager = layoutManager

        appTourData?.let {
            val chatItems = it.chat
            val itemList = mutableListOf<ChatAdapterItem>()

            viewModel.chatBackgroundRequested(chatItems)
        }
    }

    private fun toolboxStart() {
        Log.d("appTourBackground", "handleaction toolboxStart")

        tourChatViewModel.collapseRequested(true)
        app_tour_background_main_container.visibility = View.VISIBLE
        app_tour_background_current_plan.visibility = View.GONE
        app_tour_background_chat.visibility = View.GONE
        app_tour_background_toolbox.visibility = View.VISIBLE
        app_tour_background_journey.visibility = View.GONE

        appTourData?.let {

            val groupAdapter = ToolboxGroupAdapter({ //nada
            }, { group, groupItem ->
                //nada
            })

            fragment_toolbox_recycler_view.adapter = groupAdapter

            val groups = mutableListOf<ToolboxHomeGroup>()

            it.toolbox.toolboxCategories.forEach { category ->
//                allFavoriteExercises.addAll(category.exercises
//                    .filter { it.tool.isFavorite })
//                if (category.exercises.size > ToolboxHomeViewModel.GROUP_MINIMUM) {
                val filteredCategoryExercises = category.exercises
                if (filteredCategoryExercises.isNotEmpty()) {
                    var categoryExercises =
                        mutableListOf<ToolboxHomeGroupItem>(*filteredCategoryExercises.map {
                            ToolboxHomeGroupItem.ToolboxHomeToolboxItem(
                                toolboxItem = it,
                                parentName = category.name
                            )
                        }.toTypedArray())
                    groups.add(
                        ToolboxHomeGroup(
                            id = category.id,
                            title = category.name,
                            groupName = category.name,
                            groupType = ToolboxCategoryType.CATEGORY,
                            items = categoryExercises
                        )
                    )
                }
//                }
            }

            groupAdapter.submitList(groups)

        }
    }

    private fun journeyStart() {
        Log.d("appTourBackground", "handleaction journeystart")

        tourChatViewModel.collapseRequested(true)
        app_tour_background_main_container.visibility = View.VISIBLE
        app_tour_background_current_plan.visibility = View.GONE
        app_tour_background_chat.visibility = View.GONE
        app_tour_background_toolbox.visibility = View.GONE
        app_tour_background_journey.visibility = View.VISIBLE

        progressBar.visibility = View.GONE
        iView_emptyStatus.visibility = View.GONE

        appTourData?.let {
            viewModel.journeyRequested(it.journey.journey)
        }

    }

    private fun updateSelection(chartData: LineData, largestDatasetEntriesQuantity: Int) {

        if (chartData.dataSets.isEmpty()) return

        chart.legend.textColor = resources.getColor(android.R.color.white)
        var minQuantity = minOf(largestDatasetEntriesQuantity, 4)

        Handler(Looper.getMainLooper()).post {
            chart.xAxis.apply {
                when (appTourData!!.journey.dateUnit) {
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
            chart.data = chartData
        }

        updateVisibleXRange()

        Handler(Looper.getMainLooper()).post {
            chart.xAxis.apply {
                val formatter = CustomFormatter(appTourData!!.journey.dateUnit, 0)
                valueFormatter = formatter
            }
            chart.notifyDataSetChanged()
        }
    }

    private fun updateVisibleXRange(forcedValue: Float? = null) {
        Handler(Looper.getMainLooper()).post {
            chart.highlightValue(null)
            forcedValue?.let {
                chart.setVisibleXRange(
                    forcedValue,
                    forcedValue
                )
            } ?: run {
                chart.setVisibleXRange(
                    appTourData!!.journey.dateUnit.visibleXRange(),
                    appTourData!!.journey.dateUnit.visibleXRange()
                )
            }
        }
    }

    fun slideToolBoxItems() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                fragment_current_plan_recycler_view.get(PLAN_ROW_INDEX).row_week_active_plan_recycler_view.smoothScrollToPosition(
                    WEEK_INDEX
                )
            }, 1000
        )
    }

    fun removeTourChatObservers() {
        tourChatViewModel.action.removeObservers(viewLifecycleOwner)
    }

    override fun removeObservers() {
        viewModel.chatAdapterItemList.removeObservers(viewLifecycleOwner)
        //TODO remove them
        viewModel.chartLineData.removeObservers(viewLifecycleOwner)
    }

}