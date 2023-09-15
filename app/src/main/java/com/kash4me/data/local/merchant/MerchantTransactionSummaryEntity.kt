package com.kash4me.data.local.merchant

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kash4me.data.models.Monthly
import com.kash4me.data.models.Today
import com.kash4me.data.models.Weekly

@Entity(tableName = "merchant_transaction_summary")
data class MerchantTransactionSummaryEntity(

    @ColumnInfo(name = "monthly")
    val monthly: Monthly?,

    @ColumnInfo(name = "today")
    val today: Today?,

    @ColumnInfo(name = "weekly")
    val weekly: Weekly?,

    @ColumnInfo(name = "payment_status")
    val isPaymentSetupComplete: Boolean? = null

) {

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int = 1

}