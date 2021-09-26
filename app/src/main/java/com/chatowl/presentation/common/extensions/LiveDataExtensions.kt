package com.chatowl.presentation.common.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T?, K?) -> R
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        result.value = block(this.value, liveData.value)
    }
    result.addSource(liveData) {
        result.value = block(this.value, liveData.value)
    }
    return result
}

fun <T, V, R> LiveData<T>.combineWithNotNull(
    liveData: LiveData<V>,
    block: (T, V) -> R
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        this.value?.let { first ->
            liveData.value?.let { second ->
                result.value = block(first, second)
            }
        }
    }
    result.addSource(liveData) {
        this.value?.let { first ->
            liveData.value?.let { second ->
                result.value = block(first, second)
            }
        }
    }

    return result
}