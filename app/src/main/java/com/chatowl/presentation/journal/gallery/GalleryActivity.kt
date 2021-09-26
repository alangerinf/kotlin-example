package com.chatowl.presentation.journal.gallery

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.ViewModelProvider
import com.chatowl.R
import com.chatowl.presentation.common.activities.BaseActivity
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : BaseActivity() {

    lateinit var viewModel: GalleryViewModel

    companion object {
        const val GALLERY_CONTENT_KEY = "GALLERY_CONTENT"
    }

    private val adapter = GalleryItemAdapter { item ->
        val intent = intent
        intent.putExtra(GALLERY_CONTENT_KEY, item)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        viewModel  = ViewModelProvider(this).get(GalleryViewModel::class.java)

        activity_gallery_recyclerview.doOnApplyWindowInsets { v, windowInsets, padding ->
            v.updatePaddingRelative(bottom = padding.bottom + windowInsets.systemWindowInsetBottom)
        }

        activity_gallery_recyclerview.adapter = adapter

        addObservers()
    }

    private fun addObservers() {
        viewModel.isLoading.observe(this, { isLoading ->
            activity_gallery_progress_bar.visibility = if(isLoading) View.VISIBLE else View.GONE
        })
        viewModel.content.observe(this, {
            adapter.submitList(it)
        })
    }
}