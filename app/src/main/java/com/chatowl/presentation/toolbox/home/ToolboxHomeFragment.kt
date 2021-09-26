package com.chatowl.presentation.toolbox.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.tabs.TabFragment
import com.chatowl.presentation.tabs.TabViewModel
import com.chatowl.presentation.toolbox.host.ViewPagerFragmentInteraction
import kotlinx.android.synthetic.main.fragment_toolbox_category.*
import kotlinx.android.synthetic.main.fragment_toolbox_home.*

class ToolboxHomeFragment(private val fragmentInteractionListener: ViewPagerFragmentInteraction) :
    ViewModelFragment<ToolboxHomeViewModel>(ToolboxHomeViewModel::class.java) {

    private lateinit var tabViewModel: TabViewModel
    private lateinit var filterOpen: Animation
    private lateinit var filterClose: Animation

    private val groupAdapter = ToolboxGroupAdapter({ group ->
        viewModel.onGroupClicked(group)
    }, { group, groupItem ->
        viewModel.onGroupItemClicked(group, groupItem)
    })

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadAnimations(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tabFragment = requireParentFragment().requireParentFragment().requireParentFragment() as TabFragment
        tabViewModel = ViewModelProvider(tabFragment).get(TabViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_toolbox_home, container, false)
    }

    override fun setViewModel(): ToolboxHomeViewModel {
        val application = ChatOwlApplication.get()
        val repository = ToolboxRepository(ChatOwlDatabase.getInstance(application).toolboxDao)
        return ViewModelProvider(this, ToolboxHomeViewModel.Factory(application, repository)).get(
            ToolboxHomeViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        //TODO this param is to redirect to the proper tool
        //val toolId = tabViewModel.getNavigationParam()

        fragment_toolbox_recycler_view.adapter = groupAdapter

        fragment_toolbox_home_text_view_filter.setOnClickListener {
            animateFilter()
        }

        fragment_toolbox_home_image_button_search.setOnClickListener {
            viewModel.showSearchField()
        }

        fragment_toolbox_home_edit_text_search_field.addTextChangedListener(afterTextChanged = {
            viewModel.onFilterSearchChanged(it.toString())
        })

        fragment_toolbox_home_edit_text_search_field.setOnFocusChangeListener { _, b ->
            if (fragment_toolbox_home_edit_text_search_field.text.toString().isEmpty() && !b) {
                viewModel.hideSearchField()
            }
        }

        fragment_toolbox_home_text_view_filter_media_audio.setOnClickListener {
            viewModel.onFilterAudioClicked()
        }

        fragment_toolbox_home_text_view_filter_media_image.setOnClickListener {
            viewModel.onFilterImageClicked()
        }

        fragment_toolbox_home_text_view_filter_media_quote.setOnClickListener {
            viewModel.onFilterQuoteClicked()
        }

        fragment_toolbox_home_text_view_filter_media_video.setOnClickListener {
            viewModel.onFilterVideoClicked()
        }

        fragment_toolbox_home_text_view_filter_length_short.setOnClickListener {
            viewModel.onFilterShortClicked()
        }

        fragment_toolbox_home_text_view_filter_length_medium.setOnClickListener {
            viewModel.onFilterMediumClicked()
        }

        fragment_toolbox_home_text_view_filter_length_long.setOnClickListener {
            viewModel.onFilterLongClicked()
        }

        viewModel.isAllFiltersEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewModel.isMediaFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_media.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isAudioFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_media_audio.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isImageFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_media_image.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isQuoteFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_media_quote.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isVideoFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_media_video.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isDurationFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_length.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isShortFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_length_short.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isMediumFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_length_medium.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isLongFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_home_text_view_filter_length_long.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            fragmentInteractionListener.onPageFragmentAction(it)
            //findNavController().navigate(it)
        })
        viewModel.isLoadingPlanOrCategory.observe(viewLifecycleOwner, { isLoadingPlanOrCategory ->
            fragment_toolbox_home_linear_progress_indicator.visibility =
                if (isLoadingPlanOrCategory) View.VISIBLE else View.GONE
        })
        viewModel.isLoadingAndEmpty.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                fragment_toolbox_home_progress_bar.visibility = View.VISIBLE
                fragment_toolbox_scrollview.visibility = View.GONE
            } else {
                fragment_toolbox_home_progress_bar.visibility = View.GONE
                fragment_toolbox_scrollview.visibility = View.VISIBLE
            }
        })
        viewModel.filteredGroups.observe(viewLifecycleOwner, { groups ->
            fragment_toolbox_home_layout_no_results.visibility =
                if (groups.isEmpty()) View.VISIBLE else View.GONE
            groupAdapter.submitList(groups)
        })
        viewModel.showSearchField.observe(viewLifecycleOwner, { showSearch ->
            if (showSearch) {
                fragment_toolbox_home_layout_search.visibility = View.VISIBLE
                fragment_toolbox_home_image_button_search.visibility = View.GONE
            } else {
                fragment_toolbox_home_layout_search.visibility = View.GONE
                fragment_toolbox_home_image_button_search.visibility = View.VISIBLE
            }
        })

        viewModel.audioFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_home_text_view_filter_media_audio, it)
        })
        viewModel.imageFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_home_text_view_filter_media_image, it)
        })
        viewModel.quoteFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_home_text_view_filter_media_quote, it)
        })
        viewModel.videoFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_home_text_view_filter_media_video, it)
        })
        viewModel.lengthShortFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_home_text_view_filter_length_short, it)
        })
        viewModel.lengthMediumFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_home_text_view_filter_length_medium, it)
        })
        viewModel.lengthLongFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_home_text_view_filter_length_long, it)
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoadingPlanOrCategory.removeObservers(viewLifecycleOwner)
        viewModel.isLoadingAndEmpty.removeObservers(viewLifecycleOwner)
        viewModel.filteredGroups.removeObservers(viewLifecycleOwner)
        viewModel.showSearchField.removeObservers(viewLifecycleOwner)
        viewModel.audioFilter.removeObservers(viewLifecycleOwner)
        viewModel.imageFilter.removeObservers(viewLifecycleOwner)
        viewModel.quoteFilter.removeObservers(viewLifecycleOwner)
        viewModel.videoFilter.removeObservers(viewLifecycleOwner)
        viewModel.lengthShortFilter.removeObservers(viewLifecycleOwner)
        viewModel.lengthMediumFilter.removeObservers(viewLifecycleOwner)
        viewModel.lengthLongFilter.removeObservers(viewLifecycleOwner)
    }

    private fun setFilterSelected(textView: TextView, isSelected: Boolean) {
        if (isSelected) {
            textView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary_20
                )
            )
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0)
        } else {
            textView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary2_08
                )
            )
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun animateFilter() {
        if (fragment_toolbox_home_layout_filters.visibility == View.VISIBLE) {
            fragment_toolbox_home_layout_filters.startAnimation(filterClose)
        } else {
            fragment_toolbox_home_layout_filters.startAnimation(filterOpen)
        }
    }

    private fun loadAnimations(context: Context) {
        filterOpen = AnimationUtils.loadAnimation(context, R.anim.scale_open)
        filterOpen.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                fragment_toolbox_home_layout_filters.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(p0: Animation?) {}

            override fun onAnimationRepeat(p0: Animation?) {}
        })

        filterClose = AnimationUtils.loadAnimation(context, R.anim.scale_close)
        filterClose.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}

            override fun onAnimationEnd(p0: Animation?) {
                fragment_toolbox_home_layout_filters.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {}
        })
    }
}