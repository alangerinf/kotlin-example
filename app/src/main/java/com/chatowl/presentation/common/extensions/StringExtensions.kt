package com.chatowl.presentation.common.extensions

import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.*
import java.util.*

fun String.lowerCase(): String = this.toLowerCase(Locale.ROOT)

fun String.upperCase(): String = this.toUpperCase(Locale.ROOT)

fun String.capital(): String {
    return if (length > 1) {
        val first = this.first().toString().upperCase()
        val other = this.drop(1)
        "$first$other"
    } else this
}

fun String.getSpanString(
    size: Int? = null,
    color: Int? = null,
    typeface: Typeface? = null
): SpannableString {
    val spanValue = SpannableString(this)

    if (size != null)
        spanValue.setSpan(
            AbsoluteSizeSpan(size),
            0,
            this.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    if (color != null)
        spanValue.setSpan(
            ForegroundColorSpan(color),
            0,
            this.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    if (typeface != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        spanValue.setSpan(
            TypefaceSpan(typeface),
            0,
            this.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    return spanValue
}

fun String.spanString(span: String): SpannableString {
    val spannableString = SpannableString(this)
    val startIndex = spannableString.indexOf(span, ignoreCase = true)
    val endIndex = startIndex + span.length
    spannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        startIndex,
        endIndex,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spannableString
}

fun String.isTestAddress(): Boolean {
    return this.contains("@decemberlabs.com") ||
            this.contains("@chatowl.com") ||
            this.contains("@headstore.com")
}