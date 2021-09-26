package com.chatowl.presentation.settings.contactus

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.chatowl.R
import com.chatowl.presentation.common.extensions.*
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.TextWatcherAdapter
import kotlinx.android.synthetic.main.dialog_contact_us.*


class ContactUsDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: ContactUsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set the window no floating style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.dialog_contact_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set the multi line edit text to handle ime action done and cap sentences
        dialog_contact_us_edit_text_message.getEditText().imeOptions = EditorInfo.IME_ACTION_DONE
        dialog_contact_us_edit_text_message.getEditText().setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        dialog_contact_us_edit_text_message.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onMessageTextChanged(s.toString())
            }
        })

        viewModel = ViewModelProvider(this).get(ContactUsViewModel::class.java)

        viewModel.emailErrorMessage.observe(viewLifecycleOwner, { stringResourceId ->
            dialog_contact_us_edit_text_email.errorText = getString(stringResourceId)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, { stringResourceId ->
            dialog_contact_us_edit_text_message.errorText = getString(stringResourceId)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            dialog_contact_us_progress_bar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.showForm.observe(viewLifecycleOwner, { showForm ->
            dialog_contact_us_layout_form.visibility =
                    if (showForm) View.VISIBLE else View.INVISIBLE
        })

        viewModel.showSuccess.observe(viewLifecycleOwner, { showSuccess ->
            if (showSuccess) {
                animateSuccess()
            } else {
                dialog_contact_us_layout_success.visibility = View.GONE
            }
        })

        viewModel.isSendButtonEnabled.observe(viewLifecycleOwner, { isEnabled ->
            if (isEnabled)
                dialog_contact_us_button_send.customEnable()
            else
                dialog_contact_us_button_send.customDisable()
        })

        viewModel.dismiss.observe(viewLifecycleOwner, { dismiss ->
            if (dismiss) {
                dismiss()
            }
        })

        dialog_contact_us_button_send.setOnClickListener {
            viewModel.onSendClicked(dialog_contact_us_edit_text_message.text.toString(),
                dialog_contact_us_edit_text_email.text.toString())
        }

        dialog_contact_us_text_view_cancel.setOnClickListener {
            viewModel.onCancelClicked()
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

    // TODO extract to animation utils
    private fun animateSuccess() {
        val translationX = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 50f, 0f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(dialog_contact_us_layout_success, translationX, alpha)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                dialog_contact_us_layout_success.visibility = View.VISIBLE
            }
        })
        animator.start()
    }
}
