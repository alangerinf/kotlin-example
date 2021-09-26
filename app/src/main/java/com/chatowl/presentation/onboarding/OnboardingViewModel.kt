package com.chatowl.presentation.onboarding

import android.app.Application
import com.chatowl.presentation.common.BaseViewModel

class OnboardingViewModel(application: Application) : BaseViewModel(application) {

    fun onGetStartedClicked() {
        _navigate.value = OnboardingFragmentDirections.actionOnboardingToDisclaimer(false)
    }

    fun onLoginClicked() {
        _navigate.value = OnboardingFragmentDirections.actionOnboardingToDisclaimer(true)
    }
}
