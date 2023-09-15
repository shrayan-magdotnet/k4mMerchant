package com.kash4me.data.models

import com.google.gson.annotations.SerializedName

data class CustomerListResponse(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<CustomerDetails>
)

data class CustomerDetails(
    val amount_left: String? = "",
    val amount_spent: String? = "",
    val cashback_amount: String? = "",
    @SerializedName("processing_amount")
    val processingAmount: String? = "",
    @SerializedName("goal_amount")
    val goalAmount: String? = "",
    @SerializedName("progress_percent")
    val progressPercent: String? = "",
    val created_at: String?,
//    val id: Int,
    val user_details: UserDetails?
)