package com.kash4me.data.models.customer.response


import com.google.gson.annotations.SerializedName

data class CustomerTransactionsResponse(
    @SerializedName("count")
    val count: Int? = 0,
    @SerializedName("next")
    val next: String? = "",
    @SerializedName("previous")
    val previous: String? = "",
    @SerializedName("results")
    val results: List<Result?>? = listOf(),
    @SerializedName("transaction_details")
    val transactionDetails: TransactionDetails? = TransactionDetails()
) {
    data class Result(
        @SerializedName("amount_spent")
        val amountSpent: String? = "",
        @SerializedName("balance_amount")
        val balanceAmount: String? = "",
        @SerializedName("created_at")
        val createdAt: String? = "",
        @SerializedName("shop_name")
        val shopName: String? = "",
        @SerializedName("transaction_amount")
        val transactionAmount: String? = "",
        @SerializedName("transaction_type")
        val transactionType: String? = ""
    ) {

        enum class TransactionType(val value: String) {
            WITHDRAW(value = "Withdraw"),
            DEPOSIT(value = "Deposit")
        }

        fun checkTransactionType(): TransactionType? {
            return if (transactionType.equals(TransactionType.WITHDRAW.value, ignoreCase = true)) {
                TransactionType.WITHDRAW
            } else if (transactionType.equals(TransactionType.DEPOSIT.value, ignoreCase = true)) {
                TransactionType.DEPOSIT
            } else {
                null
            }
        }

    }

    data class TransactionDetails(
        @SerializedName("cashback_balance")
        val cashbackBalance: String? = "0.0",
        @SerializedName("processing")
        val processing: String? = "0.0"
    )
}