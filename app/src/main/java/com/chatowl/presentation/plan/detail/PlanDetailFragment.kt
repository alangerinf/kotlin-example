package com.chatowl.presentation.plan.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.entities.plan.ProgramVersion
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.widgets.toggle.PlanVersionsToggleView
import com.chatowl.presentation.plan.ProgramWeekAdapter
import kotlinx.android.synthetic.main.activity_mood_meter_picker.view.*
import kotlinx.android.synthetic.main.fragment_plan_detail.*


class PlanDetailFragment : ViewModelFragment<PlanDetailViewModel>(PlanDetailViewModel::class.java),
    PlanVersionsToggleView.OnSubscriptionTabSelectedListener {

    val args: PlanDetailFragmentArgs by navArgs()

    private val programWeekAdapter = ProgramWeekAdapter(false) { }

    override fun getScreenName() = "Plan detail"
    override fun getTherapyPlanName() = args.programDetail.name

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plan_detail, container, false)
    }

    override fun setViewModel(): PlanDetailViewModel {
        val application = ChatOwlApplication.get()
        val args: PlanDetailFragmentArgs by navArgs()
        return ViewModelProvider(this, PlanDetailViewModel.Factory(application, args)).get(
            PlanDetailViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_plan_detail_recycler_view.adapter = programWeekAdapter

        fragment_plan_detail_toggle_view.addOnTabSelectedListener(this)

        fragment_plan_detail_text_view_read_more.setOnClickListener {
            viewModel.onReadMoreClicked()
        }

        fragment_plan_change_to_plan_button.setOnClickListener {
            viewModel.changeToPlanClicked()
        }
    }

    override fun addObservers() {
        viewModel.programDetail.observe(viewLifecycleOwner, {
            fragment_plan_detail_text_view_name.text = "${it.name}: ${it.tagline}"
        })
        viewModel.assessmentDone.observe(viewLifecycleOwner, {
            when {
                !it -> {
                    fragment_plan_change_to_plan_button.text =
                        requireActivity().getText(R.string.start_assessment)
                }
                else ->{
                    fragment_plan_change_to_plan_button.text =
                        requireActivity().getText(R.string.change_to_this_plan)
                }
            }
        })
        viewModel.programWeeks.observe(viewLifecycleOwner, { programWeeks ->
            programWeekAdapter.submitList(programWeeks)
        })
        viewModel.programVersion.observe(viewLifecycleOwner, {
            if (fragment_plan_detail_toggle_view.getCurrentTab() != it) {
                fragment_plan_detail_toggle_view.callClick(it)
            }
        })
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.deepNavigation.observe(viewLifecycleOwner, { stringUri ->
            activity?.findNavController(R.id.main_nav_host_fragment)?.navigate(stringUri.toUri())
        })


    }

    override fun removeObservers() {
        viewModel.programDetail.removeObservers(viewLifecycleOwner)
        viewModel.programWeeks.removeObservers(viewLifecycleOwner)
        viewModel.programVersion.removeObservers(viewLifecycleOwner)
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.assessmentDone.removeObservers(viewLifecycleOwner)
        viewModel.deepNavigation.removeObservers(viewLifecycleOwner)
    }

    override fun onSubscriptionTabSelected(tab: ProgramVersion) {
        viewModel.setVersionType(tab)
    }

}