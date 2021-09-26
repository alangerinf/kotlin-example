package com.chatowl.data.entities.toolbox

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExerciseLockedMessage(
    var title: String,
    var body: String,
    var exerciseName: String,
    var exerciseIconResourceId: Int,
    var isBlockedBySubscription: Boolean
) : Parcelable