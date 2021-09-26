package com.chatowl.presentation.common.extensions

fun <T> concatenate(vararg lists: List<T>): List<T> {
    return listOf(*lists).flatten()
}