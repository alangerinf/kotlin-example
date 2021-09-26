package com.chatowl.presentation.common.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class OffsetsItemDecoration(
    private val leftOffset: Int = 0,
    private val topOffset: Int = 0,
    private val rightOffset: Int = 0,
    private val bottomOffset: Int = 0,
    private val applyToFirst: Boolean = true,
    private val applyToLast: Boolean = true,
    private val applyToFirstExclusive: Boolean = false,
    private val applyToLastExclusive: Boolean = false
): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount

        if (itemPosition == 0 && !applyToFirst && !applyToLastExclusive) return
        if (itemPosition != 0 && applyToFirstExclusive) return

        if (itemCount == (itemPosition + 1) && !applyToLast && !applyToLastExclusive) return
        if (itemPosition != (itemPosition + 1) && applyToLastExclusive) return

        with(outRect) {
            left = leftOffset
            top = topOffset
            right = rightOffset
            bottom = bottomOffset
        }
    }
}