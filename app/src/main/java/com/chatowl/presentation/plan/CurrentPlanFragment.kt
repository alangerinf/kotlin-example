package com.chatowl.presentation.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.chat.ChatAction
import com.chatowl.data.entities.chat.ChatActionType
import com.chatowl.data.entities.plan.ProgramPreview
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.tabs.TabFragment
import com.chatowl.presentation.tabs.TabViewModel
import com.chatowl.presentation.tourchat.TourChatFragment
import kotlinx.android.synthetic.main.fragment_current_plan.*
import java.util.*

class CurrentPlanFragment :
    ViewModelFragment<CurrentPlanViewModel>(CurrentPlanViewModel::class.java) {

    private lateinit var tabViewModel: TabViewModel
    private val availablePrograms = ArrayList<ProgramPreview>()

    override fun getScreenName() = "Plan list"

    private val weeksAdapter =
        ProgramWeekAdapter(true) { activity -> viewModel.onActivityClicked(activity) }
    private val availableProgramsAdapter =
        ProgramPreviewAdapter { program -> viewModel.onProgramClicked(program) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabFragment = requireParentFragment().requireParentFragment() as TabFragment
        tabViewModel = ViewModelProvider(tabFragment).get(TabViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_plan, container, false)
    }

    override fun setViewModel(): CurrentPlanViewModel {
        val application = ChatOwlApplication.get()
        val homeDao = ChatOwlDatabase.getInstance(application).homeDao
        val activitiesRepository = ChatOwlHomeRepository(homeDao)
        return ViewModelProvider(
            requireActivity(),
            CurrentPlanViewModel.Factory(application, ChatOwlPlanRepository, activitiesRepository)
        ).get(
            CurrentPlanViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        fragment_current_plan_recycler_view.adapter = weeksAdapter
        fragment_current_plan_recycler_view_available_plans.adapter = availableProgramsAdapter

        fragment_current_plan_text_view_read_more.setOnClickListener {
            viewModel.onReadMoreClicked()
        }

        fragment_current_plan_button_change_plan.setOnClickListener {
            viewModel.onChangePlanClicked()
        }

        fragment_current_plan_text_view_version.setOnClickListener {
            viewModel.onPlanVersionClicked()
        }
    }

    private fun openTourChat(flow: Integer) {
        requireActivity().supportFragmentManager.setFragmentResultListener(
            TourChatFragment.TOUR_CHAT_REQUEST_KEY,
            this
        ) { requestKey, bundle ->
            if (requestKey == TourChatFragment.TOUR_CHAT_REQUEST_KEY) {
                val result = bundle.getParcelable<ChatAction>(TourChatFragment.TOUR_CHAT_RESULT_KEY)
                when {
                    result != null -> {
                        handleChatAction(result)
                    }
                }
            }
        }


        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(
            R.id.fragment_tab_layout_main,
            TourChatFragment.newInstance(flow.toInt())
        )
        transaction.addToBackStack(TourChatFragment.TOUR_CHAT_FRAGMENT_KEY)
        transaction.commit()
    }

    private fun handleChatAction(action: ChatAction) {
        when {
            action.name.equals(ChatActionType.SESSION.name, true) -> {
                tabViewModel.navigateToChatWithSessionId(action.tool_id)
            }
            action.name.equals(ChatActionType.CHANGE_PLAN_LIST.name, true) -> {
                viewModel.onChangePlanClicked()
            }
            else -> {
                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.popBackStack(
                    TourChatFragment.TOUR_CHAT_FRAGMENT_KEY,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                ); }
        }

    }

    override fun addObservers() {
        viewModel.startChatFlow.observe(viewLifecycleOwner, { flowId ->
            flowId?.let {
                openTourChat(it)
            }
        })
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.currentProgram.observe(viewLifecycleOwner, { program ->
            program?.let {
                //assessment done
                fragment_current_plan_text_view_name.text = "${it.name}: ${it.tagline}"
                fragment_current_plan_text_view_version.text =
                    it.version.toUpperCase(Locale.getDefault())

                weeksAdapter.submitList(it.weekActivities)

                fragment_current_plan_group_active_plan.visibility = View.VISIBLE
                fragment_current_plan_group_available_plans.visibility = View.GONE
            } ?: run {
                //assessment not done yet
                fragment_current_plan_group_active_plan.visibility = View.GONE
                fragment_current_plan_group_available_plans.visibility = View.VISIBLE

/*                if(PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.PLAN_TOUR_CHAT_OPENED, false) == false) {
                    PreferenceHelper.getChatOwlPreferences().set(PreferenceHelper.Key.PLAN_TOUR_CHAT_OPENED, true)
                    // TODO tell tabViewModel to open floating chat with plan flow id
                }*/
            }
        })
        viewModel.availablePrograms.observe(viewLifecycleOwner, {
            availablePrograms.clear()
            availablePrograms.addAll(it)
            availableProgramsAdapter.submitList(it)
        })
        viewModel.startChatSessionId.observe(viewLifecycleOwner, { sessionId ->
            tabViewModel.navigateToChatWithSessionId(sessionId)
        })
        viewModel.popFromBackStack.observe(viewLifecycleOwner, { remove ->
            if (remove) {
                findNavController().popBackStack(R.id.currentPlanFragment, true)
            }
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.currentProgram.removeObservers(viewLifecycleOwner)
        viewModel.availablePrograms.removeObservers(viewLifecycleOwner)
        viewModel.startChatSessionId.removeObservers(viewLifecycleOwner)
        viewModel.startChatFlow.removeObservers(viewLifecycleOwner)
        viewModel.popFromBackStack.removeObservers(viewLifecycleOwner)
    }

}