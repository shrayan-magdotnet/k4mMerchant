package com.kash4me.data.models.staff


import com.google.gson.annotations.SerializedName

data class MyTransactionResponse(
    @SerializedName("count")
    val count: Int? = 0,
    @SerializedName("next")
    val next: String? = "",
    @SerializedName("previous")
    val previous: String? = "",
    @SerializedName("results")
    val results: List<Result?>? = listOf()
) {
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
        data class CustomerDetails(
            @SerializedName("id")
            val id: Int? = 0,
            @SerializedName("nick_name")
            val nickName: String? = ""
        )
    }
}