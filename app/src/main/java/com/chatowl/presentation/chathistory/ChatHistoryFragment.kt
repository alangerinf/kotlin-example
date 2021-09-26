package com.chatowl.presentation.chathistory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.repositories.ChatOwlChatRepository
import com.chatowl.presentation.chat.ChatItemAdapter
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_chat_history.*

class ChatHistoryFragment :
    ViewModelFragment<ChatHistoryViewModel>(ChatHistoryViewModel::class.java) {

    companion object {
        private val answerViewsIds = listOf(
            R.id.fragment_chat_layout_chat_answer_text,
            R.id.fragment_chat_layout_chat_answer_image,
            R.id.fragment_chat_layout_chat_answer_slider_ungraduated
        )
    }

    private val TAG = "ChatHistoryFragment"

    private lateinit var chatItemAdapter: ChatItemAdapter

    override fun setViewModel(): ChatHistoryViewModel {
        val application = ChatOwlApplication.get()
        val chatRepository = ChatOwlChatRepository()
        val args: ChatHistoryFragmentArgs by navArgs()
        return ViewModelProvider(
            this,
            ChatHistoryViewModel.Factory(application, args, chatRepository)
        ).get(
            ChatHistoryViewModel::class.java
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        chatItemAdapter = ChatItemAdapter(context) { item ->
            Log.d(TAG, "Item Clicked")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        fragment_chat_history_recycler_view.adapter = chatItemAdapter

        viewModel.onViewCreated()
    }

    override fun addObservers() {
        viewModel.chatAdapterItems.observe(viewLifecycleOwner, Observer { messages ->
            if (messages.isNotEmpty()) {
                chatItemAdapter.submitList(messages)
            } else {
                //TODO review this, we have a "endChat" live data to endit, this intercepted end
                //generates a missbehaivour
                //parentFragmentManager.popBackStack()
            }
            Log.d(TAG, messages.toString())
        })

        viewModel.title.observe(viewLifecycleOwner, {
            fragment_chat_history_toolbar.setTitle(it)
        })
    }

    override fun removeObservers() {
        viewModel.chatAdapterItems.removeObservers(viewLifecycleOwner)
        viewModel.title.removeObservers(viewLifecycleOwner)
    }

}