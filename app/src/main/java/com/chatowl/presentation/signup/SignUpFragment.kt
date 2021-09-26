package com.chatowl.presentation.signup

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.extensions.customDisable
import com.chatowl.presentation.common.extensions.customEnable
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.isEmailValid
import com.chatowl.presentation.common.widgets.MyEditText
import com.chatowl.presentation.main.MainActivity
import com.google.android.material.internal.TextWatcherAdapter
import com.instabug.library.Instabug.getApplicationContext
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_sign_up.*


class SignUpFragment : ViewModelFragment<SignUpViewModel>(SignUpViewModel::class.java) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSpannableStrings()

//        fragment_sign_up_image_view_logo.doOnApplyWindowInsets { v, windowInsets, padding ->
//            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
//        }

        fragment_sign_up_edit_text_nickname.apply {
            onFocusChangeListener =
                OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) closeKeyboard(this)
                    if (!hasFocus && text.toString().isEmpty()) {
                        state = MyEditText.State.ERROR
                        errorText =
                            getString(R.string.empty_nickname_field_error_message)
                    } else {
                        state = MyEditText.State.NO_ERROR
                    }
                }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val textTrim = text.toString().trim()
                    if (text.toString() != textTrim) {
                        text = textTrim
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }


        fragment_sign_up_edit_text_email.apply {
            setMaxSize(resources.getInteger(R.integer.max_length_email))
            onFocusChangeListener =
                OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        closeKeyboard(this)
                        when {
                            text.toString().isEmpty() -> {
                                errorText =
                                    getString(R.string.empty_email_field_error_message_empty)
                                state = MyEditText.State.ERROR
                            }
                            text.toString().isNotEmpty() && !text.toString().isEmailValid() -> {
                                errorText = getString(R.string.malformed_email_field_error_message)
                                state = MyEditText.State.ERROR
                            }
                            else -> {
                                errorText = ""
                                state = MyEditText.State.NO_ERROR
                            }
                        }
                    } else {
                        state = MyEditText.State.NO_ERROR
                    }
                }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val textTrim = text.toString().trim()
                    if (text.toString() != textTrim) {
                        text = textTrim
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

        }

        fragment_sign_up_button_sign_up.setOnClickListener {
            viewModel.onSignUpClicked(
                fragment_sign_up_edit_text_nickname.text.toString(),
                fragment_sign_up_edit_text_email.text.toString()
            )
        }

        fragment_sign_up_edit_text_nickname.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onNicknameFieldChanged(s.toString())
            }
        })

        fragment_sign_up_edit_text_nickname.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) { openKeyboard(v) }
        }

        fragment_sign_up_edit_text_email.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) { openKeyboard(v) }
        }

        fragment_sign_up_edit_text_email.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onEmailFieldChanged(s.toString())
            }
        })

        fragment_sign_up_check_box_age_confirmation.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onAgeCheckboxChecked(isChecked)
        }

        fragment_sign_up_check_box_terms_and_conditions.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onTermsCheckboxChecked(isChecked)
        }

        fragment_sign_up_text_view_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setSpannableStrings() {
        val spannableString = SpannableString(getString(R.string.checkbox_terms_of_service_message))
        val termsSpannableString = linkSpanToLink(
            spannableString, getString(R.string.terms_and_conditions)
        ) { viewModel.onTermsAndConditionsClicked() }
        val privacySpannableString = linkSpanToLink(
            termsSpannableString, getString(R.string.privacy_policy)
        ) { viewModel.onPrivacyPolicyClicked() }
        fragment_sign_up_text_view_terms_and_conditions.text = privacySpannableString
        fragment_sign_up_text_view_terms_and_conditions.movementMethod =
            LinkMovementMethod.getInstance()
    }

    private fun linkSpanToLink(
        spannableString: SpannableString,
        span: String,
        action: () -> Unit
    ): SpannableString {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                action.invoke()
            }

            @SuppressLint("ResourceType")
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.bgColor = Color.parseColor(requireContext().getString(R.color.windowBackground))
                ds.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
        }
        val startIndex = spannableString.indexOf(span, ignoreCase = true)
        val endIndex = startIndex + span.length
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    override fun addObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.isSignUpButtonEnabled.observe(viewLifecycleOwner, { isEnabled ->
            if (isEnabled) fragment_sign_up_button_sign_up.customEnable()
            else fragment_sign_up_button_sign_up.customDisable()
        })
        viewModel.signUpUser.observe(viewLifecycleOwner, { signUpInformation ->
            mainGraphViewModel.signUp(
                signUpInformation.first,
                signUpInformation.second
            )
        })
        viewModel.nicknameFieldError.observe(viewLifecycleOwner, {
            it?.let {
                fragment_sign_up_edit_text_nickname.state = MyEditText.State.ERROR
                fragment_sign_up_edit_text_nickname.errorText = getString(it)
            } ?: run {
                fragment_sign_up_edit_text_nickname.state = MyEditText.State.NO_ERROR
            }
        })
        viewModel.emailFieldError.observe(viewLifecycleOwner, {
            it?.let {
                fragment_sign_up_edit_text_email.state = MyEditText.State.ERROR
                fragment_sign_up_edit_text_email.errorText = getString(it)
            } ?: run {
                fragment_sign_up_edit_text_email.state = MyEditText.State.NO_ERROR
            }
        })

        viewModel.resourceLink.observe(viewLifecycleOwner, {
            startActivity( Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(it))))
        })
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                fragment_sign_up_progressbar.visibility = View.VISIBLE
                fragment_sign_up_layout_form.visibility = View.INVISIBLE
            } else {
                fragment_sign_up_progressbar.visibility = View.GONE
                fragment_sign_up_layout_form.visibility = View.VISIBLE
            }
        })
        mainGraphViewModel.challengeContinuation.observe(viewLifecycleOwner, {
            findNavController().navigate(SignUpFragmentDirections.actionSignUpToVerificationCode())
        })
        mainGraphViewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        mainGraphViewModel.errorMessage.observe(viewLifecycleOwner, {
            fragment_sign_up_edit_text_email.errorText = getString(it)
            fragment_sign_up_edit_text_email.state = MyEditText.State.ERROR
        })
    }

    override fun removeObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isSignUpButtonEnabled.removeObservers(viewLifecycleOwner)
        viewModel.signUpUser.removeObservers(viewLifecycleOwner)
        viewModel.nicknameFieldError.removeObservers(viewLifecycleOwner)
        viewModel.emailFieldError.removeObservers(viewLifecycleOwner)
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.isLoading.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.challengeContinuation.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.resourceLink.removeObservers(viewLifecycleOwner)
    }

}