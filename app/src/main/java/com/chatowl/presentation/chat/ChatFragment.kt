package com.chatowl.presentation.chat

import android.Manifest.permission
import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.generateViewId
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.api.chat.ISocketManager
import com.chatowl.data.api.chat.PlayListPlayer
import com.chatowl.data.api.chat.SocketManager
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.chat.AnswerViewState
import com.chatowl.data.entities.chat.BotChoice
import com.chatowl.data.entities.chat.ChatActionType
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.databinding.FragmentChatBinding
import com.chatowl.presentation.chat.answer.image.fullscreen.FullscreenImageChoiceContract
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.FileUtils
import com.chatowl.presentation.common.utils.MeasureUtils
import com.chatowl.presentation.common.utils.OffsetsItemDecoration
import com.chatowl.presentation.common.utils.PermissionsUtils.hasPermission
import com.chatowl.presentation.common.utils.PermissionsUtils.showPermissionLoadRecommendationDialog
import com.chatowl.presentation.main.MainViewModel
import com.chatowl.presentation.tabs.TabFragment
import com.chatowl.presentation.tabs.TabViewModel
import com.chatowl.presentation.toolbox.host.ToolboxHostFragment
import com.chatowl.presentation.toolbox.mediaexercise.FullscreenPlayerContract
import com.google.android.material.internal.TextWatcherAdapter
import com.instabug.library.logging.InstabugLog
import com.instabug.survey.Surveys
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.layout_chat_answer_image.*
import kotlinx.android.synthetic.main.layout_chat_answer_image.row_chat_answer_image_card_view_camera
import kotlinx.android.synthetic.main.layout_chat_answer_slider_ungraduated.*
import kotlinx.android.synthetic.main.layout_chat_answer_text.*
import java.io.IOException


@ExperimentalStdlibApi
class ChatFragment : ViewModelFragment<ChatViewModel>(ChatViewModel::class.java) {

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    private lateinit var mContext: Context
    private lateinit var tabViewModel: TabViewModel

    private var fadeAnimationLeft: Animator? = null
    private var fadeAnimationCenter: Animator? = null
    private var fadeAnimationRight: Animator? = null

    private lateinit var largeChatItemDecoration: OffsetsItemDecoration
    private lateinit var smallChatItemDecoration: OffsetsItemDecoration

    private lateinit var fileUtils: FileUtils
    private var layoutChangedDueToAnswerShown = false

    private val answerViewStateDelayHandler = Handler(Looper.getMainLooper())

    override fun getScreenName() = "Chat"

    companion object {
        private val answerViewsIds = listOf(
            R.id.fragment_chat_layout_chat_answer_text,
            R.id.fragment_chat_layout_chat_answer_image,
            R.id.fragment_chat_layout_chat_answer_slider_ungraduated
        )

        private val GALLERY_PERMISSIONS = permission.READ_EXTERNAL_STORAGE

        private val CAMERA_PERMISSIONS = permission.CAMERA

        private const val VIEW_STATE_DELAY = 500L
        val NAVIGATE_TO_NOTHING = -1
        val NAVIGATE_TO_TOOLBOX = 1
        val NAVIGATE_TO_JOURNAL = 2

    }

    private val fullscreenPlayerActivity =
        registerForActivityResult(FullscreenPlayerContract()) { result ->
            viewModel.onPlayStopped(result)
        }

    private val fullscreenImageChoiceActivity =
        registerForActivityResult(FullscreenImageChoiceContract()) { result ->
            result?.let {
                viewModel.onImageChoiceClicked(it)
            }
        }

    private val imageChoiceAdapter = ImageChoiceAdapter { item ->
        viewModel.onImageChoiceClicked(item)
    }

    private lateinit var chatItemAdapter: ChatItemAdapter

