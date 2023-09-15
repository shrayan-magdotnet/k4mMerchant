package com.kash4me.data.models.merchant.transaction_by_time


import com.google.gson.annotations.SerializedName

data class ViewTransactionByTimeResponse(
    @SerializedName("count")
    val count: Int? = 0,
    @SerializedName("next")
    val next: String? = "",
    @SerializedName("previous")
    val previous: Any? = Any(),
    @SerializedName("results")
    val results: List<Result?>? = listOf()
) {
    data class Result(
        @SerializedName("amount_spent")
        val amountSpent: String? = "",
        @SerializedName("assigned_by")
        val assignedBy: String? = "",
        @SerializedName("cashback_amount")
        val cashbackAmount: String? = "",
        @SerializedName("customer_details")
        val customerDetails: CustomerDetails? = CustomerDetails(),
        @SerializedName("date")
        val date: String? = "",
        @SerializedName("id")
        val id: Int? = 0,
        @SerializedName("time")
        val time: String? = ""
    ) {
        data class CustomerDetails(
            @SerializedName("id")
            val id: Int? = 0,
            @SerializedName("nick_name")
            val nickName: String? = "",
            @SerializedName("unique_id")
            val uniqueId: String? = ""
        )
    }
}