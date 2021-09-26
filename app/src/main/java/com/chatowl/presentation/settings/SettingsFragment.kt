package com.chatowl.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : ViewModelFragment<SettingsViewModel>(SettingsViewModel::class.java) {

    override fun getScreenName() = "Settings"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_settings_item_my_account.setOnClickListener {
            viewModel.onMyAccountClicked()
        }

        fragment_settings_item_app_tour.setOnClickListener(){
            viewModel.onAppTourClicked()
        }

        fragment_settings_item_app_preferences.setOnClickListener {
            viewModel.onAppPreferencesClicked()
        }

        fragment_settings_item_notification_preferences.setOnClickListener {
            viewModel.onNotificationPreferencesClicked()
        }

        fragment_settings_item_email_preferences.setOnClickListener {
            viewModel.onEmailPreferencesClicked()
        }

        fragment_settings_item_about_chatowl.setOnClickListener {
            viewModel.onAboutChatOwlClicked()
        }

        fragment_settings_item_contact_us.setOnClickListener {
            viewModel.onContactUsClicked()
        }

        fragment_settings_item_give_feedback.setOnClickListener {
            viewModel.onFeedbackClicked()
        }

        fragment_settings_item_logout.setOnClickListener {
            viewModel.onLogoutClicked()
        }

        fragment_settings_item_crisis_support.setOnClickListener {
            viewModel.onCrisisSupportClicked()
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