package com.chatowl.presentation.settings.myaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_delete_account.*


class DeleteAccountDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: DeleteAccountViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_delete_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application = ChatOwlApplication.get()
        viewModel = ViewModelProvider(
            this, DeleteAccountViewModel.Factory(
                application,
                application.userPool, ChatOwlUserRepository
            )
        ).get(DeleteAccountViewModel::class.java)

        dialog_delete_account_button_delete.setOnClickListener {
            viewModel.onDeleteClicked()
        }

        dialog_delete_account_text_view_no_thanks.setOnClickListener {
            dismiss()
        }

        addObservers()
    }

    private fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.canLogout.observe(viewLifecycleOwner, {
            logout()
        })
    }

    private fun logout() {
        viewModel.onLogout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }

    private fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
    }
}
