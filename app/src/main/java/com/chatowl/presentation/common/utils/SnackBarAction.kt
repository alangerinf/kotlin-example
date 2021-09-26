package com.chatowl.presentation.common.utils

import android.view.View

data class SnackBarAction (
    val actionText: String,
    val actionClickListener: View.OnClickListener
)