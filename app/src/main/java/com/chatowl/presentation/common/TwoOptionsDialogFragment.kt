package com.chatowl.presentation.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_two_options.*


class TwoOptionsDialogFragment(
    private val positiveEvent: Runnable,
    private val negativeEvent: Runnable,
    private val title: String,
    private val body: String,
    private val positiveText: String,
    private val negativeText: String
) : BottomSheetDialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_two_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog_two_options_text_view_title.setOnClickListener {
            findNavController().navigateUp()
        }
        dialog_two_options_text_view_title.text = title
        dialog_two_options_text_view_body.text = body
        dialog_two_options_button_positive.apply {
            text = positiveText
            setOnClickListener {
                positiveEvent.run()
                dismiss()
            }
        }
        dialog_two_options_button_negative.apply {
            text = negativeText
            setOnClickListener {
                negativeEvent.run()
                dismiss()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener {
            val bottomSheetDialog: BottomSheetDialog? = it as? BottomSheetDialog
            val bottomSheetBehavior = bottomSheetDialog?.behavior
            bottomSheetBehavior?.state = STATE_EXPANDED
            bottomSheetBehavior?.setDraggable(false)
        }
        return dialog
    }
}
