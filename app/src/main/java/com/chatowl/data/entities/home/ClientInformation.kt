package com.chatowl.data.entities.home

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "client_information")
data class ClientInformation(
    @PrimaryKey(autoGenerate = false)
    val id: Long = System.currentTimeMillis(),
    @Embedded(prefix = "subscription_")
    val subscription: SubscriptionData = SubscriptionData(),
    @Embedded(prefix = "active_program_")
    var clientActiveProgramStatus: ActiveProgramStatus = ActiveProgramStatus(),
    var newMessages: Int = 0,
    @ColumnInfo(name = "days_until_next_session_due")
    var daysUntilNextSessionDue: Int = 0,
    var createdAt: String? = null,
    var nickname: String? = null
)

data class SubscriptionData(
    @ColumnInfo(name = "is_trial")
    var isTrial: Boolean = true,
    @ColumnInfo(name = "trial_ends_at")
    val trialEndsAt: String? = null
)

data class ActiveProgramStatus(
    @ColumnInfo(name = "assessment_done")
    var assessmentDone: Boolean = false,

    @ColumnInfo(name = "program_id")
    var programId: Int = 0,

    @ColumnInfo(name = "program_started")
    var programStarted: Boolean = false,

    @ColumnInfo(name = "program_url")
    var programUrl: String = ""
)