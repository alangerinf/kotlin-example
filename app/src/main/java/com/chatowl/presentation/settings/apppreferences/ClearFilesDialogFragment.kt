package com.chatowl.presentation.settings.apppreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.activities.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_clear_files.*


class ClearFilesDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: ClearFilesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.ChatOwlAppTheme)
        return inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_clear_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(ClearFilesViewModel::class.java)

        dialog_clear_files_button_yes.setOnClickListener {
            viewModel.onConfirmClicked()
        }

        dialog_clear_files_text_view_cancel.setOnClickListener {
            dismiss()
        }

        addObservers()
    }

    private fun addObservers() {
        viewModel.navigate.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })
        viewModel.success.observe(viewLifecycleOwner, { success ->
            if(!success) {
                (activity as? BaseActivity)?.showSnackBarMessage(getString(R.string.error_deleting_files))
            }
            dismiss()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }

    private fun removeObservers() {
        viewModel.navigate.removeObservers(viewLifecycleOwner)
        viewModel.success.removeObservers(viewLifecycleOwner)
    }
}
