package com.chatowl.presentation.onboarding

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chatowl.R
import kotlinx.android.synthetic.main.fragment_feature_onboarding_slide_page.*

class FeatureOnboardingSlidePageFragment : Fragment() {

    companion object {
        const val ARG_DRAWABLE_ID = "arg_drawable_id"
        const val ARG_TITLE = "arg_title"
        const val ARG_DESCRIPTION = "arg_description"
        const val ARG_BOLD = "arg_bold"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_feature_onboarding_slide_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_DRAWABLE_ID) }?.apply {
            feature_onboarding_slide_page_image_view.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), getInt(
                ARG_DRAWABLE_ID
            )))
        }
        arguments?.takeIf { it.containsKey(ARG_TITLE) }?.apply {
            feature_onboarding_slide_page_title.text = getString(ARG_TITLE)
        }
        val bold = arguments?.getString(ARG_BOLD) ?: ""
        arguments?.takeIf { it.containsKey(ARG_DESCRIPTION) }?.apply {

            val body = SpannableString(getString(ARG_DESCRIPTION))
            if(bold.isNotEmpty()) {
                body.setSpan(
                    StyleSpan(Typeface.BOLD),
                    body.indexOf(bold),
                    body.indexOf(bold) + bold.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            feature_onboarding_slide_page_description.text = body
        }
    }
}
