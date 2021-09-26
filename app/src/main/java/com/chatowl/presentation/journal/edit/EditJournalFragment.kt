package com.chatowl.presentation.journal.edit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.journal.gallery.GalleryItem
import com.chatowl.data.repositories.JournalRepository
import com.chatowl.presentation.chat.CustomUseCameraContract
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.customDisable
import com.chatowl.presentation.common.extensions.customEnable
import com.chatowl.presentation.common.extensions.secondsToMinuteSecondsString
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.FileUtils
import com.chatowl.presentation.common.utils.PermissionsUtils.hasPermission
import com.chatowl.presentation.common.utils.PermissionsUtils.showPermissionLoadRecommendationDialog
import com.chatowl.presentation.common.utils.getMicrophoneAvailable
import com.chatowl.presentation.journal.gallery.GalleryActivity
import com.chatowl.presentation.journal.gallery.GalleryActivity.Companion.GALLERY_CONTENT_KEY
import com.chatowl.presentation.journal.entryitem.EntryItemAdapter
import com.chatowl.presentation.toolbox.mediaexercise.FullscreenPlayerContract.Companion.FULLSCREEN_PLAYER_DATA_KEY
import com.chatowl.presentation.toolbox.mediaexercise.fullscreen.FullscreenPlayerActivity
import kotlinx.android.synthetic.main.dialog_new_journal.*
import kotlinx.android.synthetic.main.fragment_journal_edit.*
import kotlinx.android.synthetic.main.layout_attachments_bar.*
import kotlinx.android.synthetic.main.layout_attachments_bar.view.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


