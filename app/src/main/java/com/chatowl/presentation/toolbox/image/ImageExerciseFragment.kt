package com.chatowl.presentation.toolbox.image

import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.BuildConfig
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.setOnUserCheckedChangeListener
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.getStringTimestampForMedia
import kotlinx.android.synthetic.main.fragment_image_exercise.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ImageExerciseFragment :
    ViewModelFragment<ImageExerciseViewModel>(ImageExerciseViewModel::class.java) {

    private lateinit var mContext: Context
    private lateinit var mediaPath: File

    val args: ImageExerciseFragmentArgs by navArgs()

    override fun getToolboxCategoryName() = args.exercise.tool.toolCategory?.name ?: ""
    override fun getExerciseName() = args.exercise.tool.title

    override fun setViewModel(): ImageExerciseViewModel {
        val application = ChatOwlApplication.get()
        val repository = ToolboxRepository(ChatOwlDatabase.getInstance(application).toolboxDao)
        val args: ImageExerciseFragmentArgs by navArgs()
        return ViewModelProvider(this, ImageExerciseViewModel.Factory(application, args, repository)).get(ImageExerciseViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mediaPath = File(context.getExternalFilesDir(null), getString(R.string.temp))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_image_exercise_image_view_close.setOnClickListener {
            findNavController().navigateUp()
        }

//        fragment_image_exercise_layout_suggested.setOnClickListener {
//            viewModel.onSuggestedExerciseClicked()
//        }

        val animationToggleFavorite =
            AnimatorInflater.loadAnimator(requireContext(), R.animator.scale_up_and_down)
                .apply { setTarget(fragment_image_exercise_toggle_favorite) }
        fragment_image_exercise_toggle_favorite.setOnUserCheckedChangeListener { isChecked ->
            animationToggleFavorite.start()
            viewModel.onFavoriteClicked(isChecked)
        }

        fragment_image_exercise_image_view_share.setOnClickListener {
            share()
        }
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.isFavorite.observe(viewLifecycleOwner, { isFavorite ->
            fragment_image_exercise_toggle_favorite.isChecked = isFavorite
        })
        // hide controls if the exercise came from the chat
        viewModel.isChatSource.observe(viewLifecycleOwner, { isChatSource ->
            if (isChatSource) {
                fragment_image_exercise_image_view_share.visibility = View.GONE
                fragment_image_exercise_toggle_favorite.visibility = View.GONE
            }
        })
        // reset animation if another image exercise is selected
        viewModel.resetAnimation.observe(viewLifecycleOwner, {
            fragment_image_exercise_layout_main.progress = 0F
        })
        viewModel.exercise.observe(viewLifecycleOwner, { exercise ->
            // background image
            GlideApp.with(requireContext()).load(exercise.tool.imageUrl)
                .into(fragment_image_exercise_image_view_background)
            // description
            fragment_image_exercise_text_view_description.text = exercise.tool.description
            // favorite
            fragment_image_exercise_toggle_favorite.isChecked = exercise.tool.isFavorite
        })
//        viewModel.suggestedExercise.observe(viewLifecycleOwner, { suggestedExercise ->
//            suggestedExercise?.let {
//                row_activity_large_text_view_title.text = it.tool.title
//                row_activity_large_text_view_description.text = it.tool.description
//                val color = it.tool.toolCategory?.color?.toColorInt()
//                    ?: ContextCompat.getColor(requireContext(), R.color.colorPrimary)
//                row_activity_large_text_view_description.setTextColor(color)
//                row_activity_large_text_tag.text = getString(R.string.suggested)
//                row_activity_large_text_tag.visibility = View.VISIBLE
//
//                GlideApp.with(requireContext())
//                    .load(it.thumbnailUrl)
//                    .into(row_activity_large_image_view)
//
//                fragment_image_exercise_layout_suggested.visibility = View.VISIBLE
//            } ?: run {
//                fragment_image_exercise_layout_suggested.visibility = View.GONE
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
        viewModel.isFavorite.removeObservers(viewLifecycleOwner)
        viewModel.isChatSource.removeObservers(viewLifecycleOwner)
        viewModel.resetAnimation.removeObservers(viewLifecycleOwner)
        viewModel.exercise.removeObservers(viewLifecycleOwner)
        //viewModel.suggestedExercise.removeObservers(viewLifecycleOwner)
    }

    private fun share() {
        val drawableBitmap = fragment_image_exercise_image_view_background.drawable.toBitmap()
        val destinationFile = getTempFilePath()
        try {
            val fileOutputStream = FileOutputStream(destinationFile)
            drawableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: NoSuchFileException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.putExtra(Intent.EXTRA_STREAM, getFileUri(destinationFile))
        shareIntent.putExtra(Intent.EXTRA_TEXT, fragment_image_exercise_text_view_description.text)
        shareIntent.type = "image/jpeg"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun getTempFilePath(): File {
        if (!mediaPath.exists()) {
            mediaPath.mkdirs()
        }
        val fileName = getStringTimestampForMedia() + ".jpg"
        return File(mediaPath, fileName)
    }

    private fun getFileUri(file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                mContext,
                "${BuildConfig.APPLICATION_ID}.provider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }
}