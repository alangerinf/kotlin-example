package com.chatowl.presentation.common.extensions

import android.graphics.Bitmap
import android.os.Build
import java.io.ByteArrayOutputStream
import java.util.*

fun Bitmap.toBase64(): String {
    // Bitmap to byte array
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()

    val base64Str = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Base64.getEncoder().encodeToString(byteArray)
    } else {
        android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }

    return base64Str //"data:image/jpeg;base64,$base64Str"
}