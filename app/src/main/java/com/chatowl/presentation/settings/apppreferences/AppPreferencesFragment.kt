package com.chatowl.presentation.settings.apppreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_app_preferences.*

class AppPreferencesFragment : ViewModelFragment<AppPreferencesViewModel>(AppPreferencesViewModel::class.java) {

    override fun getScreenName() = "App preferences"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_app_preferences_item_tool_feedback.setOnCheckedChangeListener {
            viewModel.onToolFeedbackSwitchChanged(it)
        }

        fragment_app_preferences_item_restore_purchases.setOnClickListener {
            viewModel.restorePurchasesClicked()
        }

        fragment_app_preferences_item_clear_downloads.setOnClickListener {
            viewModel.clearFilesClicked()
        }
    }

    override fun addObservers() {
        viewModel.preferences.observe(viewLifecycleOwner, { preferences ->
            fragment_app_preferences_item_tool_feedback.isChecked = preferences.allowToolFeedback
        })
        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            fragment_app_preferences_linear_progress_indicator.visibility = if(isLoading) View.VISIBLE else View.GONE
        })
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
    }

    override fun removeObservers() {
        viewModel.preferences.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
    }

}