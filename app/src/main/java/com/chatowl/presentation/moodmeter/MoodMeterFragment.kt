package com.chatowl.presentation.moodmeter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.chatowl.R
import com.chatowl.databinding.FragmentMoodMeterBinding
import com.chatowl.presentation.chat.MoodMeterAdapter
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.toolbox.host.ViewPagerFragmentInteraction


class MoodMeterFragment(private val fragmentInteractionListener: ViewPagerFragmentInteraction) : ViewModelFragment<MoodMeterViewModel>(MoodMeterViewModel::class.java) {

    private val TAG = MoodMeterFragment::class.java.simpleName
    private lateinit var binding: FragmentMoodMeterBinding
    private lateinit var moodMeterAdapter: MoodMeterAdapter

    override fun getScreenName() = "Mood list"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mood_meter, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Item updated successfully")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChatItemsRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getMoodMeters()
    }

    @ExperimentalStdlibApi
    override fun onAttach(context: Context) {
        super.onAttach(context)
        moodMeterAdapter = MoodMeterAdapter(context) { item ->
            startForResult.launch(
                Intent(context, MoodMeterPickerActivity::class.java).apply {
                    putExtra(MoodMeterPickerActivity.EXTRA_KEY_TOOLBOX_MOOD_METER, item)
                })
        }
    }

    private fun setupChatItemsRecyclerView() {
        moodMeterAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rView.scrollToPosition(positionStart)
            }
        })
        binding.rView.adapter = moodMeterAdapter
    }

    override fun addObservers() {
        viewModel.moodMeters.observe(viewLifecycleOwner, {
            moodMeterAdapter.setList(it.toMutableList())
            if(it.isEmpty()) {
                binding.layoutNoResults.visibility = View.VISIBLE
                binding.rView.visibility = View.GONE
                binding.tViewTitle.visibility = View.GONE
            } else {
                binding.layoutNoResults.visibility = View.GONE
                binding.rView.visibility = View.VISIBLE
                binding.tViewTitle.visibility = View.VISIBLE
            }
        })
    }

    override fun removeObservers() {
        viewModel.moodMeters.removeObservers(viewLifecycleOwner)
    }

}