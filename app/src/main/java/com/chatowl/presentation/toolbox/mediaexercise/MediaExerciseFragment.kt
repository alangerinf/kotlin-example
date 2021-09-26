package com.chatowl.presentation.toolbox.mediaexercise

import android.animation.AnimatorInflater
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.secondsToDescriptiveString
import com.chatowl.presentation.common.extensions.setOnUserCheckedChangeListener
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.instabug.library.logging.InstabugLog
import kotlinx.android.synthetic.main.fragment_media_exercise.*


class MediaExerciseFragment :
    ViewModelFragment<MediaExerciseViewModel>(MediaExerciseViewModel::class.java) {

//    private val toolAdapter = ToolboxItemAdapter { tool ->
//        viewModel.onSuggestedToolClicked(tool)
//    }

    val args: MediaExerciseFragmentArgs by navArgs()

    override fun getToolboxCategoryName() = args.exercise.tool.toolCategory?.name ?: ""
    override fun getExerciseName() = args.exercise.tool.title

    private val playExerciseActivity =
        registerForActivityResult(FullscreenPlayerContract()) { result ->
            viewModel.onPlayStopped(result)
        }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            viewModel.broadcastReceived(id)
            //if (downloadID === id) {
            println("Download Completed")
            //}
        }
    }


    override fun setViewModel(): MediaExerciseViewModel {
        val application = ChatOwlApplication.get()
        val toolboxRepository =
            ToolboxRepository(ChatOwlDatabase.getInstance(application).toolboxDao)
        val args: MediaExerciseFragmentArgs by navArgs()
        return ViewModelProvider(
            this,
            MediaExerciseViewModel.Factory(application, args, toolboxRepository)
        ).get(MediaExerciseViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //fragment_media_exercise_recycler_view_suggested.adapter = toolAdapter

        fragment_media_exercise_view_background_click_handler.setOnClickListener {
            viewModel.onPlayClicked()
        }

        val animationToggleFavorite =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.scale_up_and_down)
                .apply { setTarget(fragment_media_exercise_toggle_favorite) }
        fragment_media_exercise_toggle_favorite.setOnUserCheckedChangeListener { isChecked ->
            animationToggleFavorite.start()
            viewModel.onFavoriteClicked(isChecked)
        }

        fragment_media_exercise_card_view_mandatory_exercise.setOnClickListener {
            viewModel.onMandatoryToolClicked()
        }

        fragment_media_exercise_image_view_download.setOnClickListener {
            viewModel.onDownloadClicked()
        }

        fragment_media_exercise_button_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        context?.registerReceiver(
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    override fun onPause() {
        context?.unregisterReceiver(onDownloadComplete)
        super.onPause()
    }

    override fun addObservers() {

        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        viewModel.header.observe(viewLifecycleOwner, { header ->
            //fragment_media_exercise_toolbar.setTitle(header ?: "")
        })

        viewModel.isChatSource.observe(viewLifecycleOwner, { isChatSource ->
            if (isChatSource) {
                fragment_media_exercise_toggle_favorite.visibility = View.INVISIBLE
                fragment_media_exercise_image_view_download.visibility = View.INVISIBLE
                fragment_media_exercise_text_view_played.visibility = View.INVISIBLE
            }
        })

        viewModel.fullscreenPlayer.observe(viewLifecycleOwner, { playInfo ->
            playExerciseActivity.launch(playInfo)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_media_exercise_image_view_preview_icon.visibility =
                if (isLoading) View.GONE else View.VISIBLE
            fragment_media_exercise_progressbar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.previewIconResourceId.observe(viewLifecycleOwner, { resourceId ->
            fragment_media_exercise_image_view_preview_icon.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    resourceId
                )
            )

            var colorFilter = PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP)
            fragment_media_exercise_image_view_background.colorFilter = colorFilter
        })

        viewModel.title.observe(viewLifecycleOwner, { title ->
            fragment_media_exercise_text_view_title.text = title
        })

        viewModel.description.observe(viewLifecycleOwner, { description ->
            fragment_media_exercise_text_view_body.text = description
        })

        viewModel.backgroundUrl.observe(viewLifecycleOwner, { backgroundUrl ->
            GlideApp.with(requireContext()).load(backgroundUrl)
                .into(fragment_media_exercise_image_view_background)
        })

        viewModel.isFavorite.observe(viewLifecycleOwner, { isFavorite ->
            fragment_media_exercise_toggle_favorite.isChecked = isFavorite
        })

        viewModel.isDownloaded.observe(viewLifecycleOwner, { isDownloaded ->
            fragment_media_exercise_image_view_download.setImageDrawable(
                if (isDownloaded) ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_download_filled
                )
                else ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_download_outline_gray
                )
            )
        })

        viewModel.isDownloading.observe(viewLifecycleOwner, { isDownloading ->
            if (isDownloading) {
                fragment_media_exercise_lottie_view_cv_download.visibility = View.VISIBLE
                fragment_media_exercise_lottie_view_download.playAnimation()
            } else {
                fragment_media_exercise_lottie_view_cv_download.visibility = View.GONE
                fragment_media_exercise_lottie_view_download.pauseAnimation()
            }
        })

        viewModel.status.observe(viewLifecycleOwner, {
            when {
                // Paywalled
                it.first -> {
                    fragment_media_exercise_layout_exercise_information_detail.visibility =
                        View.GONE
                    fragment_media_exercise_button_subscribe.visibility = View.VISIBLE
                    fragment_media_exercise_layout_mandatory_session.visibility = View.GONE
                }
                // Locked
                it.second -> {
                    fragment_media_exercise_layout_exercise_information_detail.visibility =
                        View.GONE
                    fragment_media_exercise_button_subscribe.visibility = View.GONE
                    fragment_media_exercise_layout_mandatory_session.visibility = View.VISIBLE
                }
                // Not within limits
                it.third -> {
                    fragment_media_exercise_layout_exercise_information_detail.visibility =
                        View.GONE
                    fragment_media_exercise_button_subscribe.visibility = View.GONE
                    fragment_media_exercise_layout_mandatory_session.visibility = View.GONE
                }
                // Available
                else -> {
                    fragment_media_exercise_layout_exercise_information_detail.visibility =
                        View.VISIBLE
                    fragment_media_exercise_button_subscribe.visibility = View.GONE
                    fragment_media_exercise_layout_mandatory_session.visibility = View.GONE
                }
            }
        })

        viewModel.progressBarInformation.observe(viewLifecycleOwner, { progressInformation ->
            InstabugLog.d("MediaExerciseFragment -> progressInformation: ${progressInformation.first}, ${progressInformation.second}")
            fragment_media_exercise_progressbar_watch_position.max = progressInformation.first
            if (progressInformation.second > 0) {
                fragment_media_exercise_progressbar_watch_position.progress =
                    progressInformation.second
                fragment_media_exercise_layout_resume.visibility = View.VISIBLE
            } else {
                fragment_media_exercise_layout_resume.visibility = View.GONE
            }
        })

        viewModel.mediaTypeResourceId.observe(viewLifecycleOwner, { resourceId ->
            fragment_media_exercise_text_view_type.setCompoundDrawablesWithIntrinsicBounds(
                resourceId,
                0,
                0,
                0
            )
        })

        viewModel.statistics.observe(viewLifecycleOwner, { stats ->
            // set stats information
            fragment_media_exercise_text_view_type.text = stats.first
            fragment_media_exercise_text_view_time.text =
                stats.second?.secondsToDescriptiveString(requireContext())
            fragment_media_exercise_text_view_played.text = stats.third.toString()
        })

        viewModel.instructions.observe(viewLifecycleOwner, {
            fragment_media_exercise_text_view_preparation.visibility =
                if (it.first || it.second || it.third) View.VISIBLE else View.GONE
            fragment_media_exercise_text_view_make_yourself_comfortable.visibility =
                if (it.first) View.VISIBLE else View.GONE
            fragment_media_exercise_text_view_dim_lights.visibility =
                if (it.second) View.VISIBLE else View.GONE
            fragment_media_exercise_text_view_use_headphones.visibility =
                if (it.third) View.VISIBLE else View.GONE
        })

        viewModel.pendingExercise.observe(viewLifecycleOwner, { dueExercise ->
            dueExercise?.let {
                populateDueExercise(it)
            }
        })

        viewModel.done.observe(viewLifecycleOwner, { isDone ->
            if (isDone) {
                fragment_media_exercise_layout_exercise_done.visibility = View.VISIBLE
                fragment_media_exercise_layout_exercise_information.visibility = View.GONE
            } else {
                fragment_media_exercise_layout_exercise_done.visibility = View.GONE
                fragment_media_exercise_layout_exercise_information.visibility = View.VISIBLE
            }
        })

        viewModel.showRateExercise.observe(viewLifecycleOwner, { (id, success) ->
            if (success) {
                val b = Bundle()
                b.putInt("id", args.exercise.toolId)
                findNavController().navigate(R.id.action_mediaExercise_to_rateDialogFragment, b)
            }
        })

