package com.chatowl.presentation.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_ok.*


class OkDialogFragment : BottomSheetDialogFragment() {


    val args: OkDialogFragmentArgs by navArgs()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_ok, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog_ok_button.setOnClickListener {
            findNavController().navigateUp()
        }
        dialog_ok_text_view_title.text = args.title
        dialog_ok_text_view_body.text = args.body
    }

    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog {
        val dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener {
            val bottomSheetDialog : BottomSheetDialog? = it as? BottomSheetDialog
            val bottomSheetBehavior = bottomSheetDialog?.behavior
            bottomSheetBehavior?.state = STATE_EXPANDED
            bottomSheetBehavior?.setDraggable(false)
        }
        return dialog
    }
}
