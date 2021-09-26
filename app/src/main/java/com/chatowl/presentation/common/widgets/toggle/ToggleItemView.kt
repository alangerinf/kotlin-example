package com.chatowl.presentation.common.widgets.toggle

import androidx.appcompat.widget.AppCompatTextView

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import com.chatowl.R

class ToggleItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(ContextThemeWrapper(context, R.style.TabItemView), attrs, defStyleAttr)