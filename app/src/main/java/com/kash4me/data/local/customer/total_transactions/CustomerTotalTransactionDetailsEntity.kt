package com.kash4me.data.local.customer.total_transactions


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kash4me.utils.AppConstants

@Entity(tableName = "customer_total_transaction_details")
data class CustomerTotalTransactionDetailsEntity(
    @ColumnInfo(name = "results")
    val results: List<Result>? = listOf(),
    @ColumnInfo(name = "transaction_details")
    val transactionDetails: TransactionDetails? = TransactionDetails()
) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = AppConstants.DEFAULT_PRIMARY_KEY

    data class Result(
        @ColumnInfo(name = "amount_spent")
        val amountSpent: String? = "",
        @ColumnInfo(name = "balance_amount")
        val balanceAmount: String? = "",
        @ColumnInfo(name = "created_at")
        val createdAt: String? = "",
        @ColumnInfo(name = "shop_name")
        val shopName: String? = "",
        @ColumnInfo(name = "transaction_amount")
        val transactionAmount: String? = "",
        @ColumnInfo(name = "transaction_type")
        val transactionType: String? = ""
    ) {

        enum class TransactionType(val value: String) {
            WITHDRAW(value = "Withdraw"),
            DEPOSIT(value = "Deposit"),
            REFUND(value = "Refund"),
        }

        fun checkTransactionType(): TransactionType? {
            return if (transactionType.equals(TransactionType.WITHDRAW.value, ignoreCase = true)) {
                TransactionType.WITHDRAW
            } else if (transactionType.equals(TransactionType.DEPOSIT.value, ignoreCase = true)) {
                TransactionType.DEPOSIT
            } else if (transactionType.equals(TransactionType.REFUND.value, ignoreCase = true)) {
                TransactionType.REFUND
            } else {
                null
            }
        }

    }

    data class TransactionDetails(
        @ColumnInfo(name = "cashback_balance")
        val cashbackBalance: String? = "0.0",
        @ColumnInfo(name = "processing")
        val processing: String? = "0.0"
    )
}