class EditJournalFragment :
    ViewModelFragment<EditJournalViewModel>(EditJournalViewModel::class.java) {

    private lateinit var adapter: EntryItemAdapter
    private lateinit var fileUtils: FileUtils

    override fun getScreenName() = "Entry detail"

    companion object {
        private val RECORD_AUDIO_PERMISSIONS = Manifest.permission.RECORD_AUDIO

        private val GALLERY_PERMISSIONS = Manifest.permission.READ_EXTERNAL_STORAGE

        private val CAMERA_PERMISSIONS = Manifest.permission.CAMERA
    }

    override fun setViewModel(): EditJournalViewModel {
        val application = ChatOwlApplication.get()
        val journalDao = ChatOwlDatabase.getInstance(application).journalDao
        val journalRepository = JournalRepository(journalDao)
        val args: EditJournalFragmentArgs by navArgs()
        val isEdit = false
        return ViewModelProvider(
            this,
            EditJournalViewModel.Factory(application, journalRepository, args.journal, isEdit)
        ).get(
            EditJournalViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            this.requireActivity().window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        return inflater.inflate(R.layout.fragment_journal_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        fragment_journal_edit_toolbar.enableSecondaryAction(R.drawable.ic_new_entry) {
            viewModel.toggleEditMode()
        }

        adapter = EntryItemAdapter(
        this,
        {
//            TODO: itemClicked
        }, { journalItem ->
            viewModel.removeItem(journalItem)
        }, { journalItem ->
            viewModel.viewFullscreen(journalItem.id)
        }, { position, id, message ->
            viewModel.onTextContentChanged(position, id, message)
        },
            false
        )
        fragment_journal_edit_recycler_view.adapter = adapter

        setOnClickListeners()
        setSwipeListener()
        setKeyboardVisibilityListener()
    }

    private fun setSwipeListener() {
        attachments_audio_button.setOnTouchListener(object : View.OnTouchListener {
            val len = 10
            var deltaY: Float = 0F
            override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    ACTION_DOWN -> {
                        viewModel.onRecordButtonPressed(true)
                        deltaY = event.y
                        return false
                    }
                    ACTION_UP -> {
                        viewModel.onRecordButtonPressed(false)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fileUtils = FileUtils(context, getString(R.string.journal))
    }

    private fun setOnClickListeners() {
        layout_attachments_bar_image_view_recording_send.setOnClickListener {
            viewModel.onSendRecordingClick()
        }

        layout_attachments_bar_text_view_recording_cancel.setOnClickListener {
            viewModel.onCancelRecordingClick()
        }

        fragment_journal_edit_button_save.setOnClickListener {
            viewModel.onSaveClicked()
        }

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

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        viewModel.header.observe(viewLifecycleOwner, { header ->
            // TODO format date
            fragment_journal_edit_toolbar.setTitle(header)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, { stringResourceId ->
            showSnackBarMessage(getString(stringResourceId))
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_journal_edit_linear_progress_indicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.journal.observe(viewLifecycleOwner, {
            if (it.components.isNullOrEmpty()) {
                fragment_journal_edit_toolbar.customDisable()
                fragment_journal_edit_button_save.customDisable()
            } else {
                fragment_journal_edit_toolbar.customEnable()
                fragment_journal_edit_button_save.customEnable()
            }
        })

        viewModel.isUploading.observe(viewLifecycleOwner, { isUploading ->
            if (isUploading) {
                fragment_journal_edit_progress_bar_append.visibility = View.VISIBLE
                fragment_journal_edit_scrollview.fullScroll(View.FOCUS_DOWN)
                fragment_journal_edit_toolbar.customDisable()
                fragment_journal_edit_button_save.customDisable()
            } else {
                fragment_journal_edit_progress_bar_append.visibility = View.GONE
                fragment_journal_edit_toolbar.customEnable()
                fragment_journal_edit_button_save.customEnable()
            }
        })

        viewModel.isRecording.observe(viewLifecycleOwner, { isRecording ->
            if (isRecording) {
                fragment_journal_edit_layout_keyboard_controls.visibility = View.VISIBLE
                layout_attachments_bar_layout_edit_controls.visibility = View.GONE
                layout_attachments_bar_layout_recording_controls.visibility = View.VISIBLE
            } else {
                layout_attachments_bar_layout_edit_controls.visibility = View.VISIBLE
                layout_attachments_bar_layout_recording_controls.visibility = View.GONE
                closeKeyboard(layout_attachments_bar_layout_recording_controls)
            }
        })

        viewModel.recordingLength.observe(viewLifecycleOwner, { position ->
            layout_attachments_bar_text_view_recording_duration.text =
                position.secondsToMinuteSecondsString()
            layout_attachments_bar_image_view_progress_full.drawable.level =
                position.times(10000) // max level is 10000
                    .div(120) // max audio length is 120 seconds
                    .coerceIn(0, 10000) // max level is 10000
        })

        viewModel.toggleEdit.observe(viewLifecycleOwner, { isEdit ->
            if (isEdit) fragment_journal_edit_toolbar.hideSecondaryAction()
            else fragment_journal_edit_toolbar.showSecondaryAction()
            (fragment_journal_edit_recycler_view.adapter as EntryItemAdapter).setEditMode(isEdit)
        })

        viewModel.showSaveLayout.observe(viewLifecycleOwner, { showSave ->
            fragment_journal_edit_button_save.visibility = if (showSave) View.VISIBLE else View.GONE
        })

        viewModel.showTooltip.observe(viewLifecycleOwner, { showTooltip ->
            fragment_journal_layout_tool_tip.visibility =
                if (showTooltip) View.VISIBLE else View.GONE
        })

        viewModel.adapterJournalItems.observe(viewLifecycleOwner, { list ->
            adapter.submitList(list)
        })

        viewModel.fullscreenPlayer.observe(viewLifecycleOwner, {
            val bundle = Bundle()
            bundle.putParcelable(FULLSCREEN_PLAYER_DATA_KEY, it)
            val fullscreenIntent = Intent(requireActivity(), FullscreenPlayerActivity::class.java)
            fullscreenIntent.putExtras(bundle)
            startActivity(fullscreenIntent)
        })

        viewModel.dismiss.observe(viewLifecycleOwner, {
            if(it) {
                findNavController().navigateUp()
            }
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.header.removeObservers(viewLifecycleOwner)
        viewModel.errorMessage.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.isUploading.removeObservers(viewLifecycleOwner)
        viewModel.isRecording.removeObservers(viewLifecycleOwner)
        viewModel.recordingLength.removeObservers(viewLifecycleOwner)
        viewModel.toggleEdit.removeObservers(viewLifecycleOwner)
        viewModel.showSaveLayout.removeObservers(viewLifecycleOwner)
        viewModel.showTooltip.removeObservers(viewLifecycleOwner)
        viewModel.adapterJournalItems.removeObservers(viewLifecycleOwner)
        viewModel.fullscreenPlayer.removeObservers(viewLifecycleOwner)
        viewModel.helperText.removeObservers(viewLifecycleOwner)
        viewModel.dismiss.removeObservers(viewLifecycleOwner)
    }

    // AUDIO
    private fun startRecording() {
        if (hasPermission(RECORD_AUDIO_PERMISSIONS, recordAudioPermissionLauncher)) {
            if (getMicrophoneAvailable(requireContext())) {
                viewModel.startRecording(fileUtils.getAudioFilePath())
            } else {
                Toast.makeText(requireContext(), "Microphone is being used by another app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val recordAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) viewModel.startRecording(fileUtils.getAudioFilePath())
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
                val content = intent?.getParcelableExtra<GalleryItem>(GALLERY_CONTENT_KEY)
                content?.let {
                    viewModel.onGalleryContentSelected(content)
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
        if (hasPermission(CAMERA_PERMISSIONS, cameraPermissionLauncher)) {
            launchCamera(true)
        }
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
                viewModel.onCameraContentCaptured(successUri)
            }
        }

    private val openCameraLauncherForVideo =
        registerForActivityResult(CustomUseCameraContract(true)) { successUri ->
            if (successUri != null) {
                viewModel.onVideoContentCaptured(successUri)
            }
        }

    // KEYBOARD
    private fun setKeyboardVisibilityListener() {
        KeyboardVisibilityEvent.setEventListener(
            requireActivity(),
            viewLifecycleOwner,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    viewModel.onKeyboardStateChanged(isOpen)
                    fragment_journal_edit_layout_keyboard_controls.visibility =
                        if (isOpen) View.VISIBLE else View.GONE
                }
            })
    }

}