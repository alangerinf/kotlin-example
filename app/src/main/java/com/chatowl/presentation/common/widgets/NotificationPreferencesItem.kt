package com.chatowl.presentation.common.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chatowl.R
import kotlinx.android.synthetic.main.layout_app_preferences_item.view.*
import kotlinx.android.synthetic.main.layout_notification_preferences_item.view.*
import kotlinx.android.synthetic.main.layout_settings_item.view.*
import kotlinx.android.synthetic.main.layout_settings_item.view.layout_settings_item_text_view_name

class NotificationPreferencesItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var onCheckedListener: ((isChecked: Boolean) -> Unit)? = null

    var isChecked: Boolean = false
        set(value) {
            if (field != value) {
                field = value
            }
            layout_notification_preferences_item_switch.isChecked = value
        }

    var name: String = ""
        set(value) {
            field = value
            layout_notification_preferences_item_text_view_name.text = value
        }

    init {
        View.inflate(context, R.layout.layout_notification_preferences_item, this)

        val style = context.obtainStyledAttributes(attrs, R.styleable.NotificationPreferencesItem)
        val styleName = style.getString(R.styleable.NotificationPreferencesItem_notificationName)
        style.recycle()

        name = styleName ?: ""

        layout_notification_preferences_item_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                onCheckedListener?.invoke(isChecked)
            }
        }
    }

    fun setOnCheckedChangeListener(callback: (Boolean) -> Unit) {
        onCheckedListener = callback
    }

}