package com.chatowl.presentation.toolbox.host

import androidx.navigation.NavDirections

interface ViewPagerFragmentInteraction {
    fun onPageFragmentAction(navDirections: NavDirections)
}