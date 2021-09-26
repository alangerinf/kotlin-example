package com.chatowl.data.entities.chat

data class AnswerViewState(
        var toggleCustomAnswerControls: Boolean = false,
        var textAnswerViewState: TextAnswerViewState? = null,
        var imageAnswerViewState: ImageAnswerViewState? = null,
        var sliderAnswerViewState: SliderAnswerViewState? = null,
        var botChoices: List<BotChoice>? = null
)