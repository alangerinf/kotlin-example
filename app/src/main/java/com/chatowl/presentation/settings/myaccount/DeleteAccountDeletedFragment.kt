package com.chatowl.presentation.settings.myaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import com.chatowl.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_deleted_account.*

class DeleteAccountDeletedFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_deleted_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog_deleted_account_button.setOnClickListener {
            dismiss()
        }
    }

}
