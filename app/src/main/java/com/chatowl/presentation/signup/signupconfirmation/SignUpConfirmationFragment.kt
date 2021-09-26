package com.chatowl.presentation.signup.signupconfirmation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.activities.BaseActivity
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_sign_up_confirmation.*

class SignUpConfirmationFragment : ViewModelFragment<SignUpConfirmationViewModel>(SignUpConfirmationViewModel::class.java) {

    lateinit var incorrectCodeAnimation: ObjectAnimator
    lateinit var correctCodeAnimation: ObjectAnimator

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        fragment_sign_up_confirmation_image_view_logo.doOnApplyWindowInsets { v, windowInsets, padding ->
//            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
//        }

        setupAnimations()

        fragment_sign_up_confirmation_text_view_contact_support_center.setOnClickListener {
            viewModel.onContactSupportClicked()
        }

        fragment_sign_up_confirmation_text_view_resend_code.setOnClickListener {
            mainGraphViewModel.resendCode()
        }

        fragment_sign_up_confirmation_edit_text_digit_1.addTextChangedListener(textWatcher)
        fragment_sign_up_confirmation_edit_text_digit_2.addTextChangedListener(textWatcher)
        fragment_sign_up_confirmation_edit_text_digit_3.addTextChangedListener(textWatcher)
        fragment_sign_up_confirmation_edit_text_digit_4.addTextChangedListener(textWatcher)
        fragment_sign_up_confirmation_edit_text_digit_5.addTextChangedListener(textWatcher)
        fragment_sign_up_confirmation_edit_text_digit_6.addTextChangedListener(textWatcher)

        fragment_sign_up_confirmation_edit_text_digit_1.setOnKeyListener(onKeyListener)
        fragment_sign_up_confirmation_edit_text_digit_2.setOnKeyListener(onKeyListener)
        fragment_sign_up_confirmation_edit_text_digit_3.setOnKeyListener(onKeyListener)
        fragment_sign_up_confirmation_edit_text_digit_4.setOnKeyListener(onKeyListener)
        fragment_sign_up_confirmation_edit_text_digit_5.setOnKeyListener(onKeyListener)
        fragment_sign_up_confirmation_edit_text_digit_6.setOnKeyListener(onKeyListener)
    }

    val cleanSince6 = {
        fragment_sign_up_confirmation_edit_text_digit_6.setText("")
        fragment_sign_up_confirmation_edit_text_digit_5.apply {
            if (fragment_sign_up_confirmation_edit_text_digit_6.hasFocus() && text.toString().isEmpty()) requestFocus()
        }
    }
    val cleanSince5 = {
        fragment_sign_up_confirmation_edit_text_digit_5.setText("")
        cleanSince6()
        fragment_sign_up_confirmation_edit_text_digit_4.apply {
            if (fragment_sign_up_confirmation_edit_text_digit_5.hasFocus() && text.toString().isEmpty()) requestFocus()
        }
    }
    val cleanSince4 = {
        fragment_sign_up_confirmation_edit_text_digit_4.setText("")
        cleanSince5()
        fragment_sign_up_confirmation_edit_text_digit_3.apply {
            if (fragment_sign_up_confirmation_edit_text_digit_4.hasFocus() && text.toString().isEmpty()) requestFocus()
        }
    }
    val cleanSince3 = {
        fragment_sign_up_confirmation_edit_text_digit_3.setText("")
        cleanSince4()
        fragment_sign_up_confirmation_edit_text_digit_2.apply {
            if (fragment_sign_up_confirmation_edit_text_digit_3.hasFocus() && text.toString().isEmpty()) requestFocus()
        }
    }
    val cleanSince2 = {
        fragment_sign_up_confirmation_edit_text_digit_2.setText("")
        cleanSince3()
        fragment_sign_up_confirmation_edit_text_digit_1.apply {
            if (fragment_sign_up_confirmation_edit_text_digit_2.hasFocus() && text.toString().isEmpty()) requestFocus()
        }
    }
    val cleanSince1 = {
        fragment_sign_up_confirmation_edit_text_digit_1.setText("")
        cleanSince2()
    }

    private val focusListener = View.OnFocusChangeListener{ view, b ->
        if(b) {
            when (view) {
                fragment_sign_up_confirmation_edit_text_digit_1 -> cleanSince1()
                fragment_sign_up_confirmation_edit_text_digit_2 -> cleanSince2()
                fragment_sign_up_confirmation_edit_text_digit_3 -> cleanSince3()
                fragment_sign_up_confirmation_edit_text_digit_4 -> cleanSince4()
                fragment_sign_up_confirmation_edit_text_digit_5 -> cleanSince5()
                fragment_sign_up_confirmation_edit_text_digit_6 -> cleanSince6()
            }
        }
    }

