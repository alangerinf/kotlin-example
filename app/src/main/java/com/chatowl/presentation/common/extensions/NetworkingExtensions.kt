package com.chatowl.presentation.common.extensions

import retrofit2.Response

/**
 * Method to obtain the body result of a Response if it is successful (CODE = 200)
 * Otherwise (CODE != 200 or Failure) throw an Exception
 */
fun <T> Response<T>.obtain(): T {
    return if (isSuccessful && body() != null) body()!! else throw Exception(errorBody()?.string())
}