    override fun setViewModel(): ChatViewModel {
        val application = ChatOwlApplication.get()
        val chatDao = ChatOwlDatabase.getInstance(application).chatDao
        val userPool = application.userPool
        val socketManager:ISocketManager = SocketManager.getInstance(
            application,
            chatDao,
            userPool
        )
        return ViewModelProvider(
            this,
            ChatViewModel.Factory(application, chatDao, socketManager)
        ).get(ChatViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tabFragment = requireParentFragment().requireParentFragment() as TabFragment
        tabViewModel = ViewModelProvider(tabFragment).get(TabViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        fileUtils = FileUtils(context, getString(R.string.temp))
        chatItemAdapter = ChatItemAdapter(context) { item ->
            viewModel.onChatItemClicked(item)
        }
    }


    private lateinit var binding: FragmentChatBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val separationLarge = resources.getDimensionPixelSize(R.dimen.size_96dp)
        val separationSmall = resources.getDimensionPixelSize(R.dimen.corner_radius_large)
        largeChatItemDecoration =
            OffsetsItemDecoration(bottomOffset = separationLarge, applyToFirstExclusive = true)
        smallChatItemDecoration =
            OffsetsItemDecoration(bottomOffset = separationSmall, applyToFirstExclusive = true)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        setupAnswerPanelsMaxHeight()



        viewModel.openChat(tabViewModel.getNavigationParam())

        activity?.let { activity ->
            fadeAnimationLeft = AnimatorInflater.loadAnimator(activity, R.animator.fade_in_and_out)
                .apply {
                    setTarget(binding.fragmentChatDotTypingLeft)
                }
            fadeAnimationCenter =
                AnimatorInflater.loadAnimator(activity, R.animator.fade_in_and_out)
                    .apply {
                        setTarget(binding.fragmentChatDotTypingCenter)
                        startDelay = 200
                    }
            fadeAnimationRight = AnimatorInflater.loadAnimator(activity, R.animator.fade_in_and_out)
                .apply {
                    setTarget(binding.fragmentChatDotTypingRight)
                    startDelay = 400
                }
        }

        setupChatItemsRecyclerView()


        layout_chat_answer_image_recycler_view.adapter = imageChoiceAdapter
        layout_chat_answer_image_text_view_action.setOnClickListener {
            viewModel.onImageChoiceActionClicked()
        }

        row_chat_answer_image_card_view_gallery.setOnClickListener {
            onGalleryClicked()
        }

        row_chat_answer_image_card_view_camera.setOnClickListener {
            onCameraClicked()
        }

        layout_chat_answer_text_edit_text_message.addTextChangedListener(object :
            TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onMessageChanged(s.toString())
            }
        })

        layout_chat_answer_image_view_send.setOnClickListener {
            closeKeyboard(it)
            viewModel.onFreeTextMessageSend(layout_chat_answer_text_edit_text_message.text.toString())
            layout_chat_answer_text_edit_text_message.setText("")
        }

        layout_chat_answer_text_text_view_skip.setOnClickListener {
            closeKeyboard(it)
            viewModel.onFreeTextMessageSend(getString(R.string.dont_wanna_say))
        }

        layout_chat_answer_text_image_button_back.setOnClickListener {
            viewModel.onFreeTextBackClicked()
        }

