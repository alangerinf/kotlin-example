package com.chatowl.presentation.settings.crisissupport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.data.entities.crisissupport.CrisisSupportItem
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_crisis_support.*
import kotlinx.android.synthetic.main.row_crisis_support.view.*
import java.lang.Exception

class CrisisSupportFragment :
    ViewModelFragment<CrisisSupportViewModel>(CrisisSupportViewModel::class.java) {

    override fun getScreenName() = "Crisis Support"

    private val adapter = CrisisSupportAdapter { item ->
        viewModel.onItemClick(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crisis_support, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_crisis_support_recycler_view.adapter = adapter
    }

    override fun addObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_crisis_support_progress_bar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            fragment_crisis_support_recycler_view.visibility =
                if (isLoading) View.INVISIBLE else View.VISIBLE
        })
        viewModel.crisisSupportItemList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
        viewModel.errorMessage.observe(viewLifecycleOwner, { messageStringResourceId ->
            showSnackBarMessage(getString(messageStringResourceId))
        })
        viewModel.callIntent.observe(viewLifecycleOwner, {
            try {
                startActivity(it)
            } catch (e: Exception) {
                e.printStackTrace()
                showSnackBarMessage(getString(R.string.error_making_call))
            }
        })
    }

    override fun removeObservers() {
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.crisisSupportItemList.removeObservers(viewLifecycleOwner)
        viewModel.errorMessage.removeObservers(viewLifecycleOwner)
        viewModel.callIntent.removeObservers(viewLifecycleOwner)
    }

    class CrisisSupportAdapter(private val itemClick: (item: CrisisSupportItem) -> Unit) :
        ListAdapter<CrisisSupportItem, CrisisSupportAdapter.CrisisSupportViewHolder>(DIFF_CALLBACK) {

        companion object {

            private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CrisisSupportItem>() {
                override fun areItemsTheSame(
                    oldItem: CrisisSupportItem,
                    newItem: CrisisSupportItem
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: CrisisSupportItem,
                    newItem: CrisisSupportItem
                ): Boolean {
                    return oldItem == newItem
                }
            }

        }

        private val itemClickedAt: (Int) -> Unit = { position ->
            itemClick.invoke(getItem(position))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrisisSupportViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.row_crisis_support, parent, false)
            return CrisisSupportViewHolder(view, itemClickedAt)
        }

        override fun onBindViewHolder(holder: CrisisSupportViewHolder, position: Int) {
            val item = getItem(position)
            holder.bind(item)
        }

        inner class CrisisSupportViewHolder(
            itemView: View,
            private val itemClick: (position: Int) -> Unit
        ) :
            RecyclerView.ViewHolder(itemView) {

            init {
                itemView.setOnClickListener {
                    itemClick.invoke(adapterPosition)
                }
            }

            fun bind(item: CrisisSupportItem) {
                itemView.row_crisis_support_text_view_name.text = item.name
                itemView.row_crisis_support_text_view_phone_number.text = item.number
            }
        }

    }

}