package com.chatowl.presentation.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.api.chat.TOUR_CHAT_WELCOME_FLOW_ID
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.extensions.setupWithNavController
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.home.HomeViewModel
import com.chatowl.presentation.main.MainViewModel
import com.chatowl.presentation.toolbox.exerciselocked.ExerciseLockedDialogFragmentDirections
import com.chatowl.presentation.tourchat.TourChatAction
import com.chatowl.presentation.tourchat.TourChatFragment
import kotlinx.android.synthetic.main.fragment_tab.*


class TabFragment : ViewModelFragment<TabViewModel>(TabViewModel::class.java) {

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    val args: TabFragmentArgs by navArgs()
    var argsHandled = false

    private var currentNavController: LiveData<NavController>? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigationBar()
        args.tab?.let{
            if (!argsHandled) {
                val tabId = args.tab?.toInt()
                val param = args.param?.toInt()
                viewModel.handleTabNavigation(tabId!!, param)
                argsHandled = true
            }
        }
    }

    override fun setViewModel(): TabViewModel {
        val application = ChatOwlApplication.get()
        val homeRepository = ChatOwlHomeRepository(ChatOwlDatabase.getInstance(application).homeDao)
        return ViewModelProvider(this, TabViewModel.Factory(application, homeRepository)).get(
            TabViewModel::class.java
        )
    }

    private fun setupBottomNavigationBar() {
        tab_bottom_navigation_view.itemIconTintList = null

        fragment_tab_view_bottom_anchor.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(bottom = padding.bottom + windowInsets.systemWindowInsetBottom)
        }

        val navGraphIds = listOf(
            R.navigation.nav_home,
            R.navigation.nav_chat,
            R.navigation.nav_toolbox,
            R.navigation.nav_plan,
            R.navigation.nav_journey
        )

        val onDestinationChangedListener: (Int) -> Unit = { destinationId ->
            when (destinationId) {
                R.id.quoteExercise, R.id.imageExercise, R.id.fullscreenImageFragment -> {
                    hideBottomNavigation()
                }
                else -> {
                    showBottomNavigation()
                }
            }
        }

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = tab_bottom_navigation_view.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = childFragmentManager,
            containerId = R.id.tab_nav_host_container,
            intent = requireActivity().intent,
            onDestinationChangedListener = onDestinationChangedListener
        )

        currentNavController = controller

        tab_bottom_navigation_view.selectedItemId = R.id.nav_home
    }

    private fun hideBottomNavigation() {
        if (tab_bottom_navigation_view.visibility == View.VISIBLE) {
            tab_bottom_navigation_view.visibility = View.GONE
        }
    }

    private fun showBottomNavigation() {
        if (tab_bottom_navigation_view.visibility != View.VISIBLE) {
            tab_bottom_navigation_view.visibility = View.VISIBLE
        }
    }


    override fun addObservers() {

        mainActivityViewModel.openChat_flowId.observe(this, { flowId ->
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(
                R.id.fragment_tab_layout_main,
                TourChatFragment.newInstance(
                    flowId))
            transaction.addToBackStack(TourChatFragment.TOUR_CHAT_FRAGMENT_KEY)
            transaction.commit()
        })

        viewModel.tabNavigation.observe(viewLifecycleOwner, {
            tab_bottom_navigation_view.selectedItemId = it
        })
        mainActivityViewModel.navigate.observe(viewLifecycleOwner, {
            when (it) {
                ExerciseLockedDialogFragmentDirections.actionGlobalCurrentPlanFragment() -> {
                    viewModel.navigateToPlan()
                }
            }
        })
        mainActivityViewModel.isKeyboardOpen.observe(viewLifecycleOwner, { isOpen ->
            tab_bottom_navigation_view.visibility =
                if (isOpen) View.GONE else View.VISIBLE
        })

        viewModel.tourChatFlow.observe(viewLifecycleOwner, {
            openTourChat(it)
        })
    }

    private fun openTourChat(flowId: Int) {
        requireActivity().supportFragmentManager.setFragmentResultListener(TourChatFragment.TOUR_CHAT_REQUEST_KEY, this) { requestKey, bundle ->
            if (requestKey == TourChatFragment.TOUR_CHAT_REQUEST_KEY){
                val result = bundle.getParcelable<TourChatAction>(TourChatFragment.TOUR_CHAT_RESULT_KEY)
                viewModel.navigateToChatWithSessionId(result?.data)
            }
        }
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.fragment_tab_layout_main, TourChatFragment.newInstance(flowId))
        transaction.addToBackStack(TourChatFragment.TOUR_CHAT_FRAGMENT_KEY)
        transaction.commit()
    }

    override fun removeObservers() {
        viewModel.tabNavigation.removeObservers(viewLifecycleOwner)
        mainActivityViewModel.isKeyboardOpen.removeObservers(viewLifecycleOwner)
        viewModel.tourChatFlow.removeObservers(viewLifecycleOwner)
    }
}