package com.chatowl.presentation.chat.values

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.chat.values.PropertyValue
import com.chatowl.data.entities.chat.values.PropertyValueType
import com.chatowl.data.repositories.ChatOwlChatTestRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_values.*
import kotlinx.android.synthetic.main.row_values.view.*


class ValuesFragment : ViewModelFragment<ValuesViewModel>(ValuesViewModel::class.java) {

    private val adapter = PropertyValueAdapter { item ->
        viewModel.onItemClick(item)
    }

    override fun setViewModel(): ValuesViewModel {
        val application = ChatOwlApplication.get()
        return ViewModelProvider(this, ValuesViewModel.Factory(application, ChatOwlChatTestRepository)).get(
            ValuesViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_values, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        fragment_values_recycler_view.adapter = adapter

        fragment_values_edit_text_search.addTextChangedListener(afterTextChanged = {
            viewModel.onFilterSearchChanged(it.toString())
        })
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.filteredValues.observe(viewLifecycleOwner, { items ->
            adapter.submitList(items)
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.filteredValues.removeObservers(viewLifecycleOwner)
    }

    class PropertyValueAdapter(private val itemClick: (item: PropertyValue) -> Unit) :
        ListAdapter<PropertyValue, PropertyValueAdapter.PropertyValueViewHolder>(DIFF_CALLBACK) {

        companion object {

            private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PropertyValue>() {
                override fun areItemsTheSame(
                    oldItem: PropertyValue,
                    newItem: PropertyValue
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: PropertyValue,
                    newItem: PropertyValue
                ): Boolean {
                    return oldItem == newItem
                }
            }

        }

        private val itemClickedAt: (Int) -> Unit = { position ->
            itemClick.invoke(getItem(position))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyValueViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.row_values, parent, false)
            return PropertyValueViewHolder(view, itemClickedAt)
        }

        override fun onBindViewHolder(holder: PropertyValueViewHolder, position: Int) {
            val item = getItem(position)
            holder.bind(item)
        }

        inner class PropertyValueViewHolder(
            itemView: View,
            private val itemClick: (position: Int) -> Unit
        ) :
            RecyclerView.ViewHolder(itemView) {

            init {
                itemView.setOnClickListener {
                    itemClick.invoke(adapterPosition)
                }
            }

            fun bind(item: PropertyValue) {
                itemView.row_values_text_view_name.text = item.name
                itemView.row_values_text_view_type.text = itemView.context.getString(
                    PropertyValueType.getDescriptionString(
                        item.type ?: ""
                    ) ?: R.string.error
                )
                itemView.row_values_text_view_value.text =
                    if (item.type == PropertyValueType.STRING.apiReference) {
                        "\"${item.value}\""
                    } else {
                        item.value
                    }
            }
        }

    }
}