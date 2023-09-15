package com.kash4me.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CashBackSuccessResponse(
    val amount_left: String? = "",
    val amount_spent: String? = "",
    val cashback_amount: String? = "",
    @SerializedName("total_cashback_amount")
    val totalCashbackAmount: String? = "",
    val cashback_maturity_status_display: String? = "",
    val created_at: String? = "",
    val parent_txn: ParentTxn? = null
) : Parcelable

@Parcelize
data class ParentTxn(
    val amount_spent: String? = "",
    val cashback_amount: String? = "",
    val cashback_maturity_status: String? = "",
    val created_at: String? = "",
    val parent_id: Int? = 0
) : Parcelable