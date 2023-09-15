package com.kash4me.data.models.customer.annoucement


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AnnouncementsResponse(
    @SerializedName("count")
    val count: Int? = 0,
    @SerializedName("next")
    val next: String? = "",
    @SerializedName("previous")
    val previous: String? = "",
    @SerializedName("results")
    val results: List<Result?>? = listOf()
) {
    @Keep
    data class Result(
        @SerializedName("announced_at")
        val announcedAt: String? = "",
        @SerializedName("expire_on")
        val expireOn: String? = "",
        @SerializedName("merchant_id")
        val merchantId: Int? = null,
        @SerializedName("merchant_name")
        val merchantName: String? = "",
        @SerializedName("message")
        val message: String? = "",
        @SerializedName("notification_type")
        val notificationType: String? = ""
    )
}