package com.chatowl.presentation.common.extensions

import android.app.Activity
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.Navigation

fun Activity.findNavController(@IdRes viewId: Int): NavController =
    Navigation.findNavController(this, viewId)