package com.kash4me.data.models


data class CashBackAmountResponse(
    val cashback_details: List<CashbackDetail>,
    val total_cashback_amount: String
)

data class CashbackDetail(
    val cb_amount: String,
    val cb_rule: Int,
    val maturity_status: Boolean
)