package com.kash4me.data.models.user.fee_settings


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FeeSettingsResponse(
    @SerializedName("commission_percentage")
    val commissionPercentage: String? = "",
    @SerializedName("customer_fee")
    val customerFee: String? = "",
    @SerializedName("merchant_fee")
    val merchantFee: String? = ""
)