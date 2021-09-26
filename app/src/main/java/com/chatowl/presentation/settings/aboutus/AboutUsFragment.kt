package com.chatowl.presentation.settings.aboutus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.chatowl.BuildConfig
import com.chatowl.R
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.extensions.findNavController
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_about_us.*

class AboutUsFragment : ViewModelFragment<AboutUsViewModel>(AboutUsViewModel::class.java) {

    override fun getScreenName() = "About us"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_about_us_layout_main.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        fragment_about_us_header_image_button_close.setOnClickListener {
            findNavController().navigateUp()
        }

        fragment_about_us_button_chat_owl_website.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.chat_owl_website_url)))
            if (browserIntent.resolveActivity(requireActivity().application.packageManager) != null) {
                startActivity(browserIntent)
            } else {
                showSnackBarMessage(getString(R.string.no_internet_browser_toast_message))
            }
        }

        fragment_about_us_text_view_terms_and_conditions.setOnClickListener {
            viewModel.onTermsAndConditionsClicked()
        }

        fragment_about_us_text_view_privacy_policy.setOnClickListener {
            viewModel.onPrivacyPolicyClicked()
        }

        fragment_about_us_text_view_email_this.setOnClickListener {
            viewModel.onEmailThisClicked()
        }

        fragment_about_us_version.text = resources.getString(R.string.version_name, BuildConfig.VERSION_NAME)
    }

    override fun addObservers() {
        viewModel.fullScreenNavigate.observe(viewLifecycleOwner, {
            activity?.findNavController(R.id.main_nav_host_fragment)?.navigate(it)
        })
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.isLegalEmailSent.observe(viewLifecycleOwner, {
            if(it){
                findNavController()
                    .navigate(AboutUsFragmentDirections
                        .actionGlobalOkDialogFragment(
                            resources.getString(R.string.legal_email_sent_title),
                            resources.getString(R.string.legal_email_sent_body)))
            }
        })
        viewModel.resourcelink.observe(viewLifecycleOwner, {
            startActivity( Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(it))))
        })
    }

    override fun removeObservers() {
        viewModel.fullScreenNavigate.removeObservers(viewLifecycleOwner)
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.resourcelink.removeObservers(viewLifecycleOwner)
    }

}