package com.chatowl.presentation.chat.answer.image.fullscreen

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.chatowl.R
import com.chatowl.data.entities.chat.BotChoice
import com.chatowl.presentation.chat.answer.image.fullscreen.FullscreenImageChoiceContract.Companion.FULLSCREEN_IMAGE_URL_LIST_KEY
import com.chatowl.presentation.common.activities.BaseActivity
import kotlinx.android.synthetic.main.activity_fullscreen_image_choice.*
import java.util.ArrayList


class FullscreenImageChoiceActivity : BaseActivity() {

    private var numPages = 0
    lateinit var list: ArrayList<BotChoice>

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image_choice)

        list = intent?.getParcelableArrayListExtra(FULLSCREEN_IMAGE_URL_LIST_KEY) ?: arrayListOf()
        numPages = list.size

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = activity_fullscreen_image_choice_view_pager
        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        activity_fullscreen_image_choice_view_pager_indicator.setUpWithViewPager2(viewPager)

        activity_fullscreen_image_choice_button_select.setOnClickListener {
            val selectedItemIndex = viewPager.currentItem.coerceIn(0, list.size - 1)
            val selectedItem = list[selectedItemIndex]
            finishWithChoice(selectedItem)
        }

        activity_fullscreen_image_choice_image_view_close.setOnClickListener {
            finish()
        }
    }

    private fun finishWithChoice(choice: BotChoice) {
        val intent = Intent()
        intent.putExtra(FullscreenImageChoiceContract.SELECTED_FULLSCREEN_IMAGE_URL_KEY, choice)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * A simple pager adapter that represents FeatureOnboardingSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = numPages

        override fun createFragment(position: Int): Fragment {
            val fragment = FullscreenImageChoiceSlidePageFragment()
            fragment.arguments = Bundle().apply {
                putString(
                    FullscreenImageChoiceSlidePageFragment.IMAGE_URL_KEY,
                    list[position].fullscreenUrl
                )
            }
            return fragment
        }
    }
}