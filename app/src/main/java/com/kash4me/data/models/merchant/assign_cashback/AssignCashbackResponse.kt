package com.kash4me.data.models.merchant.assign_cashback


import com.google.gson.annotations.SerializedName

data class AssignCashbackResponse(
    @SerializedName("cashback_details")
    val cashbackDetails: List<CashbackDetail?>? = listOf(),
    @SerializedName("total_cashback_amount")
    val totalCashbackAmount: String? = ""
) {
    data class CashbackDetail(
        @SerializedName("cb_amount")
        val cbAmount: String? = "",
        @SerializedName("cb_rule")
        val cbRule: String? = "",
        @SerializedName("maturity_status")
        val maturityStatus: Boolean? = false
    )
}