package com.chatowl.presentation.common.activities

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.chatowl.R
import com.chatowl.presentation.common.utils.SnackBarAction

abstract class BaseActivity : AppCompatActivity() {

    private val rootView: View by lazy { findViewById(android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    fun showSnackBarMessage(message: String, action: SnackBarAction? = null) {
        val snackBar = Snackbar.make(
            rootView,
            message,
            Snackbar.LENGTH_LONG
        )

        val snackBarView = snackBar.view

        val snackBarTextView = snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackBarTextView.maxLines = 4
        snackBarTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBarTextView.typeface = Typeface.create("sans-serif", Typeface.NORMAL)

        action?.let {
            val snackBarAction =
                snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
            snackBarAction.typeface = Typeface.create("sans-serif", Typeface.BOLD)

            snackBar.setAction(it.actionText, it.actionClickListener)
        }

        snackBar.show()
    }

    fun openKeyboard(v: View?) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }

    fun closeKeyboard(v: View?) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v?.windowToken, 0)
    }

}