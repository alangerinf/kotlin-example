package com.chatowl.presentation.journal.edit.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chatowl.R
import com.chatowl.presentation.GlideApp
import com.chatowl.presentation.common.application.ChatOwlApplication
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import com.chatowl.presentation.common.fragments.ViewModelFragment
import kotlinx.android.synthetic.main.fragment_fullscreen_image.*

class FullscreenImageFragment :
    ViewModelFragment<FullscreenImageViewModel>(FullscreenImageViewModel::class.java) {

    override fun setViewModel(): FullscreenImageViewModel {
        val application = ChatOwlApplication.get()
        val args: FullscreenImageFragmentArgs by navArgs()
        return ViewModelProvider(this, FullscreenImageViewModel.Factory(application, args)).get(
            FullscreenImageViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fullscreen_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_fullscreen_image_layout_header.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        fragment_fullscreen_image_button_close.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun addObservers() {
        viewModel.imageUrl.observe(viewLifecycleOwner, { imageUrl ->
            GlideApp.with(this).load(imageUrl).into(fragment_fullscreen_image_image_view)
        })
    }

    override fun removeObservers() {
        viewModel.imageUrl.removeObservers(viewLifecycleOwner)
    }

}