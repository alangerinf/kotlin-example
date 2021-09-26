package com.chatowl.presentation.settings.myaccount.editfield

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.databinding.FragmentEditFieldBinding
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.fragments.ViewModelFragment
import com.chatowl.presentation.common.widgets.MyEditText
import kotlinx.android.synthetic.main.fragment_edit_field.*
import kotlinx.android.synthetic.main.fragment_journey.view.*
import kotlinx.android.synthetic.main.fragment_login_confirmation.*
import java.util.*

class EditFieldFragment : ViewModelFragment<EditFieldViewModel>(EditFieldViewModel::class.java) {

    val args: EditFieldFragmentArgs by navArgs()
    lateinit var  binding: FragmentEditFieldBinding

    @ExperimentalStdlibApi
    override fun getScreenName() = if (args.attribute.lowercase() === "email") "Edit email" else "Edit nickname"

    override fun setViewModel(): EditFieldViewModel {
        val application = ChatOwlApplication.get()
        val userPool = application.userPool
        return ViewModelProvider(this, EditFieldViewModel.Factory(application, userPool, args)).get(EditFieldViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_edit_field,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(viewModel)

        args.let {
                if (!it.isMultiline)  binding.fragmentEditFieldEditTextValue.setLines(1)
                if (it.maxSize>0) binding.fragmentEditFieldEditTextValue.setMaxSize(it.maxSize)
            }

        // TODO not triggering when keyboard opens
        fragment_edit_field_layout_main.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(bottom = padding.bottom + windowInsets.systemWindowInsetBottom)
        }

        fragment_edit_field_button_save.setOnClickListener {
            viewModel.onSaveClicked(fragment_edit_field_edit_text_value.text.toString())
        }

        fragment_edit_field_text_view_cancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private val textWatcher = object: TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            Handler(Looper.getMainLooper()).post {
                fragment_edit_field_edit_text_value?.text?.let {
                    val trimText = it.toString().trim()
                    if (it.isNotEmpty()) {
                        if(it.toString() != trimText) {
                            fragment_edit_field_edit_text_value.text = trimText
                        }
                    }
                }
            }
        }
    }

    override fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.fieldName.observe(viewLifecycleOwner, {
            fragment_edit_field_text_view_name.text = it.capitalize(Locale.ROOT)
        })
        viewModel.fieldValue.observe(viewLifecycleOwner, {
            fragment_edit_field_edit_text_value.removeTextChangedListener(textWatcher)
            fragment_edit_field_edit_text_value.text = it
            fragment_edit_field_edit_text_value.addTextChangedListener(textWatcher)
        })
        viewModel.fieldError.observe(viewLifecycleOwner, {
            it?.let {
                fragment_edit_field_edit_text_value.state = MyEditText.State.ERROR
                fragment_edit_field_edit_text_value.errorText = it
            } ?: run {
                fragment_edit_field_edit_text_value.state = MyEditText.State.NO_ERROR
            }
        })
        viewModel.systemMessage.observe(viewLifecycleOwner, {
            showSnackBarMessage(it)
        })
        viewModel.dismiss.observe(viewLifecycleOwner, { dismiss ->
            if(dismiss) {
                findNavController().navigateUp()
            }
        })
    }

    override fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.fieldName.removeObservers(viewLifecycleOwner)
        viewModel.fieldValue.removeObservers(viewLifecycleOwner)
        viewModel.systemMessage.removeObservers(viewLifecycleOwner)
        viewModel.dismiss.removeObservers(viewLifecycleOwner)
    }

}