package com.chatowl.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.extensions.dpToPx
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.tabs.TabFragment
import com.chatowl.presentation.tabs.TabViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.math.min


@ExperimentalStdlibApi
class HomeFragment : ViewModelFragment<HomeViewModel>(HomeViewModel::class.java) {

    private lateinit var tabViewModel: TabViewModel

    override fun getScreenName() = "Home"

    companion object {
        private const val PARALLAX_FACTOR = 2.0f
        private val MAX_TRANSLATION = 200.dpToPx().toFloat()
    }

    private val therapyActivitiesAdapter =
        ActivityAdapter(true) { activity -> viewModel.onActivityClicked(activity) }
    private val suggestedActivitiesAdapter =
        ActivityAdapter(true) { activity -> viewModel.onActivityClicked(activity) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabFragment = requireParentFragment().requireParentFragment() as TabFragment
        tabViewModel = ViewModelProvider(tabFragment).get(TabViewModel::class.java)
    }

    override fun setViewModel(): HomeViewModel {
        val application = ChatOwlApplication.get()
        val homeRepository = ChatOwlHomeRepository(ChatOwlDatabase.getInstance(application).homeDao)
        return ViewModelProvider(this, HomeViewModel.Factory(application, homeRepository)).get(
            HomeViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        fragment_home_tool_bar_container.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        fragment_home_app_bar_image_view_menu.setOnClickListener {

             viewModel.onMenuClicked()
            //TODO this code is to test the tour chat
            //tabViewModel.tourChatRequested(TOUR_CHAT_TEST_FLOW_ID)

            //TODO remove this , is just for testing the floating chat
//            val fragmentManager = activity?.supportFragmentManager
//            val transaction = fragmentManager?.beginTransaction()
//            transaction?.add(R.id.main_nav_host_fragment, TourChatFragment())
//            transaction?.addToBackStack(null)
//            transaction?.commit()

            //TODO remove this, is just for testing the notification behaviour
            //(activity as MainActivity).testDeepLink()

        }

        fragment_home_recycler_view.adapter = therapyActivitiesAdapter
        fragment_home_recycler_view_other_activites.adapter = suggestedActivitiesAdapter

        fragment_home_scroll_view.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val translation = -min((scrollY / PARALLAX_FACTOR), MAX_TRANSLATION)
            fragment_home_image_view_background.translationY = translation
            val alpha = (-translation + (MAX_TRANSLATION / 2)) / (MAX_TRANSLATION * 1.5f)
            //fragment_home_tool_bar_plan_information.alpha = max(0.5f, min(alpha, 1.0f))
        }
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, { navDirection ->
            findNavController().currentDestination?.getAction(navDirection.actionId)?.let {
                findNavController().navigate(navDirection)
            }

        })
        viewModel.isLoadingAndEmpty.observe(viewLifecycleOwner, { isLoadingAndEmpty ->
            fragment_home_progressbar.visibility =
                if (isLoadingAndEmpty) View.VISIBLE else View.GONE
            if (isLoadingAndEmpty) {
                fragment_home_card_view_assessment.visibility = View.GONE
                therapy_group.visibility = View.GONE
                suggested_group.visibility = View.GONE
            }
        })
        viewModel.fullScreenNavigate.observe(viewLifecycleOwner, {
            activity?.findNavController(R.id.main_nav_host_fragment)?.navigate(it)
        })
        viewModel.therapyList.observe(viewLifecycleOwner, {
            therapyActivitiesAdapter.submitList(it)
            therapy_group.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
        })
        viewModel.suggestedList.observe(viewLifecycleOwner, {
            suggestedActivitiesAdapter.submitList(it)
            suggested_group.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
        })
        viewModel.assessmentActivity.observe(viewLifecycleOwner, { assessment ->
            assessment?.let { assessmentActivity ->
                GlideApp.with(fragment_home_image_view_assessment_background)
                    .load(assessmentActivity.tool.thumbnailUrl)
                    .into(fragment_home_image_view_assessment_background)

                fragment_home_card_view_assessment.setOnClickListener {
                    viewModel.onActivityClicked(assessmentActivity)
                }
                fragment_home_card_view_assessment.visibility = View.VISIBLE
                fragment_home_text_view_therapy_plan.visibility = View.GONE
            } ?: run {
                fragment_home_card_view_assessment.visibility = View.GONE
                fragment_home_text_view_therapy_plan.visibility = View.VISIBLE
            }
        })
        viewModel.subscriptionMessage.observe(viewLifecycleOwner, { message ->
            message?.let {
                fragment_home_tool_bar_warning.text = it
                //fragment_home_tool_bar_warning.visibility = View.VISIBLE
            } ?: run {
                fragment_home_tool_bar_warning.visibility = View.GONE
            }
        })
        viewModel.newMessages.observe(viewLifecycleOwner, { newMessages ->
            if (newMessages > 0) {
                fragment_home_tool_bar_messages.text = resources.getQuantityString(
                    R.plurals.format_messages,
                    newMessages,
                    newMessages
                )
                fragment_home_tool_bar_messages.visibility = View.VISIBLE
            } else {
                fragment_home_tool_bar_messages.visibility = View.GONE
            }
        })
//        viewModel.welcomeMessage.observe(viewLifecycleOwner, {
//            fragment_home_text_view_therapy_plan.text = getString(it.first, it.second)
//        })
        viewModel.startChatSessionId.observe(viewLifecycleOwner, { sessionId ->
            tabViewModel.navigateToChatWithSessionId(sessionId)
        })
        viewModel.title.observe(viewLifecycleOwner, { title ->
            fragment_home_text_view_greeting.setText(title)
        })

        viewModel.deepNavigation.observe(viewLifecycleOwner, { stringUri ->
            run {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.mainFragment, true)
                    .build()

                activity?.findNavController(R.id.main_nav_host_fragment)
                    ?.navigate(stringUri.toUri(), navOptions)
            }


        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoadingAndEmpty.removeObservers(viewLifecycleOwner)
        viewModel.fullScreenNavigate.removeObservers(viewLifecycleOwner)
        viewModel.therapyList.removeObservers(viewLifecycleOwner)
        viewModel.suggestedList.removeObservers(viewLifecycleOwner)
        viewModel.assessmentActivity.removeObservers(viewLifecycleOwner)
        viewModel.subscriptionMessage.removeObservers(viewLifecycleOwner)
        viewModel.newMessages.removeObservers(viewLifecycleOwner)
        //viewModel.welcomeMessage.removeObservers(viewLifecycleOwner)
        viewModel.startChatSessionId.removeObservers(viewLifecycleOwner)
        viewModel.title.removeObservers(viewLifecycleOwner)
        viewModel.deepNavigation.removeObservers(viewLifecycleOwner)
    }

}