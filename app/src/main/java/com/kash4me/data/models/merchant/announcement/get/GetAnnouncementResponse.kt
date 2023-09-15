package com.kash4me.data.models.merchant.announcement.get


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GetAnnouncementResponse(
    @SerializedName("announced_at")
    val announcedAt: String? = "",
    @SerializedName("expire_on")
    val expireOn: String? = "",
    @SerializedName("message")
    val message: String? = ""
)