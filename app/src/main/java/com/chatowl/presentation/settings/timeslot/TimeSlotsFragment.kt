package com.chatowl.presentation.settings.timeslot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_time_slots.*

class TimeSlotsFragment : ViewModelFragment<TimeSlotsViewModel>(TimeSlotsViewModel::class.java) {

    override fun getScreenName() = "Time Slots"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_time_slots, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_time_slots_text_view_morning.setOnClickListener {
            
        }

        fragment_time_slots_text_view_midday.setOnClickListener {

        }

        fragment_time_slots_text_view_afternoon.setOnClickListener {

        }

        fragment_time_slots_text_view_evening.setOnClickListener {

        }
    }

    override fun addObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_time_slots_linear_progress_indicator.visibility = if(isLoading) View.VISIBLE else View.GONE
        })
    }

    override fun removeObservers() {
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
    }

}