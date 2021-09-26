package com.chatowl.presentation.secure

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.WindowManager.LayoutParams
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.databinding.FragmentSecureCodeAuthenticationBinding
import com.chatowl.presentation.common.activities.BaseActivity
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.utils.SecureAppUtils
import kotlinx.android.synthetic.main.layout_request_pin.view.*


class SecureCodeAuthenticationFragment :
    ViewModelFragment<SecureCodeAuthenticationViewModel>(SecureCodeAuthenticationViewModel::class.java) {


    private lateinit var binding: FragmentSecureCodeAuthenticationBinding

    override fun setViewModel(): SecureCodeAuthenticationViewModel {
        val application = ChatOwlApplication.get()
        return ViewModelProvider(
            this,
            SecureCodeAuthenticationViewModel.Factory(application, this)
        ).get(
            SecureCodeAuthenticationViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            this.requireActivity().window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_secure_code_authentication,
            container,
            false
        )
        return binding.root
    }

    private val textWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            binding.layoutPIN.tView_message.visibility = View.GONE
            binding.layoutPIN.iView_messageStatus.visibility = View.GONE

            binding.layoutPIN.apply {
                if (eText_digit_1.isFocused && s.length == 1) {
                    eText_digit_2.requestFocus()
                } else if (eText_digit_2.isFocused && s.length == 1) {
                    eText_digit_3.requestFocus()
                } else if (eText_digit_3.isFocused && s.length == 1) {
                    eText_digit_4.requestFocus()
                }

                if (isCodeComplete()) {
                    (requireActivity() as BaseActivity?)?.closeKeyboard(eText_digit_1)
                    binding.layoutPIN.clearFocus()
                    viewModel.tryPINAuthentication(getCode())

                }
            }

        }

        override fun afterTextChanged(s: Editable) {}
    }

    private fun getCode(): String {
        return binding.layoutPIN.eText_digit_1.text.toString() +
                binding.layoutPIN.eText_digit_2.text.toString() +
                binding.layoutPIN.eText_digit_3.text.toString() +
                binding.layoutPIN.eText_digit_4.text.toString()
    }

    private fun isCodeComplete(): Boolean {
        return binding.layoutPIN.eText_digit_1.text.toString().isNotEmpty() &&
                binding.layoutPIN.eText_digit_2.text.toString().isNotEmpty() &&
                binding.layoutPIN.eText_digit_3.text.toString().isNotEmpty() &&
                binding.layoutPIN.eText_digit_4.text.toString().isNotEmpty()
    }


    private val focusListener = View.OnFocusChangeListener { view, b ->
        binding.layoutPIN.apply {
            if (b) {
                when (view) {
                    eText_digit_1 -> cleanSince1()
                    eText_digit_2 -> cleanSince2()
                    eText_digit_3 -> cleanSince3()
                    eText_digit_4 -> cleanSince4()
                }
            } else {
                when (view) {
                    eText_digit_1 -> iView_point1.visibility = View.VISIBLE
                    eText_digit_2 -> iView_point2.visibility = View.VISIBLE
                    eText_digit_3 -> iView_point3.visibility = View.VISIBLE
                    eText_digit_4 -> iView_point4.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun clearCode() {
        cleanSince1()
        binding.layoutPIN.tView_message.visibility = View.GONE
        binding.layoutPIN.iView_messageStatus.visibility = View.GONE
    }

    val cleanSince4 = {
        binding.layoutPIN.iView_point4.visibility = View.GONE
        binding.layoutPIN.eText_digit_4.setText("")
        binding.layoutPIN.eText_digit_3.apply {
            if (binding.layoutPIN.eText_digit_4.hasFocus() && text.toString()
                    .isEmpty()
            ) requestFocus()
        }
    }
    val cleanSince3 = {
        binding.layoutPIN.iView_point3.visibility = View.GONE
        binding.layoutPIN.eText_digit_3.setText("")
        cleanSince4()
        binding.layoutPIN.eText_digit_2.apply {
            if (binding.layoutPIN.eText_digit_3.hasFocus() && text.toString()
                    .isEmpty()
            ) requestFocus()
        }
    }
    val cleanSince2 = {
        binding.layoutPIN.iView_point2.visibility = View.GONE
        binding.layoutPIN.eText_digit_2.setText("")
        cleanSince3()
        binding.layoutPIN.eText_digit_1.apply {
            if (binding.layoutPIN.eText_digit_2.hasFocus() && text.toString()
                    .isEmpty()
            ) requestFocus()
        }
    }
    val cleanSince1 = {
        binding.layoutPIN.iView_point1.visibility = View.GONE
        binding.layoutPIN.eText_digit_1.setText("")
        cleanSince2()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)
        viewModel.checkAuthenticationMode()

        binding.apply {
            layoutPIN.eText_digit_1.addTextChangedListener(textWatcher)
            layoutPIN.eText_digit_2.addTextChangedListener(textWatcher)
            layoutPIN.eText_digit_3.addTextChangedListener(textWatcher)
            layoutPIN.eText_digit_4.addTextChangedListener(textWatcher)

            layoutPIN.eText_digit_1.onFocusChangeListener = focusListener
            layoutPIN.eText_digit_2.onFocusChangeListener = focusListener
            layoutPIN.eText_digit_3.onFocusChangeListener = focusListener
            layoutPIN.eText_digit_4.onFocusChangeListener = focusListener

            tViewCannotAuthenticate.setOnClickListener {
                viewModel.iCannotAuthenticateMyself()
            }
        }

        binding.tViewSecureIdIcon.setOnClickListener {
            viewModel.tryBiometricAuthentication()
        }

        binding.btnTryAgain.setOnClickListener {
            viewModel.tryBiometricAuthentication()
        }
    }


    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        viewModel.authenticationModes.observe(viewLifecycleOwner, {
            when (it) {
                SecureAppUtils.SecureMode.MODE_BIOMETRIC -> {
                    binding.tViewSubtitle.text =
                        getString(R.string.use_biometric_recognition_to_authenticate)
                    binding.layoutBiometricId.visibility = View.VISIBLE
                    binding.layoutPIN.visibility = View.GONE
                }
                SecureAppUtils.SecureMode.MODE_PIN -> {
                    binding.tViewSubtitle.text = "Insert the 4 digits of your code"
                    binding.layoutBiometricId.visibility = View.GONE
                    binding.layoutPIN.visibility = View.VISIBLE
                }
                SecureAppUtils.SecureMode.NONE -> {
                    binding.tViewSubtitle.text = ""
                }
            }
        })

        viewModel.scannerStatus.observe(viewLifecycleOwner, {
            val (mode, message) = it

            when (mode) {
                SecureCodeAuthenticationViewModel.ScannerStatusType.NORMAL -> {
                    //only used in MODE_BIOMETRIC
                    binding.tViewSecureIdDescription.text = message
                    binding.tViewSecureIdIcon.setImageResource(R.drawable.ic_secure_circle_touch_id)
                    binding.btnTryAgain.visibility = View.GONE
                }
                SecureCodeAuthenticationViewModel.ScannerStatusType.ERROR -> {
                    when (viewModel.authenticationModes.value) {
                        SecureAppUtils.SecureMode.MODE_BIOMETRIC -> {
                            binding.tViewSecureIdDescription.text = message
                            binding.tViewSecureIdIcon.setImageResource(R.drawable.ic_secure_circle_error)
                            binding.btnTryAgain.visibility = View.VISIBLE
                        }
                        SecureAppUtils.SecureMode.MODE_PIN -> {
                            if (message.isNotEmpty()) {
                                binding.layoutPIN.tView_message.apply {
                                    text = message
                                    setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                                    visibility = View.VISIBLE
                                }

                                binding.layoutPIN.iView_messageStatus.apply {
                                    setImageResource(R.drawable.ic_close)
                                    setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
                                    visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
                SecureCodeAuthenticationViewModel.ScannerStatusType.SUCCESS -> {
                    when (viewModel.authenticationModes.value) {
                        SecureAppUtils.SecureMode.MODE_BIOMETRIC -> {
                            binding.tViewSecureIdDescription.text = message
                            binding.tViewSecureIdIcon.setImageResource(R.drawable.ic_secure_circle_success)
                            binding.btnTryAgain.visibility = View.GONE
                        }
                        SecureAppUtils.SecureMode.MODE_PIN -> {
                            if (message.isNotEmpty()) {
                                binding.layoutPIN.tView_message.apply {
                                    text = message
                                    setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                                    visibility = View.VISIBLE
                                }
                                binding.layoutPIN.iView_messageStatus.apply {
                                    setImageResource(R.drawable.ic_check)
                                    setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                                    visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
            }
        })


    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
    }

}