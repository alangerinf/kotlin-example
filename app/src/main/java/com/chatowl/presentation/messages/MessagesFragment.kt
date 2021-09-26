package com.chatowl.presentation.messages

import android.animation.AnimatorSet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.MessagesRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.Animations
import com.chatowl.presentation.toolbox.host.ViewPagerFragmentInteraction
import kotlinx.android.synthetic.main.fragment_messages.*


class MessagesFragment(private val fragmentInteractionListener: ViewPagerFragmentInteraction) : ViewModelFragment<MessagesViewModel>(MessagesViewModel::class.java) {

    private lateinit var pulseAnimatorSet: AnimatorSet

    private val adapter = MessagePreviewAdapter {
        viewModel.onMessageClicked(it)
    }

    override fun setViewModel(): MessagesViewModel {
        val application = ChatOwlApplication.get()
        val messageDao = ChatOwlDatabase.getInstance(application).messageDao
        val messagesRepository = MessagesRepository(messageDao)
        return ViewModelProvider(this, MessagesViewModel.Factory(application, messagesRepository))
            .get(MessagesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        pulseAnimatorSet = Animations.getPulseAnimation(fragment_messages_view_pulse_small, fragment_messages_view_pulse_large)

        adapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                fragment_messages_recycler_view.scrollToPosition(positionStart)
            }
        })

        fragment_messages_recycler_view.adapter = adapter
        fragment_messages_recycler_view.addOnScrollListener(recyclerScrollListener)

        fragment_messages_fab_add.setOnClickListener {
            viewModel.onCreateMessageClicked()
        }
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
        viewModel.navigate.observe(viewLifecycleOwner, {
            fragmentInteractionListener.onPageFragmentAction(it)
        })

        viewModel.adapterItems.observe(viewLifecycleOwner, { messages ->
            if (messages.isEmpty()) {
                fragment_messages_layout_empty.visibility = View.VISIBLE
                fragment_messages_recycler_view.visibility = View.GONE
                pulseAnimatorSet.start()
            } else {
                adapter.submitList(messages)
                fragment_messages_layout_empty.visibility = View.GONE
                fragment_messages_recycler_view.visibility = View.VISIBLE
                pulseAnimatorSet.cancel()
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_messages_linear_progress_indicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        })
    }

    override fun onDestroyView() {
        pulseAnimatorSet.cancel()
        super.onDestroyView()
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.adapterItems.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
    }

}