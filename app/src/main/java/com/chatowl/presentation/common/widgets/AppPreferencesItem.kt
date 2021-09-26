package com.chatowl.presentation.common.widgets

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chatowl.R
import kotlinx.android.synthetic.main.layout_app_preferences_item.view.*

class AppPreferencesItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var onCheckedListener: ((isChecked: Boolean) -> Unit)? = null

    var name: String = ""
        set(value) {
            field = value
            layout_app_preferences_item_text_view_name.text = value
        }

    var description: String = ""
        set(value) {
            field = value
            layout_app_preferences_item_text_view_description.text = value

        }

    var showDisclosure: Boolean = false
        set(value) {
            field = value
            layout_app_preferences_item_image_view_icon_end.visibility =
                if (value) View.VISIBLE else View.GONE
        }

    var isChecked: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                layout_app_preferences_item_switch.isChecked = value
            }
        }

    private var showSwitch: Boolean = false
        set(value) {
            field = value
            layout_app_preferences_item_switch.visibility = if (value) View.VISIBLE else View.GONE
        }

    private var mediumFont: Boolean = false
        set(value) {
            field = value
            val typeface = if(value) {
                Typeface.create("sans-serif-medium", Typeface.NORMAL)
            } else {
                Typeface.create("sans-serif", Typeface.NORMAL)
            }
            layout_app_preferences_item_text_view_name.typeface = typeface
        }

    init {
        View.inflate(context, R.layout.layout_app_preferences_item, this)

        val style = context.obtainStyledAttributes(attrs, R.styleable.AppPreferencesItem)
        val preferenceName = style.getString(R.styleable.AppPreferencesItem_preferenceName)
        val preferenceDescription =
            style.getString(R.styleable.AppPreferencesItem_preferenceDescription)
        val showDisclosure = style.getBoolean(R.styleable.AppPreferencesItem_showDisclosure, false)
        val showSwitch = style.getBoolean(R.styleable.AppPreferencesItem_showSwitch, false)
        val mediumFont = style.getBoolean(R.styleable.AppPreferencesItem_mediumFont, false)
        val textColor = style.getResourceId(R.styleable.AppPreferencesItem_android_textColor, -1)
        style.recycle()

        name = preferenceName ?: ""

        description = preferenceDescription ?: ""

        this.showDisclosure = showDisclosure

        this.showSwitch = showSwitch

        this.mediumFont = mediumFont

        layout_app_preferences_item_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                onCheckedListener?.invoke(isChecked)
            }
        }

        if(textColor != -1) {
            layout_app_preferences_item_text_view_name.setTextColor(ContextCompat.getColor(context, textColor))
        }

    }

    fun setOnCheckedChangeListener(callback: (Boolean) -> Unit) {
        onCheckedListener = callback
        layout_app_preferences_item_switch.visibility = View.VISIBLE
    }

}