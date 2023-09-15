package com.kash4me.data.models.merchant.announcement.create_or_update


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreateOrUpdateAnnouncementResponse(
    @SerializedName("detail")
    val detail: String? = ""
)