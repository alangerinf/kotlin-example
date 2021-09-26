package com.chatowl.presentation.settings.myaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.data.database.ChatOwlDatabase
import com.chatowl.data.repositories.ChatOwlHomeRepository
import com.chatowl.data.repositories.ChatOwlUserRepository
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.main.MainGraphViewModel
import kotlinx.android.synthetic.main.fragment_my_account.*

class MyAccountFragment : ViewModelFragment<MyAccountViewModel>(MyAccountViewModel::class.java) {

    override fun getScreenName() = "Account settings"

    override fun setViewModel(): MyAccountViewModel {
        val application = ChatOwlApplication.get()
        val userPool = application.userPool
        val homeRepository = ChatOwlHomeRepository(ChatOwlDatabase.getInstance(application).homeDao)
        return ViewModelProvider(this, MyAccountViewModel.Factory(application, ChatOwlUserRepository, homeRepository, userPool)).get(MyAccountViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        fragment_my_account_item_nickname.setOnClickListener {
            viewModel.onAttributeEdit(MainGraphViewModel.ATTRIBUTE_NICKNAME, 50, false)
        }

        fragment_my_account_text_view_download_data.setOnClickListener {
            viewModel.downloadDataClicked()
        }

        fragment_my_account_text_view_delete_account.setOnClickListener {
            viewModel.deleteAccountClicked()
        }
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.systemMessage.observe(viewLifecycleOwner, {
            when (it) {
                getString(R.string.success) -> {
                    findNavController()
                        .navigate(
                            MyAccountFragmentDirections.actionGlobalOkDialogFragment(
                                getString(R.string.download_account_data_title),
                                getString(R.string.download_account_data_body)))
                }
                else -> {
                    showSnackBarMessage(it)
                }
            }
        })
        viewModel.email.observe(viewLifecycleOwner, {
            fragment_my_account_item_email.value = it
        })
        viewModel.nickname.observe(viewLifecycleOwner, {
            fragment_my_account_item_nickname.value = it
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.systemMessage.removeObservers(viewLifecycleOwner)
        viewModel.email.removeObservers(viewLifecycleOwner)
        viewModel.nickname.removeObservers(viewLifecycleOwner)
    }

}