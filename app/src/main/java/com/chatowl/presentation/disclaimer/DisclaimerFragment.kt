package com.chatowl.presentation.disclaimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_disclaimer.*

class DisclaimerFragment : ViewModelFragment<DisclaimerViewModel>(DisclaimerViewModel::class.java) {

    override fun setViewModel(): DisclaimerViewModel {
        val application = ChatOwlApplication.get()
        val args: DisclaimerFragmentArgs by navArgs()
        return ViewModelProvider(this, DisclaimerViewModel.Factory(application, args)).get(DisclaimerViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_disclaimer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_disclaimer_button_crisis_link.setOnClickListener {
            viewModel.onCrisisButtonClicked()
        }

        fragment_disclaimer_button_i_understand.setOnClickListener{
            viewModel.onIUnderstandClicked()
        }

        fragment_disclaimer_text_view_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
    }

}