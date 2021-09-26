package com.chatowl.data.entities.settings.emails

data class EmailPreferences (
    val sessionDue: Boolean,
    val exerciseDue: Boolean,
    val newExerciseInToolbox: Boolean,
    val imageOfTheDay: Boolean,
    val quoteOfTheDay: Boolean,
    val messageFromTherapist: Boolean
)