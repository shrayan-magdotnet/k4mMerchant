package com.kash4me.data.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CustomerCashBackDetailsResponse(
    @SerializedName("count")
    val count: Int?,
    @SerializedName("next")
    val next: Any?,
    @SerializedName("previous")
    val previous: Any?,
    @SerializedName("results")
    val results: List<CashBackList>?,
    @SerializedName("total_processing")
    val totalProcessing: String?
)

@Keep
data class CashBackList(
    @SerializedName("amount_left")
    val amount_left: String?,
    @SerializedName("amount_spent")
    val amount_spent: String?,
    @SerializedName("cashback_amount")
    val cashback_amount: String?,
    @SerializedName("goal_amount")
    val goalAmount: String?,
    @SerializedName("processing_amount")
    val processing_amount: String?,
    @SerializedName("progress_percent")
    val progressPercent: String?,
    @SerializedName("shop_details")
    val shop_details: ShopDetails?
//    val txn_id: Int
)

@Keep
data class ShopDetails(
    @SerializedName("address")
    val address: String?,
    @SerializedName("logo")
    val logo: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("shop_id")
    val shop_id: Int?
)