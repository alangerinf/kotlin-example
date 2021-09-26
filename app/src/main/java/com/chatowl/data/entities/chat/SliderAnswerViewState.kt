package com.chatowl.data.entities.chat


data class SliderAnswerViewState(
        val title: String,
        val subTitle: String,
        val graduated: Boolean = false,
        val defaultChoiceStep: Int
)