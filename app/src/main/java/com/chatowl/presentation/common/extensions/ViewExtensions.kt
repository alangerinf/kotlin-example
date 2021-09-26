package com.chatowl.presentation.common.extensions

import android.view.View
import android.view.WindowInsets
import android.widget.CompoundButton

fun View.doOnApplyWindowInsets(f: (view: View, windowInsets: WindowInsets, padding: InitialPadding) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    setOnApplyWindowInsetsListener { v, insets ->
        f(v, insets, initialPadding)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

fun View.customDisable() {
    this.alpha = 0.4f
    this.isClickable = false
}

fun View.customEnable() {
    this.alpha = 1f
    this.isClickable = true
}

data class InitialPadding(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

fun CompoundButton.setOnUserCheckedChangeListener(callback: (isChecked: Boolean) -> Unit) {
    setOnCheckedChangeListener { buttonView, isChecked ->
        if (buttonView.isPressed) {
            callback.invoke(isChecked)
        }
    }
}