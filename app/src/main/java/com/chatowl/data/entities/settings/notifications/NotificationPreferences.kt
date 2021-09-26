package com.chatowl.data.entities.settings.notifications

data class NotificationPreferences(
    val sessionDue: Boolean,
    val exerciseDue: Boolean,
    val newExerciseInToolbox: Boolean,
    val imageOfTheDay: Boolean,
    val quoteOfTheDay: Boolean,
    val messageFromTherapist: Boolean
)