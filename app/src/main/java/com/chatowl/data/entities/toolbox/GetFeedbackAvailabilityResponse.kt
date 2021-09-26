package com.chatowl.data.entities.toolbox

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetFeedbackAvailabilityResponse(
    var showFeedback: Boolean
) : Parcelable