package com.kash4me.data.models.staff


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StaffTransactionsResponse(
    @SerializedName("count")
    val count: Int? = 0,
    @SerializedName("next")
    val next: Any? = Any(),
    @SerializedName("previous")
    val previous: Any? = Any(),
    @SerializedName("results")
    val results: List<Result?>? = listOf()
) {
    @Keep
    data class Result(
        @SerializedName("amount_spent")
        val amountSpent: String? = "",
        @SerializedName("cashback_amount")
        val cashbackAmount: String? = "",
        @SerializedName("created_at")
        val createdAt: String? = "",
        @SerializedName("customer_details")
        val customerDetails: CustomerDetails? = CustomerDetails(),
        @SerializedName("id")
        val id: Int? = 0
    ) {
        @Keep
        data class CustomerDetails(
            @SerializedName("nick_name")
            val nickName: String? = "",
            @SerializedName("unique_id")
            val uniqueId: String? = ""
        )
    }
}