    override fun addObservers() {

        mainGraphViewModel.resetAccSentCodes()

        mainGraphViewModel.accSentCodes.observe(viewLifecycleOwner, { accSentCodes ->
            if (accSentCodes>0) {
                findNavController()
                    .navigate(
                        SignUpConfirmationFragmentDirections
                            .actionSignupConfirmationFragmentToCodeResentDialogFragment(
                                getString(R.string.code_resent_description),
                                true
                            )
                    )
                changePasswordLineColor(R.color.primaryTextColor)
            }
        })

        // FRAGMENT VIEW MODEL
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        mainGraphViewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                fragment_sign_up_confirmation_progressbar.visibility = View.VISIBLE
                fragment_sign_up_confirmation_layout_form.visibility = View.INVISIBLE
            } else {
                fragment_sign_up_confirmation_progressbar.visibility = View.GONE
                fragment_sign_up_confirmation_layout_form.visibility = View.VISIBLE
            }
        })
        mainGraphViewModel.email.observe(viewLifecycleOwner, {
            fragment_sign_up_confirmation_text_view_email.text = it
        })
        mainGraphViewModel.challengeContinuation.observe(viewLifecycleOwner, {
            if (isCodeComplete()) {
                clearCode()
                fragment_sign_up_confirmation_text_view_result_message.text = getString(R.string.try_again)
                fragment_sign_up_confirmation_text_view_result_message.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_close), null, null, null)
                fragment_sign_up_confirmation_text_view_result_message.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                fragment_sign_up_confirmation_text_view_result_message.visibility = View.VISIBLE
                changePasswordLineColor(R.color.red)
                incorrectCodeAnimation.start()
            } else {
                fragment_sign_up_confirmation_text_view_result_message.visibility = View.INVISIBLE
            }
        })
        mainGraphViewModel.loginSuccess.observe(viewLifecycleOwner, { success ->
            if (success) {
                fragment_sign_up_confirmation_text_view_result_message.text = getString(R.string.welcome)
                fragment_sign_up_confirmation_text_view_result_message.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null, null, null)
                fragment_sign_up_confirmation_text_view_result_message.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                fragment_sign_up_confirmation_text_view_result_message.visibility = View.VISIBLE
                changePasswordLineColor(R.color.colorAccent)
                fragment_sign_up_confirmation_text_view_resend_code.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.lightViolet))
                    alpha = 0.3f
                    isFocusable = false
                    isClickable = false
                }
                correctCodeAnimation.start()
            } else {
                findNavController().navigateUp()
            }
        })
        fragment_sign_up_confirmation_edit_text_digit_1.onFocusChangeListener = focusListener
        fragment_sign_up_confirmation_edit_text_digit_2.onFocusChangeListener = focusListener
        fragment_sign_up_confirmation_edit_text_digit_3.onFocusChangeListener = focusListener
        fragment_sign_up_confirmation_edit_text_digit_4.onFocusChangeListener = focusListener
        fragment_sign_up_confirmation_edit_text_digit_5.onFocusChangeListener = focusListener
        fragment_sign_up_confirmation_edit_text_digit_6.onFocusChangeListener = focusListener
    }

    override fun removeObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.navigate.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.isLoading.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.email.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.challengeContinuation.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.loginSuccess.removeObservers(viewLifecycleOwner)
    }

    fun changePasswordLineColor(color: Int) {
        fragment_sign_up_confirmation_edit_text_digit_1.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
        fragment_sign_up_confirmation_edit_text_digit_2.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
        fragment_sign_up_confirmation_edit_text_digit_3.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
        fragment_sign_up_confirmation_edit_text_digit_4.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
        fragment_sign_up_confirmation_edit_text_digit_5.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
        fragment_sign_up_confirmation_edit_text_digit_6.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    private val onKeyListener = View.OnKeyListener { _, keyCode, event ->
        changePasswordLineColor(R.color.primaryTextColor)
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
            if (fragment_sign_up_confirmation_edit_text_digit_1.isFocused) {
                fragment_sign_up_confirmation_edit_text_digit_1.setText("")
            } else if (fragment_sign_up_confirmation_edit_text_digit_2.isFocused && fragment_sign_up_confirmation_edit_text_digit_2.text?.length == 0) {
                fragment_sign_up_confirmation_edit_text_digit_1.setText("")
                fragment_sign_up_confirmation_edit_text_digit_1.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_3.isFocused && fragment_sign_up_confirmation_edit_text_digit_3.text?.length == 0) {
                fragment_sign_up_confirmation_edit_text_digit_2.setText("")
                fragment_sign_up_confirmation_edit_text_digit_2.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_4.isFocused && fragment_sign_up_confirmation_edit_text_digit_4.text?.length == 0) {
                fragment_sign_up_confirmation_edit_text_digit_3.setText("")
                fragment_sign_up_confirmation_edit_text_digit_3.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_5.isFocused && fragment_sign_up_confirmation_edit_text_digit_5.text?.length == 0) {
                fragment_sign_up_confirmation_edit_text_digit_4.setText("")
                fragment_sign_up_confirmation_edit_text_digit_4.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_6.isFocused && fragment_sign_up_confirmation_edit_text_digit_6.text?.length == 0) {
                fragment_sign_up_confirmation_edit_text_digit_5.setText("")
                fragment_sign_up_confirmation_edit_text_digit_5.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_2.isFocused && fragment_sign_up_confirmation_edit_text_digit_2.text?.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_2.setText("")
            } else if (fragment_sign_up_confirmation_edit_text_digit_3.isFocused && fragment_sign_up_confirmation_edit_text_digit_3.text?.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_3.setText("")
            } else if (fragment_sign_up_confirmation_edit_text_digit_4.isFocused && fragment_sign_up_confirmation_edit_text_digit_4.text?.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_4.setText("")
            } else if (fragment_sign_up_confirmation_edit_text_digit_5.isFocused && fragment_sign_up_confirmation_edit_text_digit_5.text?.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_5.setText("")
            } else if (fragment_sign_up_confirmation_edit_text_digit_6.isFocused && fragment_sign_up_confirmation_edit_text_digit_6.text?.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_6.setText("")
            }

            return@OnKeyListener true
        }
        false
    }

    private val textWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            if (fragment_sign_up_confirmation_edit_text_digit_1.isFocused && s.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_2.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_2.isFocused && s.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_3.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_3.isFocused && s.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_4.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_4.isFocused && s.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_5.requestFocus()
            } else if (fragment_sign_up_confirmation_edit_text_digit_5.isFocused && s.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_6.requestFocus()
            } else if(fragment_sign_up_confirmation_edit_text_digit_6.isFocused && s.length == 1) {
                fragment_sign_up_confirmation_edit_text_digit_6.clearFocus()
            }

            if (isCodeComplete()) {
                (requireActivity() as BaseActivity?)?.closeKeyboard(fragment_sign_up_confirmation_layout_code)
                fragment_sign_up_confirmation_layout_code.clearFocus()
                mainGraphViewModel.sendCode(getCode())
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private fun clearCode() {
        fragment_sign_up_confirmation_edit_text_digit_1.setText("")
        fragment_sign_up_confirmation_edit_text_digit_2.setText("")
        fragment_sign_up_confirmation_edit_text_digit_3.setText("")
        fragment_sign_up_confirmation_edit_text_digit_4.setText("")
        fragment_sign_up_confirmation_edit_text_digit_5.setText("")
        fragment_sign_up_confirmation_edit_text_digit_6.setText("")
    }

    private fun isCodeComplete(): Boolean {
        return fragment_sign_up_confirmation_edit_text_digit_1.text.toString().isNotEmpty() &&
                fragment_sign_up_confirmation_edit_text_digit_2.text.toString().isNotEmpty() &&
                fragment_sign_up_confirmation_edit_text_digit_3.text.toString().isNotEmpty() &&
                fragment_sign_up_confirmation_edit_text_digit_4.text.toString().isNotEmpty() &&
                fragment_sign_up_confirmation_edit_text_digit_5.text.toString().isNotEmpty() &&
                fragment_sign_up_confirmation_edit_text_digit_6.text.toString().isNotEmpty()
    }

    private fun getCode(): String {
        return fragment_sign_up_confirmation_edit_text_digit_1.text.toString() +
                fragment_sign_up_confirmation_edit_text_digit_2.text.toString() +
                fragment_sign_up_confirmation_edit_text_digit_3.text.toString() +
                fragment_sign_up_confirmation_edit_text_digit_4.text.toString() +
                fragment_sign_up_confirmation_edit_text_digit_5.text.toString() +
                fragment_sign_up_confirmation_edit_text_digit_6.text.toString()
    }

    private fun setupAnimations() {
        incorrectCodeAnimation = ObjectAnimator.ofFloat(fragment_sign_up_confirmation_text_view_result_message, View.TRANSLATION_X, 20f, -20f, 0f)
        correctCodeAnimation = ObjectAnimator.ofPropertyValuesHolder(fragment_sign_up_confirmation_text_view_result_message,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f, 1f))
        correctCodeAnimation.duration = 200
        correctCodeAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                viewModel.onCorrectAnimationEnd()
            }
        })
    }

}