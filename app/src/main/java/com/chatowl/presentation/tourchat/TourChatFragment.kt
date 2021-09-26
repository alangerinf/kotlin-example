package com.chatowl.presentation.tourchat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updatePaddingRelative
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.api.chat.APP_TOUR_FLOW_ID
import com.chatowl.data.api.chat.PlayListPlayer
import com.chatowl.data.api.chat.TourChatSocketManager
import com.chatowl.data.api.chat.apptour.AppTourSocketManager
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.apptour.AppTourData
import com.chatowl.data.entities.chat.ChatActionType
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.databinding.FragmentTourChatBinding
import com.chatowl.databinding.FragmentWelcomeBinding
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.main.MainViewModel
import com.chatowl.presentation.tabs.TabFragment
import com.chatowl.presentation.tabs.TabViewModel
import com.chatowl.presentation.toolbox.host.ToolboxHostFragment
import com.chatowl.presentation.welcome.WelcomeFragmentArgs
import kotlinx.android.synthetic.main.fragment_tour_chat.*


class TourChatFragment : ViewModelFragment<TourChatViewModel>(TourChatViewModel::class.java) {

    var flowId: Int = 0

    var appTourData: AppTourData? = null

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    private lateinit var binding: FragmentTourChatBinding

    private var tabViewModel: TabViewModel? = null

    companion object {
        const val TOUR_CHAT_FRAGMENT_KEY = "TourChatFragment"
        const val TOUR_CHAT_REQUEST_KEY = "TourChatRequest"
        const val TOUR_CHAT_RESULT_KEY = "TourChatResultKey"

        private const val TOUR_CHAT_FLOW_ID_KEY = "TourChatFlowId"
        private const val APP_TOUR_DATA_ID_KEY = "AppTourDataId"

        fun newInstance(
            tourChatFlowId: Int
        ) = TourChatFragment().apply {
            arguments = Bundle().apply {
                putInt(TOUR_CHAT_FLOW_ID_KEY, tourChatFlowId)
            }
        }

        fun newInstance(
            tourChatFlowId: Int,
            appTourData: AppTourData
        ) = newInstance(tourChatFlowId).apply {
            this.arguments?.putParcelable(APP_TOUR_DATA_ID_KEY, appTourData)
        }
    }

    private val chatItemAdapter = TourChatItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flowId = arguments?.getInt(TOUR_CHAT_FLOW_ID_KEY, 0) ?: 0
        Log.d("alanalan flow:", "$flowId")
        appTourData = arguments?.getParcelable(APP_TOUR_DATA_ID_KEY)
        val tabFragment = ((requireActivity().supportFragmentManager.fragments.firstOrNull() as? NavHostFragment)?.childFragmentManager?.fragments?.firstOrNull()) as? TabFragment
        tabFragment?.let {
            tabViewModel = ViewModelProvider(it).get(TabViewModel::class.java)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tour_chat, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun setViewModel(): TourChatViewModel {
        val application = ChatOwlApplication.get()
        val chatDao = ChatOwlDatabase.getInstance(application).chatDao
        val userPool = application.userPool
        val socketManager = when (flowId) {
            APP_TOUR_FLOW_ID -> AppTourSocketManager.getInstance(
                appTourData
            )
            else -> TourChatSocketManager.getInstance(
                application,
                chatDao,
                userPool
            )
        }
        return ViewModelProvider(
            requireActivity(),
            TourChatViewModel.Factory(application, socketManager, flowId)
        ).get(TourChatViewModel::class.java)
    }


    val playListPlayer = PlayListPlayer.getInstance()
    var currentMessages: List<ChatMessage> = arrayListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        mainActivityViewModel.isChatMuted.observe(viewLifecycleOwner, { isMuted ->
            playListPlayer.setMute(isMuted)
            binding.iViewVolume.alpha = if (isMuted) 0.4f else 1.0f
        })

        binding.iViewVolume.setOnClickListener {
            mainActivityViewModel.setChatMuted(!mainActivityViewModel.isChatMuted.value!!)
        }

        fragment_tour_chat_view_top_anchor.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        fragment_tour_chat_recycler_view.layoutManager = layoutManager
        fragment_tour_chat_recycler_view.addOnItemTouchListener(object :
            RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }
        })

        chatItemAdapter.isAppTour = appTourData !=  null
        chatItemAdapter.registerAdapterDataObserver(adapterDataObserver)
        fragment_tour_chat_recycler_view.adapter = chatItemAdapter

        fragment_tour_chat_image_button_close.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(
                TOUR_CHAT_FRAGMENT_KEY,
                POP_BACK_STACK_INCLUSIVE
            )
            //TODO erase this, is just for testing purposes
//            val actionString = "{ \"name\":\"session\", \"data\":100001}"
//            val adapter: JsonAdapter<TourChatAction> =
//                Moshi.Builder().build().adapter(TourChatAction::class.java)
//            val tourChatAction = adapter.fromJson(actionString)
//            parentFragmentManager.popBackStack()
//            setFragmentResult(
//                TOUR_CHAT_REQUEST_KEY,
//                bundleOf(TOUR_CHAT_RESULT_KEY to tourChatAction)
//            )
            //TODO erase this, is just for testing purposes
