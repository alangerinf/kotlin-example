package com.chatowl.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.chatowl.R
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_onboarding.*
import kotlinx.android.synthetic.main.fragment_onboarding.view.*


class OnboardingFragment : ViewModelFragment<OnboardingViewModel>(OnboardingViewModel::class.java) {

    companion object {
        private const val NUM_PAGES = 3
    }

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_onboarding, container, false)

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = v.fragment_onboarding_view_pager

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter
        v.onboarding_view_pager_indicator.setUpWithViewPager2(viewPager)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onboarding_view_pager_indicator_container.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        fragment_onboarding_button_get_started.setOnClickListener {
            viewModel.onGetStartedClicked()
        }

        fragment_onboarding_text_view_login.setOnClickListener {
            viewModel.onLoginClicked()
        }
    }

    /**
     * A simple pager adapter that represents FeatureOnboardingSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            val fragment = FeatureOnboardingSlidePageFragment()
            fragment.arguments = Bundle().apply {
                putInt(
                        FeatureOnboardingSlidePageFragment.ARG_DRAWABLE_ID, when (position) {
                    0 -> R.drawable.background_onboarding_1
                    1 -> R.drawable.background_onboarding_2
                    else -> R.drawable.background_onboarding_3
                }
                )
                putString(
                        FeatureOnboardingSlidePageFragment.ARG_TITLE, when (position) {
                    0 -> getString(R.string.onboarding_title_1)
                    1 -> getString(R.string.onboarding_title_2)
                    else -> getString(R.string.onboarding_title_3)
                }
                )
                putString(
                        FeatureOnboardingSlidePageFragment.ARG_DESCRIPTION, when (position) {
                    0 -> getString(R.string.onboarding_body_1)
                    1 -> getString(R.string.onboarding_body_2)
                    else -> getString(R.string.onboarding_body_3)
                }
                )
                putString(
                    FeatureOnboardingSlidePageFragment.ARG_BOLD, when (position) {
                        0 -> getString(R.string.onboarding_bold_1)
                        1 -> ""
                        else -> ""
                    }
                )
            }
            return fragment
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