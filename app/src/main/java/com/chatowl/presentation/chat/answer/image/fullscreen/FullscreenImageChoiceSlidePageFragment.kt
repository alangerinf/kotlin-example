package com.chatowl.presentation.chat.answer.image.fullscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chatowl.R
import com.chatowl.presentation.GlideApp
import kotlinx.android.synthetic.main.fragment_fullscreen_image_choice_slide_page.*

class FullscreenImageChoiceSlidePageFragment : Fragment() {

    companion object {
        const val IMAGE_URL_KEY = "imageUrl"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_fullscreen_image_choice_slide_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(IMAGE_URL_KEY) }?.apply {
            GlideApp.with(requireContext())
                    .load(getString(IMAGE_URL_KEY))
                    .into(fragment_fullscreen_image_choice_slide_page_image_view)
        }
    }
}