//            setFragmentResult(
//                TOUR_CHAT_REQUEST_KEY,
//                bundleOf(TOUR_CHAT_RESULT_KEY to "fin")
//            )
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            fragment_tour_chat_recycler_view.scrollToPosition(0)
        }
    }


    @ExperimentalStdlibApi
    override fun addObservers() {
        viewModel.collapse.observe(viewLifecycleOwner, {
            when {
                it -> {
                    collapse()
                }
                else -> {
                    expand()
                }
            }
        })



        viewModel.messageList.observe(viewLifecycleOwner, { messages ->
            if (messages.isNotEmpty()) {
                chatItemAdapter.submitList(messages.reversed())
                val difference = messages.minus(currentMessages)
                val playListToAdd = difference.mapNotNull { it.textToSpeechUrl }
                playListPlayer.addItems(playListToAdd)
                currentMessages = messages
            } else {
                Log.d("alan alan","is  message empty")
                //TODO review this, we have a "endChat" live data to endit, this intercepted end
                //generates a missbehaivour
                //parentFragmentManager.popBackStack()
            }
        })
        viewModel.choices.observe(viewLifecycleOwner, { choices ->
            choices?.let {
                it.first?.let { firstChoice ->
                    fragment_tour_chat_choice_1.text = firstChoice.label
                    fragment_tour_chat_choice_1.setOnClickListener {
                        viewModel.onChoiceClicked(firstChoice)
                    }
                    fragment_tour_chat_choice_1.visibility = View.VISIBLE
                } ?: run {
                    fragment_tour_chat_choice_1.visibility = View.GONE
                    fragment_tour_chat_choice_1.setOnClickListener(null)
                }
                it.second?.let { secondChoice ->
                    fragment_tour_chat_choice_2.text = secondChoice.label
                    fragment_tour_chat_choice_2.setOnClickListener {
                        viewModel.onChoiceClicked(secondChoice)
                    }
                    fragment_tour_chat_choice_2.visibility = View.VISIBLE
                } ?: run {
                    fragment_tour_chat_choice_2.visibility = View.GONE
                    fragment_tour_chat_choice_2.setOnClickListener(null)
                }
            } ?: run {
                fragment_tour_chat_choice_1.visibility = View.GONE
                fragment_tour_chat_choice_2.visibility = View.GONE
            }
        })
        viewModel.endChat.observe(viewLifecycleOwner, { endChat ->
            if (endChat) {
                parentFragmentManager.popBackStack()
            }
        })

        viewModel.navigateThroughTabView.observe(viewLifecycleOwner, { chatActionType ->
            when(chatActionType) {
                ChatActionType.HOME -> tabViewModel?.navigateToHome()
                ChatActionType.PLAN -> tabViewModel?.navigateToPlan()
                ChatActionType.CHAT -> tabViewModel?.navigateToChat()
                ChatActionType.JOURNEY -> tabViewModel?.navigateToJourney()
                ChatActionType.TOOLBOX -> tabViewModel?.navigateToToolbox(ToolboxHostFragment.PAGE_HOME)
                ChatActionType.JOURNAL -> tabViewModel?.navigateToToolbox(ToolboxHostFragment.PAGE_JOURNAL)
                ChatActionType.ACCOUNT -> tabViewModel?.navigateToAccountSettings()
                ChatActionType.PREFERENCES -> tabViewModel?.navigateToPreferencesSettings()
                ChatActionType.NOTIFICATIONS -> tabViewModel?.navigateToNotificationsSettings()
                ChatActionType.CONTACT -> tabViewModel?.navigateToContactUs()
                ChatActionType.FEEDBACK -> tabViewModel?.navigateToFeedback()
                ChatActionType.CHANGE_PLAN_LIST -> tabViewModel?.navigateToPlan()
                ChatActionType.SESSION -> tabViewModel?.navigateToChatWithSessionId(chatActionType.data)
                ChatActionType.EXERCISE, ChatActionType.QUOTE, ChatActionType.IMAGE -> {
                    //todo, is not ready yet, the backend needs a refactor
                    tabViewModel?.navigateToExercise(chatActionType.data)
                }
            }
        })
    }

    private fun collapse() {
        fragment_tour_chat_logo.visibility = View.GONE
        blank_view.visibility = View.GONE
        fragment_tour_chat_main_container.setBackgroundColor(resources.getColor(R.color.blackChatBackgroundSoft))

        val set = ConstraintSet()
        set.clone(fragment_tour_chat_main_container)
        set.constrainPercentHeight(fragment_tour_chat_recycler_container.id, 0.3f)
        set.applyTo(fragment_tour_chat_main_container)
    }

    private fun expand() {
        fragment_tour_chat_logo.visibility = View.VISIBLE
        blank_view.visibility = View.VISIBLE
        fragment_tour_chat_main_container.setBackgroundColor(resources.getColor(R.color.blackChatBackgroundHard))

        val set = ConstraintSet()
        set.clone(fragment_tour_chat_main_container)
        set.constrainPercentHeight(fragment_tour_chat_recycler_container.id, 0.5f)
        set.applyTo(fragment_tour_chat_main_container)
    }

    override fun removeObservers() {
        viewModel.collapse.removeObservers(viewLifecycleOwner)
        viewModel.messageList.removeObservers(viewLifecycleOwner)
        viewModel.choices.removeObservers(viewLifecycleOwner)
        viewModel.endChat.removeObservers(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        chatItemAdapter.unregisterAdapterDataObserver(adapterDataObserver)
        viewModel.onViewDestroyed()
        super.onDestroyView()

    }

}