package com.chatowl.presentation.message.v2


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
import kotlinx.android.synthetic.main.dialog_message_v2.*

class MessageDialogFragmentV2 : BottomSheetDialogFragment() {

    private lateinit var viewModel: MessageDialogV2ViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_message_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val application = ChatOwlApplication.get()
        val args: MessageDialogFragmentV2Args by navArgs()
        viewModel =
            ViewModelProvider(this, MessageDialogV2ViewModel.Factory(application, args)).get(
                MessageDialogV2ViewModel::class.java
            )

        viewModel.messageBodyString.observe(viewLifecycleOwner, {
            dialog_message_v2_text_view.text = it
        })

        viewModel.messageTitleString.observe(viewLifecycleOwner, {
            dialog_message_v2_title_text_view.text = it
        })

        viewModel.showIcon.observe(viewLifecycleOwner, {
            dialog_message_v2_image_view_icon.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewModel.iconResourceId.observe(viewLifecycleOwner, {
            dialog_message_v2_image_view_icon.setImageResource(it)
        })

        dialog_message_v2_button.setOnClickListener {
            val direction =
                MessageDialogFragmentV2Directions.actionMessageDialogV2ToCurrentPlanFragment()
            findNavController().navigate(direction)
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
