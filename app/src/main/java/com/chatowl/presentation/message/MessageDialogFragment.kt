package com.chatowl.presentation.message

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_message.*

@Deprecated("replace with MessageDialogFragmentV2 (have more functionalities)")
class MessageDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: MessageDialogViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application  = ChatOwlApplication.get()
        val args: MessageDialogFragmentArgs by navArgs()
        viewModel = ViewModelProvider(this, MessageDialogViewModel.Factory(application, args)).get(MessageDialogViewModel::class.java)

        viewModel.messageBodyResourceId.observe(viewLifecycleOwner, {
            dialog_message_text_view.text = getString(it)
        })

        viewModel.showIcon.observe(viewLifecycleOwner, {
            dialog_message_image_view_icon.visibility = if(it) View.VISIBLE else View.GONE
        })

        dialog_message_button.setOnClickListener {
            findNavController().navigateUp()
        }
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
