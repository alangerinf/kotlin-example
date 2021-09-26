package com.chatowl.presentation.common.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val LAUNCH_DISPATCHER = Dispatchers.IO
private val POST_DISPATCHER = Dispatchers.Main

fun <T> CoroutineScope.fetch(call: suspend () -> T, onSuccess: (T) -> Unit, onFailure: (Exception) -> Unit) {
    launch(LAUNCH_DISPATCHER) {
        try {
            val response = call.invoke()
            withContext(POST_DISPATCHER) {
                onSuccess.invoke(response)
            }
        } catch (e: Exception) {
            withContext(POST_DISPATCHER) {
                onFailure.invoke(e)
            }
        }
    }
}