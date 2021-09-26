package com.chatowl.presentation.settings.logout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.chatowl.R
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.findNavController
import com.chatowl.presentation.main.MainGraphViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_logout.*


class LogoutDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: LogoutViewModel
    private val mainGraphViewModel: MainGraphViewModel by navGraphViewModels(R.id.nav_splash) {
        val application = ChatOwlApplication.get()
        val userPool = application.userPool
        MainGraphViewModel.Factory(application, userPool, requireActivity())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_logout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(LogoutViewModel::class.java)

        dialog_logout_text_view_no_thanks.setOnClickListener {
            dismiss()
        }
        dialog_logout_button_logout.setOnClickListener {
            mainGraphViewModel.onLogout()
        }

        addObservers()
    }

    private fun addObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.navigate.observe(viewLifecycleOwner, {
            activity?.findNavController(R.id.main_nav_host_fragment)?.navigate(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }

    private fun removeObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.navigate.removeObservers(viewLifecycleOwner)
    }
}
