package com.chatowl.presentation.messages.viewmessage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.MessagesRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.ABBREVIATED_MONTH_DAY_YEAR_HOUR_MINUTES_AM_PM
import com.chatowl.presentation.common.utils.SERVER_FORMAT
import com.chatowl.presentation.common.utils.changeDateFormat
import com.chatowl.presentation.journal.entryitem.EntryItemAdapter
import kotlinx.android.synthetic.main.fragment_message_view.*


class ViewMessageFragment :
    ViewModelFragment<ViewMessageViewModel>(ViewMessageViewModel::class.java) {

    private val adapter = EntryItemAdapter(this,{}, {}, {}, { _, _, _ -> }, false)

    override fun setViewModel(): ViewMessageViewModel {
        val application = ChatOwlApplication.get()
        val messageDao = ChatOwlDatabase.getInstance(application).messageDao
        val messagesRepository = MessagesRepository(messageDao)
        val args: ViewMessageFragmentArgs by navArgs()
        return ViewModelProvider(
            this,
            ViewMessageViewModel.Factory(application, args, messagesRepository)
        )
            .get(ViewMessageViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_message_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_view_message_recycler_view.adapter = adapter
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.message.observe(viewLifecycleOwner, { message ->
            // header
            row_message_preview_text_view_user_message_info.text = getString(
                R.string.format_string_long_space_string,
                changeDateFormat(
                    message.date,
                    SERVER_FORMAT,
                    ABBREVIATED_MONTH_DAY_YEAR_HOUR_MINUTES_AM_PM
                ),
                getString(R.string.me)
            )
            // reply
            message.reply?.let {
                fragment_message_view_text_view_reply_body.text = it.answer
                fragment_message_view_text_view_reply_info.text = getString(
                    R.string.format_string_long_space_string,
                    changeDateFormat(
                        it.date,
                        SERVER_FORMAT,
                        ABBREVIATED_MONTH_DAY_YEAR_HOUR_MINUTES_AM_PM
                    ),
                    getString(R.string.therapy_team)
                )
                fragment_message_view_layout_reply.visibility = View.VISIBLE
                fragment_view_message_text_view_reply_pending.visibility = View.GONE
            } ?: run {
                fragment_message_view_layout_reply.visibility = View.GONE
                fragment_view_message_text_view_reply_pending.visibility = View.VISIBLE
            }
        })
        viewModel.components.observe(viewLifecycleOwner, { components ->
            adapter.submitList(components)
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.message.removeObservers(viewLifecycleOwner)
        viewModel.components.removeObservers(viewLifecycleOwner)
    }

}