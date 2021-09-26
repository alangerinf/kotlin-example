package com.chatowl.presentation.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.presentation.common.extensions.customDisable
import com.chatowl.presentation.common.extensions.customEnable
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.isEmailValid
import com.chatowl.presentation.common.widgets.MyEditText
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : ViewModelFragment<LoginViewModel>(LoginViewModel::class.java) {

    val args: LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_login_text_view_contact_support_center.setOnClickListener {
            viewModel.onContactSupportClicked()
        }

        fragment_login_button_send_code.setOnClickListener {
            viewModel.onSendCodeClicked(
                fragment_login_edit_text_email.text.toString()
            )
        }

        fragment_login_text_view_sign_up.setOnClickListener {
            viewModel.onSignUpClicked()
        }

        fragment_login_button_send_code.customDisable()
        fragment_login_edit_text_email.apply {
            setMaxSize(resources.getInteger(R.integer.max_length_email))
            addTextChangedListener( object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val textTrim = text.toString().trim()
                    if (text.toString() != textTrim) text = textTrim
                    if (textTrim.isEmailValid()) fragment_login_button_send_code.customEnable()
                    else fragment_login_button_send_code.customDisable()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        closeKeyboard(this)
                        when {
                            text.toString().isEmpty() -> {
                                errorText = getString(R.string.empty_email_field_error_message_empty)
                                state = MyEditText.State.ERROR
                            }
                            !fragment_login_edit_text_email.text.toString().isEmailValid() -> {
                                errorText = getString(R.string.malformed_email_field_error_message)
                                state = MyEditText.State.ERROR
                            }
                            else -> {
                                errorText = ""
                                state = MyEditText.State.NO_ERROR
                            }
                        }
                    } else {
                        fragment_login_edit_text_email.state = MyEditText.State.NO_ERROR
                    }
                }
        }

        args.email?.let {
            fragment_login_edit_text_email.text = it
        }
    }

    override fun addObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.loginEmail.observe(viewLifecycleOwner, {
            mainGraphViewModel.onSendCodeClick(
                    fragment_login_edit_text_email.text.toString()
            )
        })
        viewModel.emailFieldError.observe(viewLifecycleOwner, {
            it?.let {
                fragment_login_edit_text_email.state = MyEditText.State.ERROR
                fragment_login_edit_text_email.errorText = getString(it)
            } ?: run {
                fragment_login_edit_text_email.state = MyEditText.State.NO_ERROR
            }
        })
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.isLoading.observe(viewLifecycleOwner, { isLoading ->
            if(isLoading) {
                fragment_login_progressbar.visibility = View.VISIBLE
                fragment_login_layout_form.visibility = View.INVISIBLE
            } else {
                fragment_login_progressbar.visibility = View.GONE
                fragment_login_layout_form.visibility = View.VISIBLE
            }
        })
        mainGraphViewModel.email.observe(viewLifecycleOwner, {
            fragment_login_edit_text_email.text = it
        })
        mainGraphViewModel.challengeContinuation.observe(viewLifecycleOwner, {
            findNavController().navigate(LoginFragmentDirections.actionLoginToLoginConfirmation())

        })
        mainGraphViewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
    }

    override fun removeObservers() {
        // FRAGMENT VIEW MODEL
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.loginEmail.removeObservers(viewLifecycleOwner)
        viewModel.emailFieldError.removeObservers(viewLifecycleOwner)
        // MAIN GRAPH VIEW MODEL
        mainGraphViewModel.navigate.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.isLoading.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.email.removeObservers(viewLifecycleOwner)
        mainGraphViewModel.challengeContinuation.removeObservers(viewLifecycleOwner)
    }

}