package com.chatowl.presentation.plan

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.api.chat.TOUR_CHAT_CHANGE_THERAPY_FLOW_ID
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_plan_version_change.*

class PlanVersionChangeDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModelPlan: PlanVersionChangeViewModel
    private lateinit var currentPlanViewModel: CurrentPlanViewModel// by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_plan_version_change, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application = ChatOwlApplication.get()
        val args: PlanVersionChangeDialogFragmentArgs by navArgs()

        viewModelPlan = ViewModelProvider(
            this,
            PlanVersionChangeViewModel.Factory(application, ChatOwlPlanRepository, args)
        ).get(
            PlanVersionChangeViewModel::class.java
        )

        val homeDao = ChatOwlDatabase.getInstance(application).homeDao
        val activitiesRepository = ChatOwlHomeRepository(homeDao)
        currentPlanViewModel = ViewModelProvider(
            requireActivity(),
            CurrentPlanViewModel.Factory(application, ChatOwlPlanRepository, activitiesRepository)
        ).get(
            CurrentPlanViewModel::class.java
        )

        dialog_plan_version_change_button_change_version.setOnClickListener {
            viewModelPlan.changeVersion()
        }


        dialog_plan_version_change_button_change_therapy.setOnClickListener {
            currentPlanViewModel.changePlanSelectedFromDialog(
                Integer(
                    TOUR_CHAT_CHANGE_THERAPY_FLOW_ID
                )
            )
            findNavController().navigateUp()
        }

        dialog_plan_version_change_text_view_cancel.setOnClickListener {
            findNavController().navigateUp()
        }

        addObservers()
    }

    fun addObservers() {
        viewModelPlan.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        viewModelPlan.versionName.observe(viewLifecycleOwner, {
            getString(R.string.format_change_to, it)
            getString(R.string.format_try_version, it)
        })

        viewModelPlan.messageBodyResourceId.observe(viewLifecycleOwner, {
            dialog_plan_version_change_text_view_body.text = getString(it)
        })

        viewModelPlan.buttonTextResourceId.observe(viewLifecycleOwner, {
            dialog_plan_version_change_button_change_version.text = getString(it)
        })

        viewModelPlan.isLoading.observe(viewLifecycleOwner, { isLoading ->
            dialog_plan_version_change_circular_progress_indicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModelPlan.navigate.removeObservers(viewLifecycleOwner)
        viewModelPlan.versionName.removeObservers(viewLifecycleOwner)
        viewModelPlan.messageBodyResourceId.removeObservers(viewLifecycleOwner)
        viewModelPlan.buttonTextResourceId.removeObservers(viewLifecycleOwner)
        viewModelPlan.isLoading.removeObservers(viewLifecycleOwner)
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
}
