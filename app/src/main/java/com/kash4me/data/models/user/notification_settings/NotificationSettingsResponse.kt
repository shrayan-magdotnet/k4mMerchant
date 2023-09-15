package com.kash4me.data.models.user.notification_settings


import com.google.gson.annotations.SerializedName

data class NotificationSettingsResponse(
    @SerializedName("email_settings")
    val emailSettings: Boolean? = false
)