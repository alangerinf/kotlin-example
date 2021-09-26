package com.chatowl.presentation.plan.description

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.chatowl.R
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.toolbox.mediaexercise.FullscreenPlayerContract
import kotlinx.android.synthetic.main.fragment_plan_description.*

class PlanDescriptionFragment : ViewModelFragment<PlanDescriptionViewModel>(PlanDescriptionViewModel::class.java) {

    private val fullscreenPlayerActivity =
        registerForActivityResult(FullscreenPlayerContract()) { }

    override fun getScreenName() = "Plan description"


    val args : PlanDescriptionFragmentArgs by navArgs()

    override fun getTherapyPlanName() = args.programDescription.name

    override fun setViewModel(): PlanDescriptionViewModel {
        val application = ChatOwlApplication.get()
        val args: PlanDescriptionFragmentArgs by navArgs()
        return ViewModelProvider(this, PlanDescriptionViewModel.Factory(application, args)).get(
            PlanDescriptionViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plan_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_plan_description_scrollview.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        fragment_plan_description_card_view.setOnClickListener {
            viewModel.onPlayClicked()
        }
    }

    override fun addObservers() {
        viewModel.programDescription.observe(viewLifecycleOwner, { description ->
            fragment_plan_description_text_view_title.text = "${description.name}:"
            fragment_plan_description_text_view_subtitle.text = description.tagLine
            fragment_plan_description_paragraph_header.text = description.intro
            fragment_plan_description_paragraph_header.text = description.description
            Glide.with(fragment_plan_description_image_view_background)
                .load(description.thumbnailUrl)
                .into(fragment_plan_description_image_view_background)
        })
        viewModel.fullscreenPlayer.observe(viewLifecycleOwner, { playerData ->
            fullscreenPlayerActivity.launch(playerData)
        })
    }

    override fun removeObservers() {
        viewModel.programDescription.removeObservers(viewLifecycleOwner)
        viewModel.fullscreenPlayer.removeObservers(viewLifecycleOwner)
    }

}