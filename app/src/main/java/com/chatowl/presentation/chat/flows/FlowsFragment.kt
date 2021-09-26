package com.chatowl.presentation.chat.flows

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.chat.flows.Flow
import com.chatowl.data.repositories.ChatOwlChatTestRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.tabs.TabFragment
import com.chatowl.presentation.tabs.TabViewModel
import kotlinx.android.synthetic.main.fragment_flows.*
import kotlinx.android.synthetic.main.row_flows.view.*


class FlowsFragment : ViewModelFragment<FlowsViewModel>(FlowsViewModel::class.java) {

    private lateinit var tabViewModel: TabViewModel

    override fun getScreenName() = "Flows"

    private val adapter = FlowAdapter { item ->
        closeKeyboard(fragment_flows_edit_text_search)
        tabViewModel.navigateToChatWithSessionId(item.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tabFragment = requireParentFragment().requireParentFragment() as TabFragment
        tabViewModel = ViewModelProvider(tabFragment).get(TabViewModel::class.java)
    }

    override fun setViewModel(): FlowsViewModel {
        val application = ChatOwlApplication.get()
        return ViewModelProvider(this, FlowsViewModel.Factory(application, ChatOwlChatTestRepository)).get(
            FlowsViewModel::class.java
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_flows, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_flows_recycler_view.adapter = adapter

        fragment_flows_edit_text_search.addTextChangedListener(afterTextChanged = {
            viewModel.onFilterSearchChanged(it.toString())
        })
    }

    override fun addObservers() {
        viewModel.filteredValues.observe(viewLifecycleOwner, { items ->
            adapter.submitList(items)
        })
    }

    override fun removeObservers() {
        viewModel.filteredValues.removeObservers(viewLifecycleOwner)
    }

    class FlowAdapter(private val itemClick: (item: Flow) -> Unit) :
        ListAdapter<Flow, FlowAdapter.FlowViewHolder>(DIFF_CALLBACK) {

        companion object {

            private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Flow>() {
                override fun areItemsTheSame(
                    oldItem: Flow,
                    newItem: Flow
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: Flow,
                    newItem: Flow
                ): Boolean {
                    return oldItem == newItem
                }
            }

        }

        private val itemClickedAt: (Int) -> Unit = { position ->
            itemClick.invoke(getItem(position))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlowViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.row_flows, parent, false)
            return FlowViewHolder(view, itemClickedAt)
        }

        override fun onBindViewHolder(holder: FlowViewHolder, position: Int) {
            val item = getItem(position)
            holder.bind(item)
        }

        inner class FlowViewHolder(
            itemView: View,
            private val itemClick: (position: Int) -> Unit
        ) :
            RecyclerView.ViewHolder(itemView) {

            init {
                itemView.setOnClickListener {
                    itemClick.invoke(adapterPosition)
                }
            }

            fun bind(item: Flow) {
                itemView.row_flows_text_view_name.text = item.name
            }
        }

    }
}