package com.kash4me.data.models.customer.claim_coupon


import com.google.gson.annotations.SerializedName

data class ClaimCouponRequest(
    @SerializedName("coupon")
    val coupon: String? = ""
)