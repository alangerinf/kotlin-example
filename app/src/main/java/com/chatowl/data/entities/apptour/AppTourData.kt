package com.chatowl.data.entities.apptour

import android.os.Parcelable
import com.chatowl.data.entities.chat.ChatMessage
import com.chatowl.data.entities.chat.Question
import com.chatowl.data.entities.journey.GetJourneyResponse
import com.chatowl.data.entities.journey.Journey
import com.chatowl.data.entities.plan.Program
import com.chatowl.data.entities.toolbox.home.ToolboxHome
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
class AppTourData(
    val welcome : Welcome,
    val tour : List<ChatMessage>,
    val plan : Program,
    //val plans : List<Plans>,
    val toolbox : ToolboxHome,
    val chat : List<ChatMessage>,
    val journey : GetJourneyResponse
): Parcelable{
}

@Parcelize
data class Welcome (
    val firstLineTitle : String,
    val secondLineTitle : String,
    val nickname : String,
    val subTitle : String,
    val firstButtonText : String,
    val secondButtonText : String
):Parcelable

@Parcelize
data class Tour (

    val id : String,
    val sender : String,
    val messageType : String,
    val timestamp : String,
    val text : String? = null,
    val subText : String? = null,
    val botStepId : Int? = null,
    val toolId : String? = null,
    val order : Int? = null,
    val question : @RawValue Question?

):Parcelable
