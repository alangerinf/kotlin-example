package com.chatowl.presentation.settings.aboutus.privacypolicy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.fragments.FragmentBase
import kotlinx.android.synthetic.main.fragment_privacy_policy.*

class PrivacyPolicyFragment : FragmentBase() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_privacy_policy_layout_main.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        fragment_privacy_policy_header_image_button_close.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}