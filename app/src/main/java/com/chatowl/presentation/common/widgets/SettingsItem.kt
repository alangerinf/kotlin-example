package com.chatowl.presentation.common.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chatowl.R
import kotlinx.android.synthetic.main.layout_settings_item.view.*

class SettingsItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var name: String = ""
        set(value) {
            field = value
            layout_settings_item_text_view_name.text = value
        }

    private var iconStartDrawable: Drawable? = null
        set(value) {
            field = value
            layout_settings_item_image_view_icon_start.setImageDrawable(iconStartDrawable)
        }

    private var showDisclosure: Boolean = true
        set(value) {
            field = value
            layout_settings_item_image_view_icon_end.visibility = if(value) View.VISIBLE else View.GONE
        }

    init {
        View.inflate(context, R.layout.layout_settings_item, this)

        val style = context.obtainStyledAttributes(attrs, R.styleable.SettingsItem)
        val styleName = style.getString(R.styleable.SettingsItem_itemName)
        val styleIconSrc = style.getResourceId(R.styleable.SettingsItem_android_src, -1)
        val styleShowDisclosure = style.getBoolean(R.styleable.SettingsItem_showSettingsDisclosure, true)
        style.recycle()

        name = styleName ?: ""
        iconStartDrawable = ContextCompat.getDrawable(context, styleIconSrc)
        showDisclosure = styleShowDisclosure
    }

}