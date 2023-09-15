package com.kash4me.data.local.customer.total_transactions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kash4me.utils.AppConstants

@Entity(tableName = "processing_transactions")
data class ProcessingTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = AppConstants.DEFAULT_PRIMARY_KEY,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = "",
    @ColumnInfo(name = "params")
    val params: Params? = Params(),
    @ColumnInfo(name = "shop_name")
    val shopName: String? = "",
    @ColumnInfo(name = "transaction_type")
    val transactionType: String? = ""
) {
    data class Params(
        @ColumnInfo(name = "amount_spent")
        val amountSpent: String? = "",
        @ColumnInfo(name = "cashback_amount")
        val cashbackAmount: String? = ""
    )
}