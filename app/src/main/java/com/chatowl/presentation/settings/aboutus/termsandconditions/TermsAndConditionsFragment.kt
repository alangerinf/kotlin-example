package com.chatowl.presentation.settings.aboutus.termsandconditions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.fragments.FragmentBase
import kotlinx.android.synthetic.main.fragment_terms_and_conditions.*

class TermsAndConditionsFragment : FragmentBase() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_terms_and_conditions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_terms_and_conditions_layout_main.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        fragment_terms_and_conditions_header_image_button_close.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}