        layout_chat_answer_slider_ungraduated_seekbar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setSeekBarValue(value.toInt())
            }
        }

        layout_chat_answer_slider_ungraduated_button_done.setOnClickListener {
            it.isEnabled = false
            viewModel.onSliderAnswerDoneClicked()
        }

        binding.fragmentChatFabReset.setOnClickListener {
            viewModel.resetChat()
        }

        binding.fragmentChatTextViewValues.setOnClickListener {
            viewModel.onValuesClicked()
        }

        binding.fragmentChatTextViewFlows.setOnClickListener {
            viewModel.onFlowsClicked()
        }

        binding.fragmentChatRecyclerView.viewTreeObserver.addOnScrollChangedListener {
            binding.fragmentChatRecyclerView.let {
                if (it.canScrollVertically(1)){
                    if ((viewModel.bindScrollEnd.value ?: true) &&
                        (!layoutChangedDueToAnswerShown)) {
                        viewModel.scrollEndReleased()
                    }
                } else {
                    if (!(viewModel.bindScrollEnd.value ?: true)) {
                        viewModel.scrollEndReached()
                    }
                }
            }
        }

        binding.fragmentChatButtonDown.setOnClickListener {
            scrollToBottom()
        }
    }

    fun scrollToBottom() {
        binding.fragmentChatRecyclerView.smoothScrollToPosition(0)
        viewModel.scrollEndReached()
    }


    val playListPlayer = PlayListPlayer.getInstance()
    var currentMessages: List<ChatMessage> = arrayListOf()

    override fun addObservers() {

        mainActivityViewModel.isChatMuted.observe(viewLifecycleOwner, { isMuted ->
            playListPlayer.setMute(isMuted)
            binding.icChatVolume.alpha = if (isMuted) 0.4f else 1.0f
        })

        binding.icChatVolume.setOnClickListener {
            mainActivityViewModel.setChatMuted(!mainActivityViewModel.isChatMuted.value!!)
        }

        viewModel.hideKeyboardOnview.observe(viewLifecycleOwner, {
            when {
                it -> {
                    hideKeyboard()
                }
            }
        })

        viewModel.navigate.observe(viewLifecycleOwner, {
            it?.let { navDirections ->
                if (navDirections != ChatFragmentDirections.actionChatNoPauseAction()) {
                    findNavController().navigate(it)
                }
            }
        })

        viewModel.socketMessage.observe(viewLifecycleOwner, { message ->
            showSnackBarMessage(message)
        })

        viewModel.showTestControls.observe(viewLifecycleOwner, { showControls ->
            if (showControls) {
                binding.fragmentChatTextViewValues.visibility = View.VISIBLE
                binding.fragmentChatTextViewFlows.visibility = View.VISIBLE
                binding.fragmentChatFabReset.visibility = View.VISIBLE
            } else {
                binding.fragmentChatTextViewValues.visibility = View.GONE
                binding.fragmentChatTextViewFlows.visibility = View.GONE
                binding.fragmentChatFabReset.visibility = View.GONE
            }
        })


        viewModel.chatAdapterItems.observe(viewLifecycleOwner, { chatItems ->

            val messages = chatItems.mapNotNull { chatItems -> (chatItems as? ChatAdapterItem.ChatHistoryAdapterItem)?.chatMessage }

            chatItemAdapter.submitList(chatItems)
            val difference = messages.minus(currentMessages)
            val playListToAdd = difference.mapNotNull { it.textToSpeechUrl }
            playListPlayer.addItems(playListToAdd)
            currentMessages = messages

        })

        viewModel.isTyping.observe(viewLifecycleOwner, { isTyping ->
            toggleTypingAnimation(isTyping)
        })

        viewModel.answerViewState.observe(viewLifecycleOwner, { answerViewState ->
            InstabugLog.d("answerViewState -> $answerViewState")
            when {
                answerViewState.textAnswerViewState != null -> {
                    answerViewStateDelayHandler.postDelayed({
                        showTextAnswerView(answerViewState)
                    }, VIEW_STATE_DELAY)
                }
                answerViewState.imageAnswerViewState != null -> {
                    answerViewStateDelayHandler.postDelayed({
                        showImageAnswerView(answerViewState)
                    }, VIEW_STATE_DELAY)
                }
                answerViewState.sliderAnswerViewState != null -> {
                    answerViewStateDelayHandler.postDelayed({
                        showSliderAnswerView(answerViewState)
                    }, VIEW_STATE_DELAY)
                }
                else -> {
                    hideAnswerViews()
                }
            }
        })

        // TEXT ANSWER
        viewModel.messageCharacterCount.observe(viewLifecycleOwner, { count ->
            layout_chat_answer_text_layout_send.visibility =
                if (count > 0) View.VISIBLE else View.GONE
            layout_chat_answer_text_progress_indicator.progress = count
        })

        viewModel.messageLengthLimitReached.observe(viewLifecycleOwner, { limitReached ->
            if (limitReached) {
                layout_chat_answer_text_progress_indicator.setIndicatorColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
                layout_chat_answer_text_progress_indicator.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red_20
                        )
                    )
            } else {
                layout_chat_answer_text_progress_indicator.setIndicatorColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPrimary
                    )
                )
                layout_chat_answer_text_progress_indicator.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue2_20
                        )
                    )
            }
        })

        // SLIDER
        viewModel.seekBarLabel.observe(viewLifecycleOwner, { label ->
            layout_chat_answer_slider_ungraduated_text_view_label.text = label
        })

        // VIDEO FULLSCREEN PLAYER
        viewModel.fullscreenPlayer.observe(viewLifecycleOwner, { playerData ->
            fullscreenPlayerActivity.launch(playerData)
        })

        // IMAGE ANSWER FULLSCREEN
        viewModel.fullScreenChoiceList.observe(viewLifecycleOwner, { botChoices ->
            viewModel.setDummyNavDirection()
            fullscreenImageChoiceActivity.launch(botChoices)
        })

        // INSTABUG TOKEN
        viewModel.surveyToken.observe(viewLifecycleOwner, { token ->
            Surveys.showSurvey(token)
        })

        // MAIN ACTIVITY VIEW MODEL OBSERVING KEYBOARD STATE
        mainActivityViewModel.isKeyboardOpen.observe(viewLifecycleOwner, { isOpen ->
            if (isOpen) {
                if ((viewModel.bindScrollEnd.value == null) || viewModel.bindScrollEnd.value!!) {
                    binding.fragmentChatRecyclerView.scrollToPosition(0)
                }
            }
        })

        mainActivityViewModel.isNetworkAvailable.observe(this, { isNetworkAvailable ->
            if (isNetworkAvailable) {
                layout_no_internet_animation.visibility = View.GONE
            } else {
                layout_no_internet_animation.visibility = View.VISIBLE
                hideKeyboard()
            }
        })

        viewModel.navigateThroughTabView.observe(viewLifecycleOwner, { chatActionType ->
            when(chatActionType) {
                ChatActionType.HOME -> tabViewModel.navigateToHome()
                ChatActionType.PLAN -> tabViewModel.navigateToPlan()
                ChatActionType.CHAT -> tabViewModel.navigateToChat()
                ChatActionType.JOURNEY -> tabViewModel.navigateToJourney()
                ChatActionType.TOOLBOX -> tabViewModel.navigateToToolbox(ToolboxHostFragment.PAGE_HOME)
                ChatActionType.JOURNAL -> tabViewModel.navigateToToolbox(ToolboxHostFragment.PAGE_JOURNAL)
                ChatActionType.ACCOUNT -> tabViewModel.navigateToAccountSettings()
                ChatActionType.PREFERENCES -> tabViewModel.navigateToPreferencesSettings()
                ChatActionType.NOTIFICATIONS -> tabViewModel.navigateToNotificationsSettings()
                ChatActionType.CONTACT -> tabViewModel.navigateToContactUs()
                ChatActionType.FEEDBACK -> tabViewModel.navigateToFeedback()
                ChatActionType.CHANGE_PLAN_LIST -> tabViewModel.navigateToPlan()
                ChatActionType.SESSION -> tabViewModel.navigateToChatWithSessionId(chatActionType.data)
                ChatActionType.EXERCISE, ChatActionType.QUOTE, ChatActionType.IMAGE -> {
                    //todo, is not ready yet, the backend needs a refactor
                    tabViewModel.navigateToExercise(chatActionType.data)
                }
            }
        })

        viewModel.bindScrollEnd.observe(viewLifecycleOwner, {
            when {
                it -> {
                    fragment_chat_button_down.visibility = View.GONE
                }
                else -> {
                        fragment_chat_button_down.visibility = View.VISIBLE
                }
            }
        })
    }

    fun hideKeyboard() {
        val imm: InputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        val view = requireActivity().currentFocus ?: View(requireActivity())

        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun removeObservers() {
        viewModel.hideKeyboardOnview.removeObservers(viewLifecycleOwner)
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.socketMessage.removeObservers(viewLifecycleOwner)
        viewModel.chatAdapterItems.removeObservers(viewLifecycleOwner)
        viewModel.isTyping.removeObservers(viewLifecycleOwner)
        viewModel.answerViewState.removeObservers(viewLifecycleOwner)
        viewModel.messageCharacterCount.removeObservers(viewLifecycleOwner)
        viewModel.messageLengthLimitReached.removeObservers(viewLifecycleOwner)
        viewModel.seekBarLabel.removeObservers(viewLifecycleOwner)
        viewModel.fullscreenPlayer.removeObservers(viewLifecycleOwner)
        viewModel.fullScreenChoiceList.removeObservers(viewLifecycleOwner)
        viewModel.surveyToken.removeObservers(viewLifecycleOwner)
        viewModel.bindScrollEnd.removeObservers(viewLifecycleOwner)
        mainActivityViewModel.isKeyboardOpen.removeObservers(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        answerViewStateDelayHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    private fun showTextAnswerView(answerViewState: AnswerViewState) {
        InstabugLog.d("showTextAnswerView")
        when {
            isTextWithSingleChoice(answerViewState.botChoices ?: emptyList()) -> {
                layout_chat_answer_text_layout_multiple_choice.visibility = View.GONE
                layout_chat_answer_text_layout_free_text.visibility = View.VISIBLE

                layout_chat_answer_text_text_single_choice.visibility = View.VISIBLE
                layout_chat_answer_text_text_single_choice.text =
                    answerViewState!!.botChoices!!.get(0).label
                layout_chat_answer_text_text_single_choice.setOnClickListener {
                    closeKeyboard(it)
                    viewModel.onMultipleChoiceItemClicked(answerViewState!!.botChoices!!.get(0))
                }
                layout_chat_answer_text_flow_multiple_choice.referencedIds =
                    arrayOf<Int>().toIntArray()
            }
            answerViewState.toggleCustomAnswerControls -> {
                layout_chat_answer_text_layout_multiple_choice.visibility = View.GONE
                layout_chat_answer_text_layout_free_text.visibility = View.VISIBLE
                layout_chat_answer_text_text_single_choice.visibility = View.GONE
            }
            else -> {
                layout_chat_answer_text_layout_multiple_choice.visibility = View.VISIBLE
                layout_chat_answer_text_layout_free_text.visibility = View.GONE
                layout_chat_answer_text_text_single_choice.visibility = View.GONE
            }
        }
        if (!isTextWithSingleChoice(answerViewState.botChoices ?: emptyList())) {
            layout_chat_answer_text_flow_multiple_choice.referencedIds = addMultipleChoiceItems(
                answerViewState.botChoices ?: emptyList()
            )
        }

        layout_chat_answer_text_image_button_back.visibility =
            if ((answerViewState.textAnswerViewState?.showCustomTextBack == true) &&
                (!isTextWithSingleChoice(answerViewState.botChoices ?: emptyList()))
            )
                View.VISIBLE else View.GONE

        showAnswerViewById(R.id.fragment_chat_layout_chat_answer_text)
        layoutChangedDueToAnswerShown = true
    }

    private fun showImageAnswerView(answerViewState: AnswerViewState) {
        InstabugLog.d("showImageAnswerView")

        imageChoiceAdapter.submitList(answerViewState.botChoices)

        layout_chat_answer_image_text_view_action.visibility =
            if (answerViewState.imageAnswerViewState?.hideFullscreenControls == true) View.GONE else View.VISIBLE

        if (answerViewState.toggleCustomAnswerControls) {
            layout_chat_answer_image_recycler_view.visibility = View.GONE
            layout_chat_answer_image_layout_custom_choice.visibility = View.VISIBLE

            layout_chat_answer_image_text_view_title.text =
                getString(R.string.select_from_gallery_or_take_picture)
            layout_chat_answer_image_text_view_action.text = getString(R.string.back)
        } else {
            layout_chat_answer_image_recycler_view.visibility = View.VISIBLE
            layout_chat_answer_image_layout_custom_choice.visibility = View.GONE

            layout_chat_answer_image_text_view_title.text = getString(R.string.please_select_image)
            layout_chat_answer_image_text_view_action.text = getString(R.string.fullscreen)
        }
        showAnswerViewById(R.id.fragment_chat_layout_chat_answer_image)
    }

    private fun showSliderAnswerView(answerViewState: AnswerViewState) {
        InstabugLog.d("showSliderAnswerView")

        val sliderAnswerViewState = answerViewState.sliderAnswerViewState
        layout_chat_answer_slider_ungraduated_text_view_title.text = sliderAnswerViewState?.title
        val botChoices = answerViewState.botChoices

        layout_chat_answer_slider_ungraduated_seekbar.valueTo = (botChoices?.size?.toFloat()
            ?: 1F) - 1F
        layout_chat_answer_slider_ungraduated_seekbar.value =
            sliderAnswerViewState?.defaultChoiceStep?.toFloat()
                ?: 0F

        InstabugLog.d(
            "showSliderAnswerView -> botChoices: ${botChoices?.map { it.label }}, defaultValue: ${
                sliderAnswerViewState?.defaultChoiceStep?.toFloat() ?: 0F
            }"
        )

        viewModel.setSeekBarValue(sliderAnswerViewState?.defaultChoiceStep ?: 0)
        layout_chat_answer_slider_ungraduated_button_done.isEnabled = true

        showAnswerViewById(R.id.fragment_chat_layout_chat_answer_slider_ungraduated)
    }

    private fun hideAnswerViews() {
        showAnswerViewById(null)
    }

    private fun showAnswerViewById(viewId: Int?) {
        answerViewsIds.forEach { answerControlId ->
            val view: View? = view?.findViewById(answerControlId)
            if (viewId == answerControlId) {
                if (view?.visibility == View.GONE) view.visibility = View.VISIBLE
            } else {
                if (view?.visibility != View.GONE) view?.visibility = View.GONE
            }
        }
        if (viewId == null) {
            if (fragment_chat_recycler_view.itemDecorationCount > 0) {
                fragment_chat_recycler_view.removeItemDecorationAt(0)
                fragment_chat_recycler_view.addItemDecoration(largeChatItemDecoration, 0)
            }
        } else {
            if (fragment_chat_recycler_view.itemDecorationCount > 0) {
                fragment_chat_recycler_view.removeItemDecorationAt(0)
                fragment_chat_recycler_view.addItemDecoration(smallChatItemDecoration, 0)
            }
        }
    }

    private fun addMultipleChoiceItems(items: List<BotChoice>): IntArray {
        InstabugLog.d("addMultipleChoiceItems -> botChoices: ${items.map { it.label }}")

        val childCount = layout_chat_answer_text_layout_multiple_choice.childCount
        // remove all children except for the flow helper
        layout_chat_answer_text_flow_multiple_choice.referencedIds = arrayOf<Int>().toIntArray()
        for (i in (childCount - 1) downTo 0) {
            val child = layout_chat_answer_text_layout_multiple_choice.getChildAt(i)
            if (child !is Flow) {
                layout_chat_answer_text_layout_multiple_choice.removeView(child)
            }
        }
        // repopulate with new children
        val flowIds = mutableListOf<Int>()
        for (item in items) {
            val textView = createSingleChoiceFromItem(item)
            layout_chat_answer_text_flow_multiple_choice.addView(textView)
            flowIds.add(textView.id)
        }
        return flowIds.toIntArray()
    }

    fun createSingleChoiceFromItem(item: BotChoice): TextView {
        val textView = TextView(
            ContextThemeWrapper(
                context,
                when {
                    item.label.equals(resources.getString(R.string.let_me_type)) -> {
                        R.style.ChatOwlChoiceTextStyleSuggested
                    }
                    else -> {
                        R.style.ChatOwlChoiceTextStyle
                    }
                }
            ), null, 0
        )
        textView.height = resources.getDimensionPixelSize(R.dimen.size_40dp)
        textView.text = item.label
        textView.id = generateViewId()
        layout_chat_answer_text_layout_multiple_choice.addView(textView)
        textView.setOnClickListener {
            closeKeyboard(it)
            viewModel.onMultipleChoiceItemClicked(item)
        }
        return textView
    }

    fun isTextWithSingleChoice(botChoices: List<BotChoice>): Boolean {
        botChoices?.let {
            return ((it.size ?: 0 == 2) &&
                    (it?.filter {
                        it.label.equals(
                            resources.getString(R.string.let_me_type),
                            true
                        )
                    }.size > 0))
        }
        return false
    }

    private fun setupAnswerPanelsMaxHeight() {
        // Half of screen height
        val answerPanelMaxHeight = MeasureUtils.getScreenHeightPercentage(mContext, 0.5)
        // Set constraints
        val constraintSet = ConstraintSet()
        constraintSet.clone(fragment_chat_layout_main)
        constraintSet.constrainMaxHeight(
            R.id.fragment_chat_layout_chat_answer_text,
            answerPanelMaxHeight
        )
        constraintSet.constrainMaxHeight(
            R.id.fragment_chat_layout_chat_answer_image,
            answerPanelMaxHeight
        )
        constraintSet.applyTo(fragment_chat_layout_main)
    }

    private fun setupChatItemsRecyclerView() {
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = true
        fragment_chat_recycler_view.addItemDecoration(largeChatItemDecoration, 0)
        fragment_chat_recycler_view.layoutManager = layoutManager
        chatItemAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                when {
                    viewModel.bindScrollEnd?.value ?: true ->
                        fragment_chat_recycler_view?.scrollToPosition(positionStart)
                }

            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
            }
        })
        fragment_chat_recycler_view.adapter = chatItemAdapter

        var layoutChangeListener =
            View.OnLayoutChangeListener() { v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
                if ((bottom != oldBottom) && (viewModel.bindScrollEnd.value ?: true)) {
                    scrollToBottom()
                }
            }
        fragment_chat_recycler_view.addOnLayoutChangeListener(layoutChangeListener)

        fragment_chat_recycler_view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                layoutChangedDueToAnswerShown = false
                return false
            }
        })

        scrollToBottom()
    }

    private fun toggleTypingAnimation(on: Boolean) {
        if (on) {
            fragment_chat_layout_typing.visibility = View.VISIBLE
            if (fadeAnimationLeft?.isPaused == true || fadeAnimationLeft?.isStarted == false) {
                if (fadeAnimationLeft?.isPaused == true) fadeAnimationLeft?.resume() else fadeAnimationLeft?.start()
                if (fadeAnimationCenter?.isPaused == true) fadeAnimationCenter?.resume() else fadeAnimationCenter?.start()
                if (fadeAnimationRight?.isPaused == true) fadeAnimationRight?.resume() else fadeAnimationRight?.start()
            }
        } else {
            fragment_chat_layout_typing.visibility = View.GONE
            fadeAnimationLeft?.pause()
            fadeAnimationCenter?.pause()
            fadeAnimationRight?.pause()
        }
    }

    private fun onGalleryClicked() {
        if (hasPermission(GALLERY_PERMISSIONS, galleryPermissionLauncher)) {
            launchGallery()
        }
    }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchGallery()
            else showPermissionLoadRecommendationDialog(requireActivity())
        }

    private fun launchGallery() {
        viewModel.setDummyNavDirection()
        galleryLauncher.launch("image/*")
    }

    private val galleryLauncher = registerForActivityResult(GetContent()) { sourceUri ->
        if (sourceUri != null) {
            try {
                val uri = fileUtils.copyFile(sourceUri)
                viewModel.onCustomImageSelected(uri)
            } catch (e: NoSuchFileException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun onCameraClicked() {
        if (hasPermission(CAMERA_PERMISSIONS, cameraPermissionLauncher)) {
            launchCamera()
        }
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else showPermissionLoadRecommendationDialog(requireActivity())
        }

    private fun launchCamera() {
        try {
            val filePath = fileUtils.getTempFilePath()
            viewModel.setDummyNavDirection()
            openCameraLauncher.launch(fileUtils.getFileUri(filePath))
        } catch (e: Exception) {
            // Unable to create file, likely because external storage is not currently mounted.
            e.printStackTrace()
        }
    }

    private val openCameraLauncher =
        registerForActivityResult(CustomUseCameraContract()) { successUri ->
            if (successUri != null) {
                Log.d("ChatFragment", "CAMERA - $successUri")
                viewModel.onCustomImageSelected(successUri)
            }
        }
}