//        viewModel.relatedToolList.observe(viewLifecycleOwner, { relatedTools ->
//            if (relatedTools.isNullOrEmpty()) {
//                fragment_media_exercise_text_view_suggested_title.visibility = View.GONE
//                fragment_media_exercise_recycler_view_suggested.visibility = View.GONE
//            } else {
//                toolAdapter.submitList(relatedTools)
//                fragment_media_exercise_text_view_suggested_title.visibility = View.VISIBLE
//                fragment_media_exercise_recycler_view_suggested.visibility = View.VISIBLE
//            }
//        })
    }

//    private fun populateSuggestedExercise(toolboxItem: ToolboxItem) {
//        row_activity_large_image_view_favorite.visibility = if(toolboxItem.isFavorite) View.VISIBLE else View.GONE
//        row_activity_large_text_view_title.text = toolboxItem.tool.title
//        row_toolbox_tool_text_view_duration.text = toolboxItem.tool.duration?.secondsToDescriptiveString(requireContext())
//        row_activity_large_image_view_icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), toolboxItem.tool.toolboxIcon()))
//
//        GlideApp.with(row_activity_large_image_view)
//            .load(toolboxItem.thumbnailUrl)
//            .into(row_activity_large_image_view)
//    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.fullscreenPlayer.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.previewIconResourceId.removeObservers(viewLifecycleOwner)
        viewModel.title.removeObservers(viewLifecycleOwner)
        viewModel.description.removeObservers(viewLifecycleOwner)
        viewModel.backgroundUrl.removeObservers(viewLifecycleOwner)
        viewModel.isFavorite.removeObservers(viewLifecycleOwner)
        viewModel.status.removeObservers(viewLifecycleOwner)
        viewModel.progressBarInformation.removeObservers(viewLifecycleOwner)
        viewModel.mediaTypeResourceId.removeObservers(viewLifecycleOwner)
        viewModel.statistics.removeObservers(viewLifecycleOwner)
        viewModel.instructions.removeObservers(viewLifecycleOwner)
        viewModel.pendingExercise.removeObservers(viewLifecycleOwner)
        //viewModel.relatedToolList.removeObservers(viewLifecycleOwner)
        viewModel.done.removeObservers(viewLifecycleOwner)
    }

    private fun populateDueExercise(toolboxItem: ToolboxItem) {
        if (toolboxItem.tool.duration != null) {
            fragment_media_exercise_text_view_due_exercise_duration.text =
                toolboxItem.tool.duration?.secondsToDescriptiveString(requireContext())
            fragment_media_exercise_text_view_due_exercise_duration.visibility = View.VISIBLE
        } else {
            fragment_media_exercise_text_view_due_exercise_duration.visibility = View.GONE
        }

        GlideApp.with(requireContext()).load(toolboxItem.tool.thumbnailUrl)
            .into(fragment_media_exercise_image_view_due_exercise_background)

        val drawableResourceId = toolboxItem.tool.toolboxIcon()

        fragment_media_exercise_image_view_due_exercise_type_icon.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                drawableResourceId
            )
        )
    }
}