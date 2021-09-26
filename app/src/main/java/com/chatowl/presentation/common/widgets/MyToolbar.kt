package com.chatowl.presentation.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePaddingRelative
import androidx.navigation.findNavController
import com.chatowl.R
import com.chatowl.presentation.common.extensions.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.my_toolbar.view.*

class MyToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.my_toolbar, this)
        doOnApplyWindowInsets { view, windowInsets, padding ->
            view.updatePaddingRelative(top = padding.top + windowInsets.systemWindowInsetTop)
        }

        val values = context.obtainStyledAttributes(attrs, R.styleable.MyToolbar)
        val title = values.getString(R.styleable.MyToolbar_toolbarTitle)
        val isBackEnabled = values.getBoolean(R.styleable.MyToolbar_isBackEnabled, false)
        val showDivider = values.getBoolean(R.styleable.MyToolbar_showDivider, true)
        val lightMode = values.getBoolean(R.styleable.MyToolbar_light, false)
        values.recycle()

        if(lightMode) {
            my_toolbar_title_text_view.setTextColor(ContextCompat.getColor(context, R.color.white))
            my_toolbar_button_back.imageTintList = ContextCompat.getColorStateList(context, R.color.selector_white)
        }

        my_toolbar_title_text_view.text = title

        my_toolbar_view_divider.visibility = if(showDivider) View.VISIBLE else View.GONE

        if (isBackEnabled) {
            my_toolbar_button_back.visibility = View.VISIBLE
            my_toolbar_button_back.setOnClickListener {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(it.windowToken, 0)
                findNavController().navigateUp()
            }
        }
    }

    fun enableSecondaryAction(drawableResourceId: Int, action: () -> Unit) {
        my_toolbar_button_secondary.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                drawableResourceId
            )
        )
        my_toolbar_button_secondary.setOnClickListener { action.invoke() }
        my_toolbar_button_secondary.visibility = View.VISIBLE
    }

    fun showSecondaryAction() {
        my_toolbar_button_secondary.visibility = View.VISIBLE
    }

    fun hideSecondaryAction() {
        my_toolbar_button_secondary.visibility = View.INVISIBLE
    }

    fun setTitle(title: String) {
        my_toolbar_title_text_view.text = title
    }

}