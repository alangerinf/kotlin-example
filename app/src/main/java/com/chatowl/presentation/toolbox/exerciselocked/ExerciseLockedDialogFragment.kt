package com.chatowl.presentation.toolbox.exerciselocked

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.main.MainViewModel
import com.chatowl.presentation.plan.CurrentPlanFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_exercise_locked.*


class ExerciseLockedDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: ExerciseLockedDialogViewModel
    private val mainActivityViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_exercise_locked, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application  = ChatOwlApplication.get()
        val args: ExerciseLockedDialogFragmentArgs by navArgs()
        viewModel = ViewModelProvider(this, ExerciseLockedDialogViewModel.Factory(application, args)).get(ExerciseLockedDialogViewModel::class.java)

        viewModel.exerciseLockedMessage.observe(viewLifecycleOwner, { message ->
            dialog_exercise_locked_text_view_title.text = message.title
            dialog_exercise_locked_text_view_body.text = message.body
            dialog_exercise_locked_image_view.setImageDrawable(ContextCompat.getDrawable(requireContext(), message.exerciseIconResourceId))
            dialog_exercise_locked_text_view_name.text = message.exerciseName
        })

        viewModel.action.observe(viewLifecycleOwner, {
            dialog_exercise_locked_button_primary_action.text = it
        })

        viewModel.cancel.observe(viewLifecycleOwner, {
            dialog_exercise_locked_text_view_secondary_action.text = it
        })

        viewModel.dismiss.observe(viewLifecycleOwner, {
            findNavController().navigateUp()
        })

        viewModel.navigate.observe(viewLifecycleOwner, {
            mainActivityViewModel.setNavigation(it)
        })

        dialog_exercise_locked_button_primary_action.setOnClickListener {
            viewModel.onPrimaryActionClicked()
        }

        dialog_exercise_locked_text_view_secondary_action.setOnClickListener {
            viewModel.onSecondaryActionClicked()
        }

        val lastDestination = findNavController().previousBackStackEntry?.destination?.label?:""
        val currentPlanClassName = (CurrentPlanFragment::class.java as Class).simpleName
        if (lastDestination.equals(currentPlanClassName)){
            dialog_exercise_locked_text_view_secondary_action.visibility = View.GONE
        }

    }

    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog {
        val dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener {
            val bottomSheetDialog : BottomSheetDialog? = it as? BottomSheetDialog
            val bottomSheetBehavior = bottomSheetDialog?.behavior
            bottomSheetBehavior?.state = STATE_EXPANDED
            bottomSheetBehavior?.setDraggable(false)
        }
        return dialog
    }
}
