package com.chatowl.presentation.common.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.chatowl.R
import java.lang.Integer.MAX_VALUE

class MyEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class State {
        DEFAULT, ERROR, NO_ERROR
    }

    private val editTextContainer = LinearLayout(ContextThemeWrapper(context, R.style.ChatOwlEditTextContainer))
    private val editText = AppCompatEditText(ContextThemeWrapper(context, R.style.ChatOwlMyEditTextStyle), attrs, defStyleAttr)
    private val errorTextView = AppCompatTextView(ContextThemeWrapper(context, R.style.ChatOwlEditTextError))
    private val backgroundDrawable: Drawable

    fun getEditText() = editText

    var text: CharSequence?
        get() = editText.text
        set(value) {
            Handler(Looper.getMainLooper()).post {
                editText.setText(value)
                editText.setSelection(editText.length())
            }
        }

    var errorText: CharSequence?
        get() = errorTextView.text
        set(value) {
            errorTextView.text = value
            errorTextView.compoundDrawablePadding = 5
            errorTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close_small, 0, 0, 0);
        }

    var state: State = State.DEFAULT
        set(value) {
            field = value
            when (value) {
                State.DEFAULT -> {
                    errorTextView.visibility = View.GONE
                    editTextContainer.background = backgroundDrawable
                }
                State.ERROR -> {
                    errorTextView.visibility = View.VISIBLE
                    editTextContainer.setBackgroundResource(R.drawable.background_edit_text_error)
                }
                State.NO_ERROR -> {
                    errorTextView.visibility = View.INVISIBLE
                    editTextContainer.background = backgroundDrawable
                }
            }
        }

    init {
        orientation = VERTICAL

        editTextContainer.orientation = HORIZONTAL

        val values = context.obtainStyledAttributes(attrs, R.styleable.MyEditText)
        val backgroundDrawableRes =
            values.getResourceId(R.styleable.MyEditText_android_background, -1)

        backgroundDrawable = if (backgroundDrawableRes != -1) {
            ContextCompat.getDrawable(context, backgroundDrawableRes)!!
        } else {
            ContextCompat.getDrawable(context, R.drawable.background_edit_text_default)!!
        }

        val paddingDp = resources.getDimension(R.dimen.padding_8dp).div(resources.displayMetrics.density)
        editText.setPadding(paddingDp.toInt())

        editTextContainer.addView(editText, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))

        addView(editTextContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(errorTextView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        state = if (values.getBoolean(R.styleable.MyEditText_metErrorTextGone, true)) State.DEFAULT else State.NO_ERROR

        values.recycle()
    }

    fun setOnEditorActionListener(onEditorActionListener: TextView.OnEditorActionListener?) {
        editText.setOnEditorActionListener(onEditorActionListener)
        editText.setOnFocusChangeListener { _, _ ->  }
    }

    fun requestEditTextFocus() {
        editText.requestFocus()
    }

    override fun setOnFocusChangeListener(onFocusChangeListener: OnFocusChangeListener?) {
        editText.onFocusChangeListener = onFocusChangeListener
    }

    override fun isEnabled(): Boolean {
        return editText.isEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        editText.isEnabled = enabled
    }

    override fun setFocusable(focusable: Boolean) {
        super.setFocusable(focusable)
        editText.isFocusable = focusable
    }

    override fun setFocusableInTouchMode(focusableInTouchMode: Boolean) {
        super.setFocusableInTouchMode(focusableInTouchMode)
        editText.isFocusableInTouchMode = focusableInTouchMode
    }

    fun addTextChangedListener(watcher: TextWatcher) {
        editText.addTextChangedListener(watcher)
    }

    fun removeTextChangedListener(watcher: TextWatcher) {
        editText.removeTextChangedListener(watcher)
    }

    fun setInputType(inputType: Int) {
        editText.inputType = inputType
    }

    fun setHint(hint: String) {
        editText.hint = hint
    }

    fun setLines(lines: Int) {
        if (lines == 1) {
            editText.setSingleLine()
        }
        editText.maxLines = lines
    }

    fun setMaxSize(size: Int) {
        val filters = editText.filters.toMutableList()
        filters.add(InputFilter.LengthFilter(size))
        editText.filters = filters.toTypedArray()
    }
}