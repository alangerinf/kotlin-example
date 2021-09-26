package com.chatowl.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.SecureAppUtils

class SplashFragment : ViewModelFragment<SplashViewModel>(SplashViewModel::class.java) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun addObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.splashDone.observe(viewLifecycleOwner, {
            if(it) mainGraphViewModel.initAuth()
        })
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        mainGraphViewModel.challengeContinuation.observe(viewLifecycleOwner, {
            findNavController().navigate(SplashFragmentDirections.actionSplashToLoginConfirmationCode())
        })
        mainGraphViewModel.loginSuccess.observe(viewLifecycleOwner, {
            if (SecureAppUtils.getCurrentSecureMode().first == SecureAppUtils.SecureMode.NONE) {
                findNavController().navigate(SplashFragmentDirections.actionSplashToMainGraph())
            } else {
               findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToSecureCodeAuthenticationFragment())
            }
        })
    }

    override fun removeObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.splashDone.removeObservers(viewLifecycleOwner)
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.navigate.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.challengeContinuation.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.loginSuccess.removeObservers(viewLifecycleOwner)
    }

}