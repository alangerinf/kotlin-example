package com.chatowl.presentation.secure

import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.WindowManager.LayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.databinding.FragmentSecureModePickerBinding
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment

class SecureModePickerFragment :
    ViewModelFragment<SecureModePickerViewModel>(SecureModePickerViewModel::class.java) {

    override fun setViewModel(): SecureModePickerViewModel {
        val application = ChatOwlApplication.get()
        return ViewModelProvider(
            this,
            SecureModePickerViewModel.Factory(application, this)
        ).get(
            SecureModePickerViewModel::class.java
        )
    }

    private lateinit var binding : FragmentSecureModePickerBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.requireActivity().window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_secure_mode_picker,
            container,
            false
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        binding.layoutBiometricRecognition.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                viewModel.fingerprintPressed()
            }
        }
        binding.layoutCode.setOnClickListener {
            viewModel.createCodePressed()
        }
        binding.tViewSkip.setOnClickListener {
            viewModel.skipPressed()
        }
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.isFingerprintAvailable.observe(viewLifecycleOwner, {
            binding.layoutBiometricRecognition.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
    }

}