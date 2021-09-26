package com.chatowl.presentation.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.chatowl.R
import kotlinx.android.synthetic.main.layout_my_account_item.view.*

class MyAccountItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var name: String = ""
        set(value) {
            field = value
            layout_my_account_item_text_view_name.text = value
        }

    var value: String = ""
        set(value) {
            field = value
            layout_my_account_item_text_view_value.text = value
        }
    var editable: Boolean = true
        set(value){
            field = value
            when{
                value -> {
                    layout_my_account_item_text_view_edit.visibility = View.VISIBLE
                }
                else -> {
                    layout_my_account_item_text_view_edit.visibility = View.GONE
                }
            }
        }
    init {
        View.inflate(context, R.layout.layout_my_account_item, this)

        val style = context.obtainStyledAttributes(attrs, R.styleable.MyAccountItem)
        val styleName = style.getString(R.styleable.MyAccountItem_name)
        val styleValue = style.getString(R.styleable.MyAccountItem_value)
        val styleEditable = style.getBoolean(R.styleable.MyAccountItem_editable, true)
        style.recycle()

        name = styleName ?: ""
        value = styleValue ?: ""
        editable = styleEditable
    }

    override fun setOnClickListener(l: OnClickListener?) {
        layout_my_account_item_root.setOnClickListener(l)
    }

}