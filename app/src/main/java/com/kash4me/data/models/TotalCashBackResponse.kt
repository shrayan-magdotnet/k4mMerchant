package com.kash4me.data.models

data class TotalCashBackResponse(
    val count: Int,
    val next: Any,
    val previous: Any,
    val results: List<CashBack>,
    val transaction_details: TransactionDetails
)

data class TransactionDetails(
    val cashback_balance: String,
    val processing: String
)

data class CashBack(
    val cashback_amount: String,
    val cashback_status: String,
    val created_at: String,
    val shop_name: String,
    val txn_id: Int
)