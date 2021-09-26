package com.chatowl.presentation.journal.newone

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.journal.gallery.GalleryItem
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.data.repositories.JournalRepository
import com.chatowl.presentation.chat.CustomUseCameraContract
import com.chatowl.presentation.common.activities.BaseActivity
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.customDisable
import com.chatowl.presentation.common.extensions.customEnable
import com.chatowl.presentation.common.extensions.secondsToMinuteSecondsString
import com.chatowl.presentation.common.utils.FileUtils
import com.chatowl.presentation.journal.edit.EditJournalViewModel
import com.chatowl.presentation.journal.entryitem.EntryItemAdapter
import com.chatowl.presentation.journal.gallery.GalleryActivity
import com.chatowl.presentation.toolbox.mediaexercise.FullscreenPlayerContract
import com.chatowl.presentation.toolbox.mediaexercise.fullscreen.FullscreenPlayerActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.TextWatcherAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_new_journal.*
import kotlinx.android.synthetic.main.fragment_journal_edit.*
import kotlinx.android.synthetic.main.layout_attachments_bar.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import com.chatowl.presentation.common.utils.PermissionsUtils.hasPermission
import com.chatowl.presentation.common.utils.PermissionsUtils.showPermissionLoadRecommendationDialog
import com.chatowl.presentation.common.utils.PreferenceHelper
import com.chatowl.presentation.common.utils.get
import com.chatowl.presentation.common.utils.getMicrophoneAvailable
import kotlinx.android.synthetic.main.layout_attachments_bar.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NewJournalDialogFragment : BottomSheetDialogFragment() {

    private lateinit var journalViewModel: EditJournalViewModel
    private lateinit var adapter: EntryItemAdapter
    private lateinit var fileUtils: FileUtils

    fun getScreenName() = "Compose"

    companion object {
        private val RECORD_AUDIO_PERMISSIONS = Manifest.permission.RECORD_AUDIO

        private val GALLERY_PERMISSIONS = Manifest.permission.READ_EXTERNAL_STORAGE

        private val CAMERA_PERMISSIONS = Manifest.permission.CAMERA
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ChatOwlBottomSheetDialogAdjustResize)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fileUtils = FileUtils(context, getString(R.string.journal))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        //this.requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        //Prevent back key
        dialog?.setOnKeyListener(DialogInterface.OnKeyListener { _, _, _ -> return@OnKeyListener true })
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            this.requireActivity().window.setDecorFitsSystemWindows(false)
        }

        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_new_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewModel()
        val size = Point()
        val display = dialog?.window?.windowManager?.defaultDisplay
        display?.getRealSize(size)
        val height = size.y
        val layoutParams = dialog_new_journal_layout_content.layoutParams
        layoutParams.height = (height * 0.8).toInt()
        dialog_new_journal_layout_content.layoutParams = layoutParams

        adapter = EntryItemAdapter(
            this,
            {
//            TODO: itemClicked
            }, { journalItem ->
                journalViewModel.removeItem(journalItem)
            }, { journalItem ->
                journalViewModel.viewFullscreen(journalItem.id)
            }, { position, id, message ->
                journalViewModel.onTextContentChanged(position, id, message)
            },
            false
        )
        dialog_new_journal_recycler_view.adapter = adapter

        dialog_new_journal_edit_text_helper.imeOptions = EditorInfo.IME_ACTION_DONE
        dialog_new_journal_edit_text_helper.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)

        addObservers()
        setOnClickListeners()
        setSwipeListener()
        setKeyboardVisibilityListener()
    }

    private fun initViewModel() {
        // dependencies
        val application = ChatOwlApplication.get()
        val journalDao = ChatOwlDatabase.getInstance(application).journalDao
        val journalRepository = JournalRepository(journalDao)
        // set edit mode
        val isEdit = true
        journalViewModel = ViewModelProvider(
            this, EditJournalViewModel
                .Factory(application, journalRepository, null, isEdit)
        )
            .get(EditJournalViewModel::class.java)
    }

    private fun addObservers() {
        journalViewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        journalViewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            dialog_new_journal_linear_progress_indicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })

        journalViewModel.errorMessage.observe(viewLifecycleOwner, { stringResourceId ->
            dialog?.window?.decorView?.let { view ->
                Snackbar.make(view, getString(stringResourceId), Snackbar.LENGTH_SHORT).show()
            }
        })

        journalViewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            dialog_new_journal_progress_bar_append.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })

        journalViewModel.isUploading.observe(viewLifecycleOwner, { isUploading ->
            if (isUploading) {
                dialog_new_journal_progress_bar_append.visibility = View.VISIBLE
                dialog_new_journal_scroll_view.fullScroll(View.FOCUS_DOWN)
                dialog_new_journal_image_button_close.customDisable()
                dialog_new_journal_button_save.customDisable()
            } else {
                dialog_new_journal_progress_bar_append.visibility = View.GONE
                dialog_new_journal_image_button_close.customEnable()
                dialog_new_journal_button_save.customEnable()
            }
        })

        journalViewModel.isRecording.observe(viewLifecycleOwner, { isRecording ->
            if (isRecording) {
                dialog_new_journal_layout_keyboard_controls.visibility = View.VISIBLE
                layout_attachments_bar_layout_edit_controls.visibility = View.GONE
                layout_attachments_bar_layout_recording_controls.visibility = View.VISIBLE
            } else {
                layout_attachments_bar_layout_edit_controls.visibility = View.VISIBLE
                layout_attachments_bar_layout_recording_controls.visibility = View.GONE
                closeKeyboard(layout_attachments_bar_layout_recording_controls)
            }
        })

        journalViewModel.journal.observe(viewLifecycleOwner, {
            if (it.components.isNullOrEmpty()) {
                dialog_new_journal_button_save.customDisable()
            } else {
                dialog_new_journal_button_save.customEnable()
            }
        })

        journalViewModel.recordingLength.observe(viewLifecycleOwner, { position ->
            layout_attachments_bar_text_view_recording_duration.text =
                position.secondsToMinuteSecondsString()
            layout_attachments_bar_image_view_progress_full.drawable.level =
                position.times(10000) // max level is 10000
                    .div(120) // max audio length is 120 seconds
                    .coerceIn(0, 10000) // max level is 10000
        })

        journalViewModel.toggleEdit.observe(viewLifecycleOwner, { isEdit ->
            (dialog_new_journal_recycler_view.adapter as EntryItemAdapter).setEditMode(isEdit)
        })

        journalViewModel.showSaveLayout.observe(viewLifecycleOwner, { show ->
            dialog_new_journal_button_save.visibility = if (show) View.VISIBLE else View.GONE
        })

        journalViewModel.showTooltip.observe(viewLifecycleOwner, { showTooltip ->
            dialog_new_journal_layout_tool_tip.visibility =
                if (showTooltip) View.VISIBLE else View.GONE
        })

        journalViewModel.adapterJournalItems.observe(viewLifecycleOwner, { items ->
            adapter.submitList(items)
        })

        journalViewModel.fullscreenPlayer.observe(viewLifecycleOwner, {
            val bundle = Bundle()
            bundle.putParcelable(FullscreenPlayerContract.FULLSCREEN_PLAYER_DATA_KEY, it)
            val fullscreenIntent = Intent(requireActivity(), FullscreenPlayerActivity::class.java)
            fullscreenIntent.putExtras(bundle)
            startActivity(fullscreenIntent)
        })

        journalViewModel.helperText.observe(viewLifecycleOwner, {
            if (dialog_new_journal_edit_text_helper.text.toString() != it) {
                dialog_new_journal_edit_text_helper.setText(it)
                dialog_new_journal_edit_text_helper.setSelection(dialog_new_journal_edit_text_helper.text.length)
            }
        })

        journalViewModel.dismiss.observe(viewLifecycleOwner, {
            if (it) {
                dismiss()
            }
        })
    }

    private fun setOnClickListeners() {
        layout_attachments_bar_image_view_recording_send.setOnClickListener {
            journalViewModel.onSendRecordingClick()
        }

        layout_attachments_bar_text_view_recording_cancel.setOnClickListener {
            journalViewModel.onCancelRecordingClick()
        }

        dialog_new_journal_button_save.setOnClickListener {
            journalViewModel.onSaveClicked()
        }

        dialog_new_journal_image_button_close.setOnClickListener {
            journalViewModel.onNewJournalDialogClosed()
        }

        dialog_new_journal_edit_text_helper.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                journalViewModel.onNewTextEntry()
                closeKeyboard(dialog_new_journal_edit_text_helper)
                true
            } else {
                false
            }
        }

        dialog_new_journal_edit_text_helper.addTextChangedListener(object :
            TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                journalViewModel.onHelperViewTextChanged(s.toString())
            }
        })

        attachments_image_button.setOnClickListener {
            onGalleryClicked()
        }

        attachments_camera_button.setOnClickListener {
            onCameraClicked()
        }

        attachments_video_button.setOnClickListener {
            onVideoClicked()
        }
    }

    private fun setSwipeListener() {
        attachments_audio_button.setOnTouchListener(object : View.OnTouchListener {
            val len = 10
            var deltaY: Float = 0F
            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        journalViewModel.onRecordButtonPressed(true)
                        deltaY = event.y
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        journalViewModel.onRecordButtonPressed(false)
                        deltaY -= event.y //now has difference value
                        if (deltaY > len) {
                            startRecording()
                        }
                        p0?.performClick()
                    }
                }
                return false
            }
        })
    }

    // AUDIO
    private fun startRecording() {
        if (hasPermission(RECORD_AUDIO_PERMISSIONS, recordAudioPermissionLauncher)) {
            if (getMicrophoneAvailable(requireContext())) {
                journalViewModel.startRecording(fileUtils.getAudioFilePath())
            } else {
                Toast.makeText(requireContext(), "Microphone is being used by another app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val recordAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) journalViewModel.startRecording(fileUtils.getAudioFilePath())
            else showPermissionLoadRecommendationDialog(requireActivity())
        }

    // GALLERY
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
        galleryLauncher.launch(Intent(requireActivity(), GalleryActivity::class.java))
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val content =
                    intent?.getParcelableExtra<GalleryItem>(GalleryActivity.GALLERY_CONTENT_KEY)
                content?.let {
                    journalViewModel.onGalleryContentSelected(content)
                }
            }
        }

    // CAMERA
    private fun onCameraClicked() {
        if (hasPermission(CAMERA_PERMISSIONS, cameraPermissionLauncher)) {
            launchCamera()
        }
    }

    // CAMERA FOR VIDEO
    private fun onVideoClicked() {
        if (hasPermission(CAMERA_PERMISSIONS, cameraForVideoLauncher)) {
            launchCamera(true)
        }
    }

    private val cameraForVideoLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera(true)
            else showPermissionLoadRecommendationDialog(requireActivity())
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else showPermissionLoadRecommendationDialog(requireActivity())
        }

    private fun launchCamera(video: Boolean = false) {
        try {
            val filePath = fileUtils.getTempFilePath()
            when {
                video -> openCameraLauncherForVideo.launch(fileUtils.getFileUri(filePath))
                else -> openCameraLauncher.launch(fileUtils.getFileUri(filePath))
            }
        } catch (e: Exception) {
            // Unable to create file, likely because external storage is not currently mounted.
            e.printStackTrace()
        }
    }

    private val openCameraLauncher =
        registerForActivityResult(CustomUseCameraContract()) { successUri ->
            if (successUri != null) {
                journalViewModel.onCameraContentCaptured(successUri)
            }
        }

    private val openCameraLauncherForVideo =
        registerForActivityResult(CustomUseCameraContract(true)) { successUri ->
            if (successUri != null) {
                journalViewModel.onVideoContentCaptured(successUri)
            }
        }

    // KEYBOARD
    private fun setKeyboardVisibilityListener() {
        KeyboardVisibilityEvent.setEventListener(
            requireActivity(),
            viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    journalViewModel.onKeyboardStateChanged(isOpen)
                    dialog_new_journal_layout_keyboard_controls.visibility =
                        if (isOpen) View.VISIBLE else View.GONE
                }
            })
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener {
            val bottomSheetDialog: BottomSheetDialog? = it as? BottomSheetDialog
            val bottomSheetBehavior = bottomSheetDialog?.behavior
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior?.setDraggable(false)
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setOnKeyListener { it, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                journalViewModel.onNewJournalDialogClosed()
                true
            } else {
                false
            }
        }
        return dialog
    }

    private fun closeKeyboard(v: View?) {
        (requireActivity() as? BaseActivity)?.closeKeyboard(v)
    }

    override fun onResume() {
        super.onResume()
        val accessToken =
            PreferenceHelper.getChatOwlPreferences().get(PreferenceHelper.Key.ACCESS_TOKEN, "")
                ?: ""
        if (accessToken.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.i("ViewModelFragment", getScreenName())
                try {
                    ChatOwlUserRepository.sendTracking(
                        PostEventTrackingRequest.ViewedScreenBuilder()
                            .screenName(getScreenName())
                            .toolboxCategoryName("")
                            .therapyPlanName("")
                            .exerciseName("")
                            .build()
                    )
                } catch (e: Exception) {
                    Log.e("ViewModelFragment", e.toString())
                }
            }
        }
    }
}