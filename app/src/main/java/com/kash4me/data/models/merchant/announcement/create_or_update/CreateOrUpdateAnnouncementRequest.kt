package com.kash4me.data.models.merchant.announcement.create_or_update


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreateOrUpdateAnnouncementRequest(
    @SerializedName("end_date")
    val endDate: String? = "",
    @SerializedName("message")
    val message: String? = "",
    @SerializedName("start_date")
    val startDate: String? = ""
)