package com.chatowl.presentation.plan.detail

import android.app.Dialog
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.chatowl.R
import com.chatowl.data.entities.plan.ProgramPreview
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_plan_change.*


class ProgramChangeDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModelPlan: ProgramChangeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_plan_change, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application = ChatOwlApplication.get()
        val args: ProgramChangeDialogFragmentArgs by navArgs()

        viewModelPlan = ViewModelProvider(
            this,
            ProgramChangeViewModel.Factory(application, ChatOwlPlanRepository, args)
        ).get(
            ProgramChangeViewModel::class.java
        )

        viewModelPlan.planPreview.observe(viewLifecycleOwner, {
            programPreviewLoaded(it)
        })

        dialog_plan_change_button_change_plan.setOnClickListener {
            viewModelPlan.changePlanConfirmed()
        }

        dialog_plan_change_text_view_cancel.setOnClickListener {
            findNavController().navigateUp()
        }

        val thumbnailUrl = args.planPreview.thumbnailUrl
        val placeholder = requireContext().getDrawable(R.drawable.ic_hour_glass_rotate)
        (placeholder as Animatable).start()
        Glide.with(requireActivity())
            .load(thumbnailUrl)
            .placeholder(placeholder)
            .into(row_plan_preview_image_view)

        addObservers()
    }

    private fun programPreviewLoaded(programPreview: ProgramPreview){
        row_plan_preview_text_view_tagline.text = programPreview.tagline
        row_plan_preview_text_view_title.text = programPreview.name
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener {
            val bottomSheetDialog: BottomSheetDialog? = it as? BottomSheetDialog
            val bottomSheetBehavior = bottomSheetDialog?.behavior
            bottomSheetBehavior?.state = STATE_EXPANDED
            bottomSheetBehavior?.setDraggable(false)
        }
        return dialog
    }

    fun addObservers() {
        viewModelPlan.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        viewModelPlan.planChanged.observe(viewLifecycleOwner, {
            if (it){
                while(findNavController().navigateUp()){}
            }
        })

        viewModelPlan.versionName.observe(viewLifecycleOwner, {
            row_plan_review_text_view_version.text = it
        })

    }

    fun removeObservers() {
        viewModelPlan.navigate.removeObservers(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }

}
