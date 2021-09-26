package com.chatowl.presentation.plan.planlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.repositories.ChatOwlPlanRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.plan.ProgramPreviewAdapter
import com.chatowl.presentation.toolbox.mediaexercise.FullscreenPlayerContract
import kotlinx.android.synthetic.main.fragment_plan_list.*

class PlanListFragment : ViewModelFragment<PlanListViewModel>(PlanListViewModel::class.java) {

    private val planAdapter = ProgramPreviewAdapter { programPreview ->
        viewModel.onProgramClicked(programPreview)
    }

    override fun setViewModel(): PlanListViewModel {
        val application = ChatOwlApplication.get()
        val args: PlanListFragmentArgs by navArgs()
        return ViewModelProvider(this, PlanListViewModel.Factory(application, args, ChatOwlPlanRepository)).get(
            PlanListViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plan_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_plan_list_recycler_view_available_plans.adapter = planAdapter

        fragment_plan_list_button_stay_in_plan.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun addObservers() {
        viewModel.programList.observe(viewLifecycleOwner, { programs ->
            planAdapter.submitList(programs)
        })

        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

    }

    override fun removeObservers() {
        viewModel.programList.removeObservers(viewLifecycleOwner)
        viewModel.navigate.removeObservers(viewLifecycleOwner)
    }

}