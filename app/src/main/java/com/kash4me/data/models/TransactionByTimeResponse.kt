package com.kash4me.data.models


data class TransactionByTimeResponse(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<Results>
)

data class Transaction(
    val amount_spent: String,
    val cashback_amount: String,
    val created_at: String,
    val customer: UserDetails? = null,
    val id: Int,
    val customer_details: CustomerDetailsOfTimeResponse
)

data class Results(
    val date: String,
    val transactions: List<Transaction>
)

data class CustomerDetailsOfTimeResponse(
    val id: Int? = null,
    val nick_name: String? = null
)