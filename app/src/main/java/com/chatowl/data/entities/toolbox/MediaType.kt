package com.chatowl.data.entities.toolbox

import com.chatowl.R

enum class ToolType {
    SESSION,
    EXERCISE,
    INTERACTIVE,
    QUOTE,
    IMAGE
}

enum class ToolSubtype(val drawableId: Int, val toolboxDrawableId: Int) {
    SESSION(R.drawable.ic_session, R.drawable.ic_session_toolbox),
    QUOTE(R.drawable.ic_quote, R.drawable.ic_quote_toolbox),
    IMAGE(R.drawable.ic_image, R.drawable.ic_image_toolbox),
    AUDIO(R.drawable.ic_audio, R.drawable.ic_audio_toolbox),
    VIDEO(R.drawable.ic_video, R.drawable.ic_video_toolbox),
    `MOOD-METER`(R.drawable.ic_mood_meter, R.drawable.ic_mood_toolbox),
    JOURNAL(R.drawable.ic_journal, R.drawable.ic_journal_toolbox),
    `GUIDED-JOURNAL`(R.drawable.ic_guided_journal, R.drawable.ic_guided_journal_toolbox),
    `MESSAGE-COUNSELOR`(R.drawable.ic_message_counselor, R.drawable.ic_message_counselor_toolbox),
    `ONE-MINUTE-BREATHING`(R.drawable.ic_one_minute_breathing, R.drawable.ic_one_minute_breathing_toolbox);
}

enum class MediaType (val typeDrawableResourceId: Int, val fileExtension: String) {
    VIDEO(R.drawable.ic_media_type_video, ".mp4"),
    AUDIO(R.drawable.ic_play_outlined, ".mp3")
}