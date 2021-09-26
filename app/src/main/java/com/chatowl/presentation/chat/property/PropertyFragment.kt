package com.chatowl.presentation.chat.property

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
import com.chatowl.presentation.main.MainActivity
import kotlinx.android.synthetic.main.fragment_property.*

class PropertyFragment : ViewModelFragment<PropertyViewModel>(PropertyViewModel::class.java) {

    override fun setViewModel(): PropertyViewModel {
        val application = ChatOwlApplication.get()
        val args: PropertyFragmentArgs by navArgs()
        return ViewModelProvider(this, PropertyViewModel.Factory(application, args)).get(PropertyViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_property, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_property_button_send.setOnClickListener {
            viewModel.onSendValueClicked(fragment_property_edit_text_message.text.toString())
        }
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_property_edit_text_message.isEnabled = !isLoading
            fragment_property_button_send.isEnabled = !isLoading
        })
        viewModel.success.observe(viewLifecycleOwner, { success ->
            if(success) {
                showSnackBarMessage(getString(R.string.success))
                findNavController().navigateUp()
            } else {
                showSnackBarMessage(getString(R.string.error))
            }
        })
        viewModel.valueString.observe(viewLifecycleOwner, {
            fragment_property_edit_text_message.setText(it)
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
        viewModel.success.removeObservers(viewLifecycleOwner)
        viewModel.valueString.removeObservers(viewLifecycleOwner)
    }
}