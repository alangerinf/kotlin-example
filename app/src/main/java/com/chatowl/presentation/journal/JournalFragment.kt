package com.chatowl.presentation.journal

import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.entities.tracking.PostEventTrackingRequest
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.data.repositories.JournalRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.Animations
import com.chatowl.presentation.main.MainViewModel
import com.chatowl.presentation.toolbox.host.ViewPagerFragmentInteraction
import com.google.android.material.internal.TextWatcherAdapter
import kotlinx.android.synthetic.main.fragment_journal_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


@ExperimentalStdlibApi
class JournalFragment(private val fragmentInteractionListener: ViewPagerFragmentInteraction) : ViewModelFragment<JournalViewModel>(JournalViewModel::class.java) {

    private lateinit var pulseAnimatorSet: AnimatorSet

    private val entriesAdapter = JournalAdapter({ entry ->
        viewModel.onJournalClicked(entry)
    }) { entry ->
        viewModel.onJournalTryToDelete(entry, requireActivity()) { refreshAdapter() }
    }

    override fun getScreenName() = "Entry list"

    private fun refreshAdapter()  {entriesAdapter.notifyDataSetChanged()}

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    override fun setViewModel(): JournalViewModel {
        val application = ChatOwlApplication.get()
        val journalDao = ChatOwlDatabase.getInstance(application).journalDao
        val journalRepository = JournalRepository(journalDao)
        return ViewModelProvider(
            this,
            JournalViewModel.Factory(application, journalRepository)
        ).get(
            JournalViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_journal_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pulseAnimatorSet = Animations.getPulseAnimation(fragment_journal_home_view_pulse_small, fragment_journal_home_view_pulse_large)

        entriesAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                fragment_journal_home_recycler_view.scrollToPosition(positionStart)
            }
        })
        fragment_journal_home_recycler_view.adapter = entriesAdapter
        fragment_journal_home_recycler_view.addOnScrollListener(recyclerScrollListener)

        fragment_journal_home_edit_text_search_field.addTextChangedListener(object :
            TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onSearchFieldChanged(s.toString())
            }
        })

        fragment_journal_home_edit_text_search_field.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(fragment_journal_home_edit_text_search_field)
                true
            } else {
                false
            }
        }

        fragment_journal_home_fab_add.setOnClickListener {
            viewModel.onCreateNewEntryClicked()
        }

        val touchHelperCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                val binIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_trash_bin, null)

                val ICON_HORIZONTAL_MARGIN = 38F

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                    return .16f
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    entriesAdapter.onItemTryToDeleteDeleted(viewHolder.adapterPosition)
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )

                    // TODO: Adjust width with width itself and not with height as it is now
                    val iconHeight = binIcon?.intrinsicHeight
                    val iconWidth = binIcon?.intrinsicWidth
                    val halfIconHeight = iconHeight?.div(2)
                    val halfIconWidth = iconWidth?.div(2)

                    val iconHorizontalMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        ICON_HORIZONTAL_MARGIN,
                        fragment_journal_home_recycler_view.context.resources.displayMetrics
                    )
                    val top =
                        viewHolder.itemView.top + ((viewHolder.itemView.bottom - viewHolder.itemView.top) / 2 - halfIconHeight!!)
                    val imgLeft =
                        viewHolder.itemView.right - iconHorizontalMargin - halfIconWidth!! * 2

                    if (dX < 0) {
                        binIcon?.setBounds(
                            (imgLeft).toInt(),
                            top,
                            viewHolder.itemView.right - iconHorizontalMargin.toInt(),
                            top + binIcon.intrinsicHeight
                        )
                    } else {
                        binIcon?.setBounds(0, 0, 0, 0)
                    }
                    binIcon?.draw(c)
                }
            }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(fragment_journal_home_recycler_view)
    }

    private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (!recyclerView.canScrollVertically(1)) {
                viewModel.onScrollEnded()
            }
        }
    }

    override fun addObservers() {

        mainActivityViewModel.isNetworkAvailable.observe(viewLifecycleOwner, {
            if (it) viewModel.syncWithClient()
        })

        viewModel.navigate.observe(viewLifecycleOwner, {
            fragmentInteractionListener.onPageFragmentAction(it)
            //findNavController().navigate(it)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_journal_home_linear_progress_indicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.isPageLoading.observe(viewLifecycleOwner, { isPageLoading ->
            fragment_journal_home_linear_progress_indicator.visibility =
                if (isPageLoading) View.VISIBLE else View.GONE
        })

        viewModel.isSearching.observe(viewLifecycleOwner, { isPageLoading ->
            fragment_journal_home_linear_progress_indicator.visibility =
                if (isPageLoading) View.VISIBLE else View.GONE
        })

        viewModel.adapterItems.observe(viewLifecycleOwner, { itemList ->
            if (itemList.isEmpty() && fragment_journal_home_edit_text_search_field.text.toString()
                    .isEmpty()
            ) {
                fragment_journal_text_view_no_entries.visibility = View.VISIBLE
                fragment_journal_home_layout_content.visibility = View.GONE
                pulseAnimatorSet.start()
            } else {
                fragment_journal_text_view_no_entries.visibility = View.GONE
                fragment_journal_home_layout_content.visibility = View.VISIBLE
                entriesAdapter.submitList(itemList)
                pulseAnimatorSet.cancel()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchJournalEntries()
    }

    override fun onDestroyView() {
        pulseAnimatorSet.cancel()
        super.onDestroyView()
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.adapterItems.removeObservers(viewLifecycleOwner)
    }

}