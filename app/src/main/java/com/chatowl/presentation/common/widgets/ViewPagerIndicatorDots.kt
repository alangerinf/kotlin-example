package com.chatowl.presentation.common.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout

import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.chatowl.R

class ViewPagerIndicatorDots @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = -1, defStyleRes: Int = -1) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        @DrawableRes
        private val DRAWABLE_INDICATOR_SELECTED = R.drawable.indicator_dot_selected

        @DrawableRes
        private val DRAWABLE_INDICATOR_NOT_SELECTED = R.drawable.indicator_dot_unselected

        @DimenRes
        private val DEFAULT_INDICATOR_MARGIN = R.dimen.viewpager_indicator_margin
        private const val DEFAULT_INDICATOR_TINT = -1
    }

    private var dots: Array<ImageView>? = null
    private val iconSelected: Drawable?
    private val iconUnselected: Drawable?
    private var indicatorMargin: Int = 0
    private var indicatorColorTint: Int = 0

    private val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

                if (position == RecyclerView.NO_POSITION) {
                    return
                }

                selectAt(position)
            }
        }
    }

    init {

        orientation = HORIZONTAL

        iconSelected = AppCompatResources.getDrawable(context, DRAWABLE_INDICATOR_SELECTED)
        iconUnselected = AppCompatResources.getDrawable(context, DRAWABLE_INDICATOR_NOT_SELECTED)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator, defStyleAttr, 0)

            indicatorMargin = a.getDimensionPixelSize(R.styleable.ViewPagerIndicator_vpiIndicatorMargin, resources.getDimensionPixelSize(
                    DEFAULT_INDICATOR_MARGIN
            ))
            indicatorColorTint = a.getColor(R.styleable.ViewPagerIndicator_vpiIndicatorTint, ContextCompat.getColor(context, R.color.white))

            a.recycle()
        }

        if (indicatorColorTint != DEFAULT_INDICATOR_TINT) {
            iconSelected!!.mutate().setTint(indicatorColorTint)
        }
    }

    fun setUpWithViewPager2(viewPager2: ViewPager2, isLoop: Boolean = false) {
        val adapter = viewPager2.adapter
                ?: throw IllegalArgumentException("PagerAdapter cannot be null.")

        if (adapter.itemCount <= 1) {
            visibility = View.INVISIBLE
            return
        } else {
            visibility = View.VISIBLE
        }

        val dotCount = if (isLoop) adapter.itemCount - 1 else adapter.itemCount
        addDots(dotCount)

        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val actualPosition = if(isLoop && position == adapter.itemCount - 1) 0 else position
                selectAt(actualPosition)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        }
        viewPager2.registerOnPageChangeCallback(pageChangeCallback)
        pageChangeCallback.onPageSelected(0) // Necessary to trigger listener. setCurrentItem(0) wouldn't work.
    }

    private fun addDots(itemCount: Int) {
        removeAllViews()

        dots = (0 until itemCount).map {
            val imageView = ImageView(context)
            val params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            params.marginEnd = indicatorMargin
            params.marginStart = indicatorMargin
            imageView.layoutParams = params
            addView(imageView)
            imageView
        }.toTypedArray()
    }

    fun selectAt(position: Int) {
        if (dots == null) {
            return
        }

        for (i in dots!!.indices) {
            val dot = dots!![i]
            if (i == position) {
                dot.setImageDrawable(iconSelected)
            } else {
                dot.setImageDrawable(iconUnselected)
            }
        }
    }

}
