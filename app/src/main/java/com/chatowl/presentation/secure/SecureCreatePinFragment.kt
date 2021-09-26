package com.chatowl.presentation.secure

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.WindowManager.LayoutParams
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.databinding.FragmentSecureCreatePinBinding
import com.chatowl.presentation.common.activities.BaseActivity
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.layout_request_pin.view.*

class SecureCreatePinFragment :
    ViewModelFragment<SecureCreatePinViewModel>(SecureCreatePinViewModel::class.java) {

    override fun setViewModel(): SecureCreatePinViewModel {
        val application = ChatOwlApplication.get()
        return ViewModelProvider(
            this,
            SecureCreatePinViewModel.Factory(application, this)
        ).get(
            SecureCreatePinViewModel::class.java
        )
    }


    private lateinit var binding : FragmentSecureCreatePinBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            this.requireActivity().window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_secure_create_pin,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        binding.apply {
            layoutPIN.eText_digit_1.addTextChangedListener(textWatcher)
            layoutPIN.eText_digit_2.addTextChangedListener(textWatcher)
            layoutPIN.eText_digit_3.addTextChangedListener(textWatcher)
            layoutPIN.eText_digit_4.addTextChangedListener(textWatcher)

            layoutPIN.eText_digit_1.onFocusChangeListener = focusListener
            layoutPIN.eText_digit_2.onFocusChangeListener = focusListener
            layoutPIN.eText_digit_3.onFocusChangeListener = focusListener
            layoutPIN.eText_digit_4.onFocusChangeListener = focusListener

            tViewBack.setOnClickListener {
                viewModel.backPressed()
            }
        }
    }


    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        viewModel.currentCodeStatus.observe(viewLifecycleOwner, {
            clearCode()
            binding.tViewSubtitle.text = getString(it.subtitleMessageId)
        })

        viewModel.successMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                binding.layoutPIN.tView_message.apply {
                    text = it
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                    visibility = View.VISIBLE
                }
                binding.layoutPIN.iView_messageStatus.apply {
                    setImageResource(R.drawable.ic_check)
                    setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                    visibility = View.VISIBLE
                }
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                binding.layoutPIN.tView_message.apply {
                    text = it
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    visibility = View.VISIBLE
                }

                binding.layoutPIN.iView_messageStatus.apply {
                    setImageResource(R.drawable.ic_close)
                    setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
                    visibility = View.VISIBLE
                }
            }
        })

    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.isLoading.removeObservers(viewLifecycleOwner)
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
            if (binding.layoutPIN.eText_digit_4.hasFocus() && text.toString().isEmpty()) requestFocus()
        }
    }
    val cleanSince3 = {
        binding.layoutPIN.iView_point3.visibility = View.GONE
        binding.layoutPIN.eText_digit_3.setText("")
        cleanSince4()
        binding.layoutPIN.eText_digit_2.apply {
            if (binding.layoutPIN.eText_digit_3.hasFocus() && text.toString().isEmpty()) requestFocus()
        }
    }
    val cleanSince2 = {
        binding.layoutPIN.iView_point2.visibility = View.GONE
        binding.layoutPIN.eText_digit_2.setText("")
        cleanSince3()
        binding.layoutPIN.eText_digit_1.apply {
            if (binding.layoutPIN.eText_digit_2.hasFocus() && text.toString().isEmpty()) requestFocus()
        }
    }
    val cleanSince1 = {
        binding.layoutPIN.iView_point1.visibility = View.GONE
        binding.layoutPIN.eText_digit_1.setText("")
        cleanSince2()
    }

    private val focusListener = View.OnFocusChangeListener{ view, b ->
        binding.layoutPIN.apply {
            if(b) {
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

    private fun getCode(): String {
        return binding.layoutPIN.eText_digit_1.text.toString() +
                binding.layoutPIN.eText_digit_2.text.toString() +
                binding.layoutPIN.eText_digit_3.text.toString() +
                binding.layoutPIN.eText_digit_4.text.toString()
    }

    private fun isCodeComplete(): Boolean {
        return  binding.layoutPIN.eText_digit_1.text.toString().isNotEmpty() &&
                binding.layoutPIN.eText_digit_2.text.toString().isNotEmpty() &&
                binding.layoutPIN.eText_digit_3.text.toString().isNotEmpty() &&
                binding.layoutPIN.eText_digit_4.text.toString().isNotEmpty()
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
                    if (viewModel.currentCodeStatus.value == SecureCreatePinViewModel.InsertCodeStatus.FIRST_CODE) {
                        viewModel.changeToSecondCode(getCode())
                    } else {
                        viewModel.verifyCode(getCode())
                    }


                }
            }

        }

        override fun afterTextChanged(s: Editable) {}
    }

}