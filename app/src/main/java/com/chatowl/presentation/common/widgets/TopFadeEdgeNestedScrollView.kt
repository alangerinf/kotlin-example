package com.chatowl.presentation.common.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class TopFadeEdgeNestedScrollView : NestedScrollView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun getBottomFadingEdgeStrength(): Float {
        return 0.0f
    }
}