package com.chatowl.presentation.common.fragments

import androidx.fragment.app.Fragment
import com.chatowl.presentation.common.activities.BaseActivity
import com.chatowl.presentation.common.utils.SnackBarAction

open class FragmentBase : Fragment() {

    fun showSnackBarMessage(message: String, action: SnackBarAction? = null) {
        (requireActivity() as BaseActivity?)?.showSnackBarMessage(message, action)
    }

}