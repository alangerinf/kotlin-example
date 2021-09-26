package com.chatowl.presentation.toolbox.category

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.toolbox.ToolboxItem
import com.chatowl.data.repositories.ToolboxRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_toolbox_category.*

class ToolboxCategoryFragment :
    ViewModelFragment<ToolboxCategoryViewModel>(ToolboxCategoryViewModel::class.java) {

    private lateinit var filterOpen: Animation
    private lateinit var filterClose: Animation

    val args: ToolboxCategoryFragmentArgs by navArgs()

    override fun getScreenName() = args.category.name

    private val availableToolAdapter = ToolboxItemAdapter { tool ->
        viewModel.onToolClicked(tool)
    }

    private val upcomingToolAdapter = ToolboxItemAdapter { tool ->
        viewModel.onToolClicked(tool)
    }

    override fun setViewModel(): ToolboxCategoryViewModel {
        val application = ChatOwlApplication.get()
        val toolboxRepository =
            ToolboxRepository(ChatOwlDatabase.getInstance(application).toolboxDao)
        val args: ToolboxCategoryFragmentArgs by navArgs()
        return ViewModelProvider(
            this,
            ToolboxCategoryViewModel.Factory(application, args, toolboxRepository)
        ).get(ToolboxCategoryViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadAnimations(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_toolbox_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        fragment_toolbox_category_recycler_view_available.adapter = availableToolAdapter
        fragment_toolbox_category_recycler_view_upcoming.adapter = upcomingToolAdapter

        fragment_toolbox_category_edit_text_search_field.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(fragment_toolbox_category_edit_text_search_field)
                true
            } else {
                false
            }
        }

        fragment_toolbox_category_image_button_search.setOnClickListener {
            viewModel.showSearchField()
        }

        fragment_toolbox_category_edit_text_search_field.addTextChangedListener(afterTextChanged = {
            viewModel.onFilterSearchChanged(it.toString())
        })

        fragment_toolbox_category_edit_text_search_field.setOnFocusChangeListener { _, b ->
            if(fragment_toolbox_category_edit_text_search_field.text.toString().isEmpty() && !b) {
                viewModel.hideSearchField()
            }
        }

        fragment_toolbox_category_text_view_filter.setOnClickListener {
            animateFilter()
        }

        fragment_toolbox_category_text_view_filter_media_audio.setOnClickListener {
            viewModel.onFilterAudioClicked()
        }

        fragment_toolbox_category_text_view_filter_media_image.setOnClickListener {
            viewModel.onFilterImageClicked()
        }

        fragment_toolbox_category_text_view_filter_media_quote.setOnClickListener {
            viewModel.onFilterQuoteClicked()
        }

        fragment_toolbox_category_text_view_filter_media_video.setOnClickListener {
            viewModel.onFilterVideoClicked()
        }

        fragment_toolbox_category_text_view_filter_length_short.setOnClickListener {
            viewModel.onFilterShortClicked()
        }

        fragment_toolbox_category_text_view_filter_length_medium.setOnClickListener {
            viewModel.onFilterMediumClicked()
        }

        fragment_toolbox_category_text_view_filter_length_long.setOnClickListener {
            viewModel.onFilterLongClicked()
        }
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        viewModel.header.observe(viewLifecycleOwner, { header ->
            fragment_toolbox_category_toolbar.setTitle(header)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_toolbox_category_linear_progress_indicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.filteredData.observe(viewLifecycleOwner, { toolboxItemList ->
            fragment_toolbox_category_group_available.visibility =
                if (toolboxItemList.first.isEmpty()) View.GONE else View.VISIBLE

            fragment_toolbox_category_group_upcoming.visibility =
                if (toolboxItemList.second.isEmpty()) View.GONE else View.VISIBLE

            fragment_toolbox_category_layout_no_results.visibility =
                if(toolboxItemList.first.isEmpty() && toolboxItemList.second.isEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            availableToolAdapter.submitList(ToolboxItem.deleteDuplicates(toolboxItemList.first))

            upcomingToolAdapter.submitList(ToolboxItem.deleteDuplicates(toolboxItemList.second))
        })

        viewModel.showSearchField.observe(viewLifecycleOwner, { showSearch ->
            if(showSearch) {
                fragment_toolbox_category_layout_search.visibility = View.VISIBLE
                fragment_toolbox_category_image_button_search.visibility = View.GONE
            } else {
                fragment_toolbox_category_layout_search.visibility = View.GONE
                fragment_toolbox_category_image_button_search.visibility = View.VISIBLE
            }
        })

        viewModel.audioFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_category_text_view_filter_media_audio, it)
        })
        viewModel.imageFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_category_text_view_filter_media_image, it)
        })
        viewModel.quoteFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_category_text_view_filter_media_quote, it)
        })
        viewModel.videoFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_category_text_view_filter_media_video, it)
        })
        viewModel.lengthShortFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_category_text_view_filter_length_short, it)
        })
        viewModel.lengthMediumFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_category_text_view_filter_length_medium, it)
        })
        viewModel.lengthLongFilter.observe(viewLifecycleOwner, {
            setFilterSelected(fragment_toolbox_category_text_view_filter_length_long, it)
        })

        viewModel.isAllFiltersEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewModel.isMediaFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_media.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isAudioFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_media_audio.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isImageFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_media_image.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isQuoteFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_media_quote.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isVideoFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_media_video.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isDurationFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_length.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isShortFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_length_short.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isMediumFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_length_medium.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.isLongFilterEnabled.observe(viewLifecycleOwner, {
            fragment_toolbox_category_text_view_filter_length_long.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun setFilterSelected(textView: TextView, isSelected: Boolean) {
        if (isSelected) {
            textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary_20))
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0)
        } else {
            textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary2_08))
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun animateFilter() {
        if (fragment_toolbox_category_layout_filters.visibility == View.VISIBLE) {
            fragment_toolbox_category_layout_filters.startAnimation(filterClose)
        } else {
            fragment_toolbox_category_layout_filters.startAnimation(filterOpen)
        }
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.filteredData.removeObservers(viewLifecycleOwner)
        viewModel.showSearchField.removeObservers(viewLifecycleOwner)
        viewModel.audioFilter.removeObservers(viewLifecycleOwner)
        viewModel.imageFilter.removeObservers(viewLifecycleOwner)
        viewModel.quoteFilter.removeObservers(viewLifecycleOwner)
        viewModel.videoFilter.removeObservers(viewLifecycleOwner)
        viewModel.lengthShortFilter.removeObservers(viewLifecycleOwner)
        viewModel.lengthMediumFilter.removeObservers(viewLifecycleOwner)
        viewModel.lengthLongFilter.removeObservers(viewLifecycleOwner)
    }

    private fun loadAnimations(context: Context) {
        filterOpen = AnimationUtils.loadAnimation(context, R.anim.scale_open)
        filterOpen.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                fragment_toolbox_category_layout_filters.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(p0: Animation?) {}

            override fun onAnimationRepeat(p0: Animation?) {}
        })

        filterClose = AnimationUtils.loadAnimation(context, R.anim.scale_close)
        filterClose.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}

            override fun onAnimationEnd(p0: Animation?) {
                fragment_toolbox_category_layout_filters.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {}
        })
